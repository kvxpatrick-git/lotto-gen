# LottoGen - 로또 번호 생성기

다양한 전략으로 로또 번호를 생성하고 관리하는 **Android/iOS 크로스 플랫폼** 앱

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Kotlin Multiplatform (KMP) |
| UI | Compose Multiplatform (Material3) |
| Architecture | MVI (Model-View-Intent) |
| Async | Coroutines + Flow |
| DI | Koin (Multiplatform) |
| Local DB | SQLDelight (Multiplatform) |
| Navigation | Navigation Compose |
| Ads | Google Mobile Ads SDK (AdMob) |
| Build | Gradle (Version Catalog) |

## 지원 플랫폼

| 플랫폼 | 상태 |
|--------|------|
| Android | ✅ 지원 |
| iOS | ✅ 지원 |

## 주요 기능

### 1. 스플래시 화면
- 로고 0.5초 페이드인 애니메이션
- 최소 3초 노출
- 백그라운드에서 당첨번호 데이터 동기화
- 동기화 실패 시 캐시 데이터 사용 + 토스트 알림

### 2. 번호발행 탭
- **추천번호**: 완전 랜덤 5세트 생성
- **높은확률**: 통계 상위 15개 번호 기반 가중 랜덤 5세트
- **낮은확률**: 통계 하위 15개 번호 기반 가중 랜덤 5세트
- **혼합선택**: 1~45 그리드에서 직접 선택 후 나머지 랜덤 채움
- 북마크 토글 (즉시 DB 저장/삭제)

### 3. 히스토리 탭
- 전체 당첨번호 목록 (회차, 날짜, 번호, 보너스, 1등 당첨금)
- 쉼표 구분 번호 검색 (예: `3,12,19`)
- **검색 조건**: 입력된 번호를 "모두 포함"한 회차만 필터링

### 4. 통계 탭
- 1~45 번호별 출현 횟수
- 가로 막대 그래프 시각화
- 막대 MAX = ceil(maxCount/step) * step

### 5. 북마크 탭
- 저장된 번호 조합 리스트
- 저장 날짜 표시
- 토글 OFF 시 즉시 삭제

### 6. 설정 탭
- 최신 회차 업데이트 상태 표시
- 앱 버전 표시

## 프로젝트 구조

```
LottoGen/
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Project settings
├── gradle/libs.versions.toml     # Version catalog
│
├── composeApp/                   # Shared Kotlin Multiplatform module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/           # Shared code (Android + iOS)
│       │   ├── kotlin/
│       │   │   └── com/hsmomo/lottogen/
│       │   │       ├── App.kt                    # Root Composable
│       │   │       ├── data/                     # Data Layer
│       │   │       │   ├── local/
│       │   │       │   │   └── LottoLocalDataSource.kt
│       │   │       │   ├── remote/
│       │   │       │   │   └── FakeRemoteDataSource.kt
│       │   │       │   └── repository/
│       │   │       │       ├── LottoRepository.kt
│       │   │       │       └── LottoRepositoryImpl.kt
│       │   │       ├── domain/                   # Domain Layer
│       │   │       │   ├── model/
│       │   │       │   │   ├── WinningDraw.kt
│       │   │       │   │   ├── Bookmark.kt
│       │   │       │   │   ├── NumberSet.kt
│       │   │       │   │   ├── NumberStatistics.kt
│       │   │       │   │   └── AppInfo.kt
│       │   │       │   ├── strategy/             # Strategy Pattern
│       │   │       │   │   ├── NumberGenerationStrategy.kt
│       │   │       │   │   ├── RandomStrategy.kt
│       │   │       │   │   ├── HighProbabilityStrategy.kt
│       │   │       │   │   ├── LowProbabilityStrategy.kt
│       │   │       │   │   └── MixedStrategy.kt
│       │   │       │   └── usecase/
│       │   │       │       ├── SyncDrawDataUseCase.kt
│       │   │       │       ├── GetDrawHistoryUseCase.kt
│       │   │       │       ├── GetStatisticsUseCase.kt
│       │   │       │       ├── BookmarkUseCase.kt
│       │   │       │       ├── GenerateNumbersUseCase.kt
│       │   │       │       └── GetAppInfoUseCase.kt
│       │   │       ├── presentation/             # Presentation Layer (MVI)
│       │   │       │   ├── mvi/
│       │   │       │   │   ├── MviContract.kt
│       │   │       │   │   └── MviViewModel.kt
│       │   │       │   ├── navigation/
│       │   │       │   │   └── NavGraph.kt
│       │   │       │   ├── components/
│       │   │       │   │   ├── LottoBall.kt
│       │   │       │   │   ├── NumberSetRow.kt
│       │   │       │   │   └── AdBanner.kt       # expect declaration
│       │   │       │   ├── splash/
│       │   │       │   ├── home/
│       │   │       │   ├── generate/
│       │   │       │   ├── history/
│       │   │       │   ├── statistics/
│       │   │       │   ├── bookmark/
│       │   │       │   └── settings/
│       │   │       ├── di/                       # Dependency Injection
│       │   │       │   ├── AppModule.kt          # Koin modules
│       │   │       │   └── DatabaseModule.kt     # expect declaration
│       │   │       └── ui/theme/
│       │   │           ├── Color.kt
│       │   │           ├── Theme.kt
│       │   │           └── Type.kt
│       │   └── sqldelight/                       # SQLDelight schema
│       │       └── com/hsmomo/lottogen/db/
│       │           └── LottoDatabase.sq
│       │
│       ├── androidMain/          # Android-specific code
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/
│       │   │   └── com/hsmomo/lottogen/
│       │   │       ├── MainActivity.kt
│       │   │       ├── LottoApplication.kt
│       │   │       ├── di/
│       │   │       │   └── DatabaseModule.android.kt  # actual impl
│       │   │       └── presentation/components/
│       │   │           └── AdBanner.android.kt        # actual impl
│       │   └── res/
│       │       └── values/
│       │           ├── strings.xml
│       │           └── themes.xml
│       │
│       └── iosMain/              # iOS-specific code
│           └── kotlin/
│               └── com/hsmomo/lottogen/
│                   ├── MainViewController.kt
│                   ├── di/
│                   │   └── DatabaseModule.ios.kt      # actual impl
│                   └── presentation/components/
│                       └── AdBanner.ios.kt            # actual impl
│
└── iosApp/                       # iOS Xcode project
    ├── iosApp.xcodeproj/
    └── iosApp/
        ├── ContentView.swift
        ├── iOSApp.swift
        ├── Info.plist
        └── Assets.xcassets/
```

## MVI 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                        UI (Screen)                       │
│  ┌─────────────────┐          ┌─────────────────────┐   │
│  │  State 관찰      │◄─────────│  collectAsState()   │   │
│  │  (UI 렌더링)     │          └─────────────────────┘   │
│  └─────────────────┘                                     │
│           │                                              │
│           │ 사용자 액션                                   │
│           ▼                                              │
│  ┌─────────────────┐                                     │
│  │  sendIntent()   │                                     │
│  └────────┬────────┘                                     │
└───────────┼─────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────┐
│                    ViewModel (MVI)                       │
│  ┌─────────────────┐          ┌─────────────────────┐   │
│  │  handleIntent() │─────────►│  reduce()           │   │
│  │  (Intent 처리)  │          │  (State 업데이트)    │   │
│  └─────────────────┘          └─────────────────────┘   │
│           │                                              │
│           │ 일회성 이벤트                                 │
│           ▼                                              │
│  ┌─────────────────┐                                     │
│  │ postSideEffect()│ ──────► Toast, Navigation 등        │
│  └─────────────────┘                                     │
└─────────────────────────────────────────────────────────┘
```

## expect/actual 패턴

플랫폼별 구현이 필요한 기능:

| 기능 | expect 선언 | Android actual | iOS actual |
|------|-------------|----------------|------------|
| Database Driver | `DatabaseModule.kt` | SQLDelight AndroidSqliteDriver | SQLDelight NativeSqliteDriver |
| Ad Banner | `AdBanner.kt` | Google AdMob SDK | Placeholder (추후 구현) |

## 빌드 및 실행

### 요구사항
- Android Studio Ladybug 이상 (또는 IntelliJ IDEA)
- JDK 17
- Xcode 15+ (iOS 빌드용)
- Android SDK 35 (compileSdk)
- minSdk 26 (Android 8.0)
- iOS 15.0+

### 프록시 서버 실행 (필수)
앱은 이제 로또 데이터 조회를 `proxy-server`를 통해 수행합니다.

```bash
cd proxy-server
npm install
npm run start
```

- 기본 포트: `8787`
- Android 에뮬레이터 기준 앱 기본 프록시 URL: `http://10.0.2.2:8787`
- iOS 시뮬레이터 기준 앱 기본 프록시 URL: `http://localhost:8787`

