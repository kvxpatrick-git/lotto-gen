package com.hsmomo.lottogen.presentation.statistics

import com.hsmomo.lottogen.domain.usecase.GetStatisticsUseCase
import com.hsmomo.lottogen.presentation.mvi.MviViewModel

class StatisticsViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : MviViewModel<StatisticsContract.State, StatisticsContract.Intent, StatisticsContract.Effect>(
    initialState = StatisticsContract.State()
) {

    init {
        sendIntent(StatisticsContract.Intent.LoadStatistics)
    }

    override suspend fun handleIntent(intent: StatisticsContract.Intent) {
        when (intent) {
            is StatisticsContract.Intent.LoadStatistics -> handleLoadStatistics()
        }
    }

    private suspend fun handleLoadStatistics() {
        reduce { copy(isLoading = true) }

        try {
            val statistics = getStatisticsUseCase()
            val maxCount = statistics.maxOfOrNull { it.count } ?: 0

            reduce {
                copy(isLoading = false, statistics = statistics, maxCount = maxCount)
            }
        } catch (e: Exception) {
            reduce { copy(isLoading = false) }
            postSideEffect(StatisticsContract.Effect.ShowError("통계 로딩 실패"))
        }
    }
}