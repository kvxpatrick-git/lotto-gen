package com.hsmomo.lottogen.presentation.generate

import com.hsmomo.lottogen.domain.model.NumberSet
import com.hsmomo.lottogen.domain.usecase.GenerationType
import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object GenerateContract {

    data class State(
        val isLoading: Boolean = false,
        val generatedSets: List<NumberSetWithBookmark> = emptyList(),
        val selectedNumbers: Set<Int> = emptySet(),
        val isMixedMode: Boolean = false,
        val currentGenerationType: GenerationType? = null
    ) : UiState {
        val canGenerateMixed: Boolean
            get() = selectedNumbers.isNotEmpty() && selectedNumbers.size <= 6
    }

    data class NumberSetWithBookmark(
        val numberSet: NumberSet,
        val isBookmarked: Boolean
    )

    sealed class Intent : UiIntent {
        data class Generate(val type: GenerationType) : Intent()
        data object EnterMixedMode : Intent()
        data object ExitMixedMode : Intent()
        data class ToggleNumber(val number: Int) : Intent()
        data object ResetSelection : Intent()
        data object GenerateMixed : Intent()
        data class ToggleBookmark(val numberSet: NumberSet) : Intent()
    }

    sealed class Effect : UiSideEffect {
        data class ShowMessage(val message: String) : Effect()
    }
}