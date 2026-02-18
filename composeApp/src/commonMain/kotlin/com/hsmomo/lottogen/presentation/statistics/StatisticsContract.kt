package com.hsmomo.lottogen.presentation.statistics

import com.hsmomo.lottogen.domain.model.NumberStatistics
import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object StatisticsContract {

    data class State(
        val isLoading: Boolean = false,
        val statistics: List<NumberStatistics> = emptyList(),
        val maxCount: Int = 0
    ) : UiState {
        val normalizedMaxCount: Int
            get() {
                if (maxCount == 0) return 0
                val step = if (maxCount > 100) 50 else 10
                return ((maxCount / step) + 1) * step
            }
    }

    sealed class Intent : UiIntent {
        data object LoadStatistics : Intent()
    }

    sealed class Effect : UiSideEffect {
        data class ShowError(val message: String) : Effect()
    }
}