import express from "express";
import * as cheerio from "cheerio";

const app = express();
const PORT = Number(process.env.PORT ?? 8787);
const REQUEST_TIMEOUT_MS = 10_000;
const DRAW_CACHE = new Map();
const DRAW_CACHE_TTL_MS = 1000 * 60 * 60 * 24;

function nowMs() {
  return Date.now();
}

function estimateLatestDrawNo() {
  const firstDrawUtc = Date.UTC(2002, 11, 7); // 2002-12-07
  const diffDays = Math.floor((nowMs() - firstDrawUtc) / (24 * 60 * 60 * 1000));
  return Math.floor(diffDays / 7) + 1;
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
    const res = await fetch(url, {
      headers: toFetchHeaders(),
      redirect: "follow",
      signal: controller.signal
    });
    const text = await res.text();
    return {
      status: res.status,
      url: res.url,
      text
    };
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

async function fetchDraw(drawNo) {
  const cached = DRAW_CACHE.get(drawNo);
  if (cached && nowMs() - cached.cachedAt < DRAW_CACHE_TTL_MS) {
    return cached.data;
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
        return data;
      }
    } catch {
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
      return parsedHtml;
    }
  } catch {
    // fall through
  }

  return null;
}

app.get("/health", (_, res) => {
  res.json({ ok: true, now: new Date().toISOString() });
});

app.get("/api/lotto/latest", async (_, res) => {
  try {
    const estimated = estimateLatestDrawNo();
    for (let drawNo = estimated; drawNo >= Math.max(1, estimated - 20); drawNo -= 1) {
      const draw = await fetchDraw(drawNo);
      if (draw) {
        return res.json({ latestDrawNo: drawNo });
      }
    }
    return res.status(503).json({ message: "Failed to resolve latest draw" });
  } catch (error) {
    return res.status(500).json({ message: "Internal error", error: String(error) });
  }
});

app.get("/api/lotto/draws", async (req, res) => {
  const start = Number(req.query.start);
  const end = Number(req.query.end);

  if (!Number.isFinite(start) || !Number.isFinite(end) || start <= 0 || end <= 0 || start > end) {
    return res.status(400).json({ message: "Invalid range" });
  }

  if (end - start > 300) {
    return res.status(400).json({ message: "Range too large. Max 301 draws per request." });
  }

  try {
    const draws = [];
    for (let drawNo = start; drawNo <= end; drawNo += 1) {
      const draw = await fetchDraw(drawNo);
      if (draw) draws.push(draw);
    }
    return res.json({ draws });
  } catch (error) {
    return res.status(500).json({ message: "Internal error", error: String(error) });
  }
});

app.listen(PORT, () => {
  console.log(`[lotto-proxy] listening on ${PORT}`);
});
