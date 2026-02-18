import { readFile } from "node:fs/promises";

function fail(message) {
  console.error(`[seed-validate][ERROR] ${message}`);
}

function info(message) {
  console.log(`[seed-validate][INFO] ${message}`);
}

function parseJsonDate(value) {
  if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const t = Date.parse(`${value}T00:00:00Z`);
  return Number.isFinite(t);
}

async function main() {
  const filePath = process.argv[2] ?? "data/lotto_seed.json";
  let raw;
  try {
    raw = await readFile(filePath, "utf8");
  } catch (e) {
    fail(`cannot read file: ${filePath} (${e.code ?? e.message})`);
    process.exit(1);
  }

  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (e) {
    fail(`invalid JSON: ${e.message}`);
    process.exit(1);
  }

  const draws = Array.isArray(parsed) ? parsed : parsed?.draws;
  if (!Array.isArray(draws)) {
    fail("schema invalid: expected array or { draws: [] }");
    process.exit(1);
  }

  const errors = [];
  const drawNos = [];
  const seen = new Set();

  draws.forEach((draw, index) => {
    const path = `draws[${index}]`;
    const drawNo = Number(draw?.drawNo);
    const bonus = Number(draw?.bonus);
    const firstPrizeAmount = Number(draw?.firstPrizeAmount);
    const numbers = draw?.numbers;

    if (!Number.isInteger(drawNo) || drawNo <= 0) {
      errors.push(`${path}.drawNo must be positive integer`);
    } else {
      drawNos.push(drawNo);
      if (seen.has(drawNo)) errors.push(`${path}.drawNo duplicated: ${drawNo}`);
      seen.add(drawNo);
    }

    if (!parseJsonDate(draw?.drawDate)) {
      errors.push(`${path}.drawDate must be yyyy-MM-dd`);
    }

    if (!Array.isArray(numbers) || numbers.length !== 6) {
      errors.push(`${path}.numbers must be length-6 array`);
    } else {
      const nSet = new Set(numbers);
      if (nSet.size !== 6) errors.push(`${path}.numbers must not contain duplicates`);
      for (const n of numbers) {
        if (!Number.isInteger(n) || n < 1 || n > 45) {
          errors.push(`${path}.numbers must be integers in [1,45]`);
          break;
        }
      }
    }

    if (!Number.isInteger(bonus) || bonus < 1 || bonus > 45) {
      errors.push(`${path}.bonus must be integer in [1,45]`);
    }

    if (!Number.isInteger(firstPrizeAmount) || firstPrizeAmount < 0) {
      errors.push(`${path}.firstPrizeAmount must be non-negative integer`);
    }
  });

  if (errors.length > 0) {
    errors.slice(0, 50).forEach((e) => fail(e));
    if (errors.length > 50) fail(`...and ${errors.length - 50} more errors`);
    process.exit(1);
  }

  drawNos.sort((a, b) => a - b);
  const min = drawNos[0];
  const max = drawNos[drawNos.length - 1];
  const missing = [];
  for (let n = min; n <= max; n += 1) {
    if (!seen.has(n)) missing.push(n);
  }

  info(`file=${filePath}`);
  info(`draw count=${draws.length}, range=${min}..${max}`);
  if (missing.length > 0) {
    const preview = missing.slice(0, 30).join(", ");
    fail(`missing drawNo count=${missing.length}, sample=[${preview}]`);
    process.exit(2);
  }

  info("validation passed");
}

main().catch((e) => {
  fail(`unexpected failure: ${e.message}`);
  process.exit(1);
});
