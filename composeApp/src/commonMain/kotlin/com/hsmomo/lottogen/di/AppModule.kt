package com.hsmomo.lottogen.di

import com.hsmomo.lottogen.data.remote.LottoApiService
import com.hsmomo.lottogen.data.remote.LottoRemoteDataSource
import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.data.repository.LottoRepositoryImpl
import com.hsmomo.lottogen.domain.strategy.HighProbabilityStrategy
import com.hsmomo.lottogen.domain.strategy.LowProbabilityStrategy
import com.hsmomo.lottogen.domain.strategy.MixedStrategy
import com.hsmomo.lottogen.domain.strategy.RandomStrategy
import com.hsmomo.lottogen.domain.usecase.BookmarkUseCase
import com.hsmomo.lottogen.domain.usecase.GenerateNumbersUseCase
import com.hsmomo.lottogen.domain.usecase.GetAppInfoUseCase
import com.hsmomo.lottogen.domain.usecase.GetDrawHistoryUseCase
import com.hsmomo.lottogen.domain.usecase.GetStatisticsUseCase
import com.hsmomo.lottogen.domain.usecase.SyncDrawDataUseCase
import com.hsmomo.lottogen.presentation.bookmark.BookmarkViewModel
import com.hsmomo.lottogen.presentation.generate.GenerateViewModel
import com.hsmomo.lottogen.presentation.history.HistoryViewModel
import com.hsmomo.lottogen.presentation.settings.SettingsViewModel
import com.hsmomo.lottogen.presentation.splash.SplashViewModel
import com.hsmomo.lottogen.presentation.statistics.StatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Network
    single { createHttpClient() }
    single(named("lottoProxyBaseUrl")) { proxyBaseUrl() }
    single { LottoApiService(get(), get(named("lottoProxyBaseUrl"))) }
    single { LottoRemoteDataSource(get()) }

    // Repository
    single<LottoRepository> { LottoRepositoryImpl(get(), get()) }

    // Strategies
    factory { RandomStrategy() }
    factory { HighProbabilityStrategy(get()) }
    factory { LowProbabilityStrategy(get()) }
    factory { MixedStrategy() }

    // Use Cases
    factory { SyncDrawDataUseCase(get()) }
    factory { GetDrawHistoryUseCase(get()) }
    factory { GetStatisticsUseCase(get()) }
    factory { BookmarkUseCase(get()) }
    factory { GenerateNumbersUseCase(get(), get(), get(), get()) }
    factory { GetAppInfoUseCase(get()) }

    // ViewModels
    viewModel { SplashViewModel(get()) }
    viewModel { GenerateViewModel(get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { BookmarkViewModel(get()) }
    viewModel { SettingsViewModel(get(), "1.0.0") }
}