### Android 빌드
```bash
./gradlew :composeApp:assembleDebug
```

### Android 실행
```bash
./gradlew :composeApp:installDebug
```

### iOS 빌드
1. iOS 프레임워크 생성:
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

2. Xcode에서 `iosApp/iosApp.xcodeproj` 열기

3. Xcode에서 빌드 및 실행

## Git 협업 규칙

### 기본 브랜치 전략
- `main`: 배포 가능한 안정 브랜치
- 작업 브랜치: `feat/*`, `fix/*`, `chore/*`, `docs/*`

브랜치 예시:
```bash
git checkout -b feat/history-filter
git checkout -b fix/proxy-timeout
```

### 커밋 메시지 규칙 (Conventional Commits)
- 형식: `<type>: <summary>`
- 권장 type: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`

예시:
```text
feat: add number history include-all filter
fix: handle proxy timeout fallback
chore: align gradle jbr toolchain setup
```

### 작업 흐름
1. `main` 최신 반영 후 작업 브랜치 생성
2. 기능 단위로 커밋
3. 원격 브랜치 푸시 후 PR 생성
4. 리뷰 반영 후 `main`으로 머지

기본 명령:
```bash
git checkout main
git pull origin main
git checkout -b feat/your-task

git add .
git commit -m "feat: your change"
git push -u origin feat/your-task
```

### PR 권장 체크리스트
- 변경 목적/범위가 PR 본문에 명확히 정리됨
- UI 변경 시 스크린샷 첨부
- 빌드/실행 확인 완료 (`./gradlew :composeApp:assembleDebug`)
- 관련 이슈/태스크 링크 첨부

## AdMob 설정

현재 **테스트 광고 ID**가 적용되어 있습니다:

### Android
- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner Unit ID: `ca-app-pub-3940256099942544/6300978111`

### iOS
- App ID: `ca-app-pub-3940256099942544~1458002511`

### 프로덕션 배포 시
1. [AdMob 콘솔](https://admob.google.com)에서 앱 등록
2. Android: `AndroidManifest.xml`의 App ID 교체, `AdBanner.android.kt`의 Unit ID 교체
3. iOS: `Info.plist`의 GADApplicationIdentifier 교체, AdBanner 구현 추가

## TODO

### 실제 API 연동
현재 `FakeRemoteDataSource`가 샘플 데이터를 제공합니다.

```kotlin
// data/remote/FakeRemoteDataSource.kt
// TODO: Replace with actual API (e.g., 동행복권 API)
```

동행복권 API 예시:
```
GET https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=1
```

### iOS 광고 구현
현재 iOS에서는 광고 플레이스홀더가 표시됩니다. 실제 구현 시 Google Mobile Ads SDK for iOS 연동 필요.

## 라이선스

Private Project - HSMomo

---

*Generated by Claude Code*
