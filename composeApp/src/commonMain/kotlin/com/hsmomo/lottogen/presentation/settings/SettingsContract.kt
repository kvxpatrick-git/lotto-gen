package com.hsmomo.lottogen.presentation.settings

import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object SettingsContract {

    data class State(
        val isLoading: Boolean = false,
        val latestDrawNo: Int? = null,
        val needsUpdate: Boolean = false,
        val appVersion: String = ""
    ) : UiState {
        val updateStatusText: String
            get() = if (latestDrawNo != null) {
                if (needsUpdate) {
                    "최신 회차 ${latestDrawNo}회차까지 업데이트됨 / 업데이트 필요"
                } else {
                    "최신 회차 ${latestDrawNo}회차까지 업데이트됨"
                }
            } else {
                "데이터 없음 / 업데이트 필요"
            }
    }

    sealed class Intent : UiIntent {
        data object LoadSettings : Intent()
    }

    sealed class Effect : UiSideEffect
}