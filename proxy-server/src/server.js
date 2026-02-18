import express from "express";
import * as cheerio from "cheerio";
import { readFile } from "node:fs/promises";

const app = express();
const PORT = Number(process.env.PORT ?? 8787);
const REQUEST_TIMEOUT_MS = 10_000;
const DRAW_CACHE = new Map();
const DRAW_CACHE_TTL_MS = 1000 * 60 * 60 * 24;
const MAIN_INFO_URL = "https://www.dhlottery.co.kr/selectMainInfo.do";
const MAIN_INFO_CACHE_TTL_MS = 1000 * 30;
const MAIN_INFO_CACHE = {
  fetchedAt: 0,
  byDrawNo: new Map()
};
const BOOTSTRAP_FILE_URL = new URL("../data/lotto_seed.json", import.meta.url);
const BOOTSTRAP_CACHE_TTL_MS = 1000 * 60;
const BOOTSTRAP_CACHE = {
  fetchedAt: 0,
  draws: null
};
let bootstrapMissingLogged = false;

function log(level, message, meta = undefined) {
  const prefix = `[lotto-proxy][${new Date().toISOString()}][${level}]`;
  if (meta === undefined) {
    console.log(`${prefix} ${message}`);
  } else {
    console.log(`${prefix} ${message}`, meta);
  }
}

function nowMs() {
  return Date.now();
}

function estimateLatestDrawNo() {
  const firstDrawUtc = Date.UTC(2002, 11, 7); // 2002-12-07
  const diffDays = Math.floor((nowMs() - firstDrawUtc) / (24 * 60 * 60 * 1000));
  return Math.floor(diffDays / 7) + 1;
}

function parseYmd(ymd) {
  const value = String(ymd ?? "").trim();
  if (/^\d{8}$/.test(value)) {
    return `${value.slice(0, 4)}-${value.slice(4, 6)}-${value.slice(6, 8)}`;
  }
  return "";
}

function toFetchHeaders() {
  return {
    "User-Agent":
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
    "Accept": "*/*",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    "Referer": "https://www.dhlottery.co.kr/gameResult.do?method=byWin",
    "Origin": "https://www.dhlottery.co.kr",
    "Cache-Control": "no-cache"
  };
}

async function fetchText(url) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    log("INFO", "upstream fetch start", { url });
    const res = await fetch(url, {
      headers: toFetchHeaders(),
      redirect: "follow",
      signal: controller.signal
    });
    const text = await res.text();
    const payload = {
      status: res.status,
      url: res.url,
      text
    };
    log("INFO", "upstream fetch done", {
      url,
      status: payload.status,
      resolvedUrl: payload.url,
      textLength: text.length
    });
    return payload;
  } finally {
    clearTimeout(timeout);
  }
}

function parseJsonPayload(text) {
  const trimmed = text.trim();
  if (!trimmed.startsWith("{")) {
    return null;
  }

  try {
    const parsed = JSON.parse(trimmed);
    if (parsed?.returnValue === "success" && Number(parsed?.drwNo) > 0) {
      return parsed;
    }
  } catch {
    return null;
  }
  return null;
}

function parseByWinHtml(drawNo, html) {
  const $ = cheerio.load(html);
  const numbers = [];
  $(".num.win p span.ball_645").each((_, el) => {
    const n = Number($(el).text().trim());
    if (Number.isFinite(n)) numbers.push(n);
  });

  let bonus = Number($(".num.bonus p span.ball_645").first().text().trim());
  if (!Number.isFinite(bonus)) bonus = 0;

  let drawDate = "";
  const dateText = $(".win_result .desc").first().text().trim();
  const dateMatch = dateText.match(/(\d{4})\D+(\d{1,2})\D+(\d{1,2})/);
  if (dateMatch) {
    const [, y, m, d] = dateMatch;
    drawDate = `${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`;
  }

  if (numbers.length < 6) {
    return null;
  }

  return {
    drawNo,
    drawDate,
    numbers: numbers.slice(0, 6).sort((a, b) => a - b),
    bonus,
    firstPrizeAmount: 0
  };
}

function normalizeJsonDraw(json) {
  return {
    drawNo: Number(json.drwNo),
    drawDate: String(json.drwNoDate ?? ""),
    numbers: [
      Number(json.drwtNo1),
      Number(json.drwtNo2),
      Number(json.drwtNo3),
      Number(json.drwtNo4),
      Number(json.drwtNo5),
      Number(json.drwtNo6)
    ].sort((a, b) => a - b),
    bonus: Number(json.bnusNo),
    firstPrizeAmount: Number(json.firstWinamnt ?? 0)
  };
}

function normalizeMainInfoDraw(item) {
  const drawNo = Number(item?.ltEpsd);
  const numbers = [
    Number(item?.tm1WnNo),
    Number(item?.tm2WnNo),
    Number(item?.tm3WnNo),
    Number(item?.tm4WnNo),
    Number(item?.tm5WnNo),
    Number(item?.tm6WnNo)
  ];

  if (!Number.isFinite(drawNo) || numbers.some((n) => !Number.isFinite(n))) {
    return null;
  }

  return {
    drawNo,
    drawDate: parseYmd(item?.ltRflYmd),
    numbers: numbers.sort((a, b) => a - b),
    bonus: Number(item?.bnsWnNo) || 0,
    firstPrizeAmount: Number(item?.rnk1WnAmt ?? 0)
  };
}

function isValidDrawShape(draw) {
  return (
    Number.isFinite(Number(draw?.drawNo)) &&
    typeof draw?.drawDate === "string" &&
    Array.isArray(draw?.numbers) &&
    draw.numbers.length === 6 &&
    draw.numbers.every((n) => Number.isFinite(Number(n))) &&
    Number.isFinite(Number(draw?.bonus)) &&
    Number.isFinite(Number(draw?.firstPrizeAmount))
  );
}

function normalizeSeedDraw(raw) {
  const numbers = raw.numbers.map((n) => Number(n)).sort((a, b) => a - b);
  return {
    drawNo: Number(raw.drawNo),
    drawDate: String(raw.drawDate),
    numbers,
    bonus: Number(raw.bonus),
    firstPrizeAmount: Number(raw.firstPrizeAmount)
  };
}

async function loadBootstrapDraws(force = false) {
  const useCache = !force && nowMs() - BOOTSTRAP_CACHE.fetchedAt < BOOTSTRAP_CACHE_TTL_MS;
  if (useCache && Array.isArray(BOOTSTRAP_CACHE.draws)) {
    return BOOTSTRAP_CACHE.draws;
  }

  try {
    const text = await readFile(BOOTSTRAP_FILE_URL, "utf8");
    const parsed = JSON.parse(text);
    const list = Array.isArray(parsed) ? parsed : parsed?.draws;
    if (!Array.isArray(list)) {
      log("ERROR", "bootstrap file has invalid schema (draws array missing)");
      BOOTSTRAP_CACHE.fetchedAt = nowMs();
      BOOTSTRAP_CACHE.draws = [];
      return [];
    }

    const draws = list.filter(isValidDrawShape).map(normalizeSeedDraw).sort((a, b) => a.drawNo - b.drawNo);
    BOOTSTRAP_CACHE.fetchedAt = nowMs();
    BOOTSTRAP_CACHE.draws = draws;
    log("INFO", "bootstrap file loaded", { count: draws.length });
    return draws;
  } catch (error) {
    if (error?.code === "ENOENT") {
      if (!bootstrapMissingLogged) {
        log("WARN", "bootstrap file missing; fallback to live sync only", { file: BOOTSTRAP_FILE_URL.pathname });
        bootstrapMissingLogged = true;
      }
      BOOTSTRAP_CACHE.fetchedAt = nowMs();
      BOOTSTRAP_CACHE.draws = [];
      return [];
    }
    log("ERROR", "bootstrap file load failed", { error: String(error) });
    throw error;
  }
}

function parseMainInfo(text) {
  let payload;
  try {
    payload = JSON.parse(String(text ?? ""));
  } catch {
    return null;
  }

  const list = payload?.data?.result?.pstLtEpstInfo?.lt645;
  if (!Array.isArray(list)) {
    return null;
  }

  const byDrawNo = new Map();
  for (const row of list) {
    const normalized = normalizeMainInfoDraw(row);
    if (normalized) {
      byDrawNo.set(normalized.drawNo, normalized);
    }
  }
  return byDrawNo;
}

async function refreshMainInfo(force = false) {
  const useCache = !force && nowMs() - MAIN_INFO_CACHE.fetchedAt < MAIN_INFO_CACHE_TTL_MS;
  if (useCache && MAIN_INFO_CACHE.byDrawNo.size > 0) {
    return MAIN_INFO_CACHE.byDrawNo;
  }

  const res = await fetchText(MAIN_INFO_URL);
  const parsed = parseMainInfo(res.text);
  if (!parsed || parsed.size === 0) {
    log("ERROR", "selectMainInfo parse failed", {
      status: res.status,
      resolvedUrl: res.url,
      textPreview: String(res.text).slice(0, 180)
    });
    return null;
  }

  MAIN_INFO_CACHE.fetchedAt = nowMs();
  MAIN_INFO_CACHE.byDrawNo = parsed;
  log("INFO", "selectMainInfo parsed", { count: parsed.size });
  return parsed;
}

async function fetchDraw(drawNo, allowLegacyFallback = true) {
  const cached = DRAW_CACHE.get(drawNo);
  if (cached && nowMs() - cached.cachedAt < DRAW_CACHE_TTL_MS) {
    log("DEBUG", "draw cache hit", { drawNo });
    return cached.data;
  }

  try {
    const mainInfoMap = await refreshMainInfo(false);
    const fromMainInfo = mainInfoMap?.get(drawNo) ?? null;
    if (fromMainInfo) {
      DRAW_CACHE.set(drawNo, { data: fromMainInfo, cachedAt: nowMs() });
      log("INFO", "draw fetched by selectMainInfo", { drawNo });
      return fromMainInfo;
    }
  } catch (error) {
    log("WARN", "selectMainInfo fetch failed; fallback to legacy sources", {
      drawNo,
      error: String(error)
    });
  }

  if (!allowLegacyFallback) {
    log("DEBUG", "draw not found in selectMainInfo; legacy fallback skipped", { drawNo });
    return null;
  }

  const candidates = [
    `https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=${drawNo}`,
    `https://www.nlotto.co.kr/common.do?method=getLottoNumber&drwNo=${drawNo}`
  ];

  for (const url of candidates) {
    try {
      const res = await fetchText(url);
      const parsed = parseJsonPayload(res.text);
      if (parsed) {
        const data = normalizeJsonDraw(parsed);
        DRAW_CACHE.set(drawNo, { data, cachedAt: nowMs() });
        log("INFO", "draw fetched by json api", { drawNo, source: url });
        return data;
      }
      log("WARN", "json payload parse failed", { drawNo, source: url, status: res.status });
    } catch {
      log("WARN", "json source failed", { drawNo, source: url });
      // try next source
    }
  }

  try {
    const htmlRes = await fetchText(
      `https://www.dhlottery.co.kr/gameResult.do?method=byWin&drwNo=${drawNo}`
    );
    const parsedHtml = parseByWinHtml(drawNo, htmlRes.text);
    if (parsedHtml) {
      DRAW_CACHE.set(drawNo, { data: parsedHtml, cachedAt: nowMs() });
      log("INFO", "draw fetched by html fallback", { drawNo });
      return parsedHtml;
    }
    log("WARN", "html fallback parse failed", { drawNo, status: htmlRes.status });
  } catch {
    log("WARN", "html fallback failed", { drawNo });
    // fall through
  }

  log("ERROR", "draw fetch failed", { drawNo });
  return null;
}

app.get("/health", (_, res) => {
  res.json({ ok: true, now: new Date().toISOString() });
});

app.get("/api/lotto/latest", async (_, res) => {
  try {
    const estimated = estimateLatestDrawNo();
    log("INFO", "latest request", { estimated });

    const mainInfoMap = await refreshMainInfo(true);
    if (mainInfoMap && mainInfoMap.size > 0) {
      const latestDrawNo = Math.max(...Array.from(mainInfoMap.keys()));
      log("INFO", "latest resolved via selectMainInfo", { latestDrawNo });
      return res.json({ latestDrawNo });
    }

    log("WARN", "latest falling back to legacy probe", { estimated });
    for (let drawNo = estimated; drawNo >= Math.max(1, estimated - 20); drawNo -= 1) {
      const draw = await fetchDraw(drawNo);
      if (draw) {
        log("INFO", "latest resolved via legacy fallback", { latestDrawNo: drawNo });
        return res.json({ latestDrawNo: drawNo });
      }
    }

    log("ERROR", "latest resolution failed", { estimated });
    return res.status(503).json({ message: "Failed to resolve latest draw" });
  } catch (error) {
    log("ERROR", "latest endpoint internal error", { error: String(error) });
    return res.status(500).json({ message: "Internal error", error: String(error) });
  }
});

app.get("/api/lotto/bootstrap", async (_, res) => {
  try {
    const draws = await loadBootstrapDraws(false);
    if (draws.length === 0) {
      return res.status(404).json({
        message: "Bootstrap seed file not found or empty",
        hint: "Create proxy-server/data/lotto_seed.json"
      });
    }
    return res.json({ draws });
  } catch (error) {
    log("ERROR", "bootstrap endpoint internal error", { error: String(error) });
    return res.status(500).json({ message: "Internal error", error: String(error) });
  }
});

app.get("/api/lotto/draws", async (req, res) => {
  const start = Number(req.query.start);
  const end = Number(req.query.end);
  log("INFO", "draws request", { start, end });

  if (!Number.isFinite(start) || !Number.isFinite(end) || start <= 0 || end <= 0 || start > end) {
    return res.status(400).json({ message: "Invalid range" });
  }

  if (end - start > 300) {
    return res.status(400).json({ message: "Range too large. Max 301 draws per request." });
  }

  try {
    const allowLegacyFallback = end - start <= 2;
    const draws = [];
    for (let drawNo = start; drawNo <= end; drawNo += 1) {
      const draw = await fetchDraw(drawNo, allowLegacyFallback);
      if (draw) draws.push(draw);
    }
    log("INFO", "draws response", { start, end, count: draws.length, allowLegacyFallback });
    return res.json({ draws });
  } catch (error) {
    log("ERROR", "draws endpoint internal error", { start, end, error: String(error) });
    return res.status(500).json({ message: "Internal error", error: String(error) });
  }
});

app.listen(PORT, () => {
  log("INFO", `listening on ${PORT}`);
});
