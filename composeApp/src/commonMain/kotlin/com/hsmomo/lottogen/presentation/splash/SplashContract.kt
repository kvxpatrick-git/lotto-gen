package com.hsmomo.lottogen.presentation.splash

import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object SplashContract {

    data class State(
        val isLoading: Boolean = true,
        val isSyncComplete: Boolean = false,
        val isSyncFailed: Boolean = false,
        val isMinTimeElapsed: Boolean = false,
        val logoAlpha: Float = 0f
    ) : UiState {
        val canNavigate: Boolean
            get() = isMinTimeElapsed && (isSyncComplete || isSyncFailed)
    }

    sealed class Intent : UiIntent {
        data object StartSync : Intent()
        data object MinTimeElapsed : Intent()
        data object CheckNavigation : Intent()
    }

    sealed class Effect : UiSideEffect {
        data object NavigateToHome : Effect()
        data class ShowError(val message: String) : Effect()
    }
}