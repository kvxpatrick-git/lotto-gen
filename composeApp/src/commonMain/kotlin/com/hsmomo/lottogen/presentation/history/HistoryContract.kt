package com.hsmomo.lottogen.presentation.history

import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object HistoryContract {

    data class State(
        val isLoading: Boolean = false,
        val draws: List<WinningDraw> = emptyList(),
        val filteredDraws: List<WinningDraw> = emptyList(),
        val searchQuery: String = "",
        val searchNumbers: List<Int> = emptyList(),
        val isSearchActive: Boolean = false
    ) : UiState {
        val displayDraws: List<WinningDraw>
            get() = if (isSearchActive) filteredDraws else draws
    }

    sealed class Intent : UiIntent {
        data object LoadDraws : Intent()
        data class UpdateSearchQuery(val query: String) : Intent()
        data object Search : Intent()
        data object ClearSearch : Intent()
    }

    sealed class Effect : UiSideEffect {
        data class ShowError(val message: String) : Effect()
    }
}