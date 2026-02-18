# Remediation Progress Report

- Date: 2026-02-18
- Order: P0 -> P1
- Scope: Android + commonMain

## 1) P0 - Statistics scale formula fix (DONE)

### Action
- Fixed normalized max calculation to exact ceil rule.
- Added unit tests for boundary and rounding cases.

### Files
- `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsContract.kt`
- `composeApp/src/commonTest/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsContractTest.kt`

### Result
- `normalizedMaxCount` now follows `ceil(maxCount/step) * step`.
- `:composeApp:allTests` passed.

## 2) P1-1 - Recommended mode spec conflict resolution (DONE)

### Action
- Resolved baseline document conflict by aligning spec draft with current implemented behavior (recommended = fully random 5 sets).

### File
- `로또 번호 발행:조회 앱 기획서 (초안)`

## 3) P1-2 - History mixed valid/invalid input handling (DONE)

### Action
- Changed parsing logic to reject query if any token is out of range (1..45).
- Kept invalid-format error handling.

### File
- `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt`

## 4) P1-3 - Settings latest status semantics (DONE)

### Action
- Added repository contract for remote latest draw lookup.
- Updated app-info use case to compare local latest with remote latest when available.
- Retained last-sync-time fallback when remote check fails.

### Files
- `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/data/repository/LottoRepository.kt`
- `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/data/repository/LottoRepositoryImpl.kt`
- `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/usecase/GetAppInfoUseCase.kt`

## 5) P1-4 - Runtime requirement doc alignment (DONE)

### Action
- Updated README requirements to reflect actual executable environment (JBR 21 daemon + Java 17 compile target).

### File
- `README.md`

## Verification Commands

- `./gradlew :composeApp:allTests` -> SUCCESS
- `./gradlew :composeApp:assembleDebug` -> SUCCESS

## Remaining Risk

- AGP warning remains for `compileSdk=35` with AGP 8.5.2.
- Deprecated Gradle features warning remains (Gradle 9 compatibility).
