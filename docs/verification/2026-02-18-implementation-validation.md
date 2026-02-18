# LottoGen Implementation Validation Report

- Date: 2026-02-18
- Scope: Android + commonMain (code/log/build evidence)
- Baseline docs: `로또 번호 발행:조회 앱 기획서 (초안)`, `README.md`

## Stage 0 - Requirement Baseline

### Decision rules
- PASS: requirement is implemented with clear code path + behavior evidence.
- PARTIAL: implemented but deviates from baseline rule or leaves ambiguity.
- FAIL: requirement is missing or contradicts baseline.
- N/A: out of current scope.

### Requirement matrix (baseline -> implementation)

| ID | Requirement | Result | Evidence |
|---|---|---|---|
| REQ-001 | Splash fade-in 0.5s | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:22`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:69`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashScreen.kt:63` |
| REQ-002 | Splash minimum 3s | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:21`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:44` |
| REQ-003 | Sync in background and then navigate home | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:49`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:85`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/navigation/NavGraph.kt:26` |
| REQ-004 | Sync failure: keep app usable with cached data + message | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:56`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashViewModel.kt:93`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/splash/SplashContract.kt:17` |
| REQ-005 | Home has 5 bottom tabs and default tab is Generate | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/home/HomeScreen.kt:43`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/home/HomeScreen.kt:53`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/home/HomeScreen.kt:82` |
| REQ-006 | Banner ad fixed in bottom area across tab switching | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/home/HomeScreen.kt:56`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/home/HomeScreen.kt:58`, `composeApp/src/androidMain/kotlin/com/hsmomo/lottogen/presentation/components/AdBanner.android.kt:19` |
| REQ-007 | Generate tab has recommended/high/low/mixed controls | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateScreen.kt:141`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateScreen.kt:147`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateScreen.kt:160`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateScreen.kt:166` |
| REQ-008 | Recommended generates 5 sets via 5 strategy modes (spec) | PARTIAL | 5 sets are generated (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateViewModel.kt:33`) but recommended path uses random only (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/usecase/GenerateNumbersUseCase.kt:22`). Note: `README.md:35` defines recommended as fully random, conflicting with spec draft (`기획서` line 103-113). |
| REQ-009 | High/Low probability use weighted top/bottom groups and 5 sets | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/strategy/HighProbabilityStrategy.kt:13`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/strategy/HighProbabilityStrategy.kt:24`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/strategy/LowProbabilityStrategy.kt:13`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/strategy/LowProbabilityStrategy.kt:24` |
| REQ-010 | Mixed mode: choose up to 6 numbers from 1..45 and fill remaining randomly | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateViewModel.kt:70`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateScreen.kt:91`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/strategy/RandomStrategy.kt:19` |
| REQ-011 | Generated rows support bookmark toggle with DB save/delete + timestamp | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/generate/GenerateViewModel.kt:103`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/usecase/BookmarkUseCase.kt:14`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/data/repository/LottoRepositoryImpl.kt:142`, `composeApp/src/commonMain/sqldelight/com/hsmomo/lottogen/db/LottoDatabase.sq:17` |
| REQ-012 | History list shows draw no/date/6 numbers+bonus/1st prize | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryScreen.kt:154`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryScreen.kt:165`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryScreen.kt:171` |
| REQ-013 | History search supports comma input and include-all filtering | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryScreen.kt:72`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt:49`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/data/repository/LottoRepositoryImpl.kt:103` |
| REQ-014 | History input validation should guide out-of-range/invalid input | PARTIAL | Invalid format shows error (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt:56`) and empty valid numbers shows range error (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt:61`), but mixed valid+out-of-range values are silently filtered (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt:53`). |
| REQ-015 | Statistics shows 1..45 counts with bar graph | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/data/repository/LottoRepositoryImpl.kt:115`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsScreen.kt:85`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsScreen.kt:112` |
| REQ-016 | Statistics bar max uses ceil(maxCount/step)*step | FAIL | Current formula over-increments when max is exact multiple (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsContract.kt:19`). |
| REQ-017 | Bookmark tab shows saved sets + saved date + unbookmark action | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/bookmark/BookmarkScreen.kt:82`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/bookmark/BookmarkScreen.kt:107`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/bookmark/BookmarkScreen.kt:121` |
| REQ-018 | Settings shows latest-update status and app version | PASS | `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/settings/SettingsContract.kt:15`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/settings/SettingsScreen.kt:65`, `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/settings/SettingsScreen.kt:93` |
| REQ-019 | Update status should compare remote latest draw no vs local max | PARTIAL | Current logic uses local latest + last sync age heuristic (`composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/usecase/GetAppInfoUseCase.kt:14`) rather than direct remote-vs-local comparison from settings context. |
| REQ-020 | Local schema supports WinningDraw/Bookmark/AppMeta model | PASS | `composeApp/src/commonMain/sqldelight/com/hsmomo/lottogen/db/LottoDatabase.sq:2`, `composeApp/src/commonMain/sqldelight/com/hsmomo/lottogen/db/LottoDatabase.sq:17`, `composeApp/src/commonMain/sqldelight/com/hsmomo/lottogen/db/LottoDatabase.sq:24` |

Stage 0 summary:
- PASS: 16
- PARTIAL: 3
- FAIL: 1
- N/A: 0

## Stage 1 - Static Implementation Audit

### Key findings
1. Requirement conflict exists between planning docs:
- Spec draft requires recommended mode to represent 5 strategy families.
- README currently states recommended is fully random 5 sets.
- Implementation follows README behavior (random-only recommended).

2. Statistics max scaling currently does not match stated formula in README/spec.
- `((max/step)+1)*step` returns 110 for max 100 (step 10), while ceil rule should keep 100.

3. Settings "needs update" semantics are heuristic (7-day window), not strict latest draw comparison.
- This can show update-needed despite local latest draw being already current.

4. Test coverage gap:
- No `commonTest`/`androidTest` verification for generation/search/statistics logic.

## Stage 2 - Build/Runtime Evidence

Commands executed:
- `./gradlew :composeApp:assembleDebug`
- `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`

Result:
- Both commands succeeded (`BUILD SUCCESSFUL`).

Warnings captured:
- AGP 8.5.2 with `compileSdk = 35` compatibility warning.
- Gradle deprecated features warning for Gradle 9 compatibility.

Environment consistency note:
- README says JDK 17 (`README.md:210`), but daemon toolchain requires JetBrains JDK 21 (`gradle/gradle-daemon-jvm.properties:2`, `gradle/gradle-daemon-jvm.properties:3`).

## Stage 3 - Final Assessment

### Final status
- Overall: **PARTIALLY COMPLIANT**
- Core app flows are implemented and buildable.
- 1 functional mismatch (statistics scaling), 3 partial mismatches (recommended strategy intent, search validation nuance, settings latest-check semantics).

### Priority backlog

#### P0 (must fix)
1. Fix statistics normalized max formula to exact ceil rule.
- Target: `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/statistics/StatisticsContract.kt`

#### P1 (should fix next)
1. Resolve spec-vs-README conflict for recommended generation mode.
- Either update spec to match random-only behavior, or implement multi-strategy aggregation.

2. Strengthen history input validation feedback for mixed valid/invalid tokens.
- Target: `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/presentation/history/HistoryViewModel.kt`

3. Align settings "latest" status with remote latest draw comparison semantics.
- Targets: `composeApp/src/commonMain/kotlin/com/hsmomo/lottogen/domain/usecase/GetAppInfoUseCase.kt`, data source contract

4. Align runtime requirements docs (JDK section) with actual toolchain requirements.
- Target: `README.md`

#### P2 (quality)
1. Add automated tests for generation, search filtering, statistics scaling, and bookmark toggle behavior.
2. Add CI check to prevent README/runtime requirement drift.

## Acceptance checklist for next execution phase
- [ ] REQ-016 fixed and unit-tested.
- [ ] REQ-008 baseline conflict resolved by product decision.
- [ ] REQ-014 validation behavior defined and implemented.
- [ ] REQ-019 latest-check semantics finalized and implemented.
- [ ] README environment requirements updated to match executable setup.
