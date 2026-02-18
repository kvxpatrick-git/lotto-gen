package com.hsmomo.lottogen.presentation.settings

import com.hsmomo.lottogen.domain.usecase.GetAppInfoUseCase
import com.hsmomo.lottogen.presentation.mvi.MviViewModel

class SettingsViewModel(
    private val getAppInfoUseCase: GetAppInfoUseCase,
    private val appVersion: String
) : MviViewModel<SettingsContract.State, SettingsContract.Intent, SettingsContract.Effect>(
    initialState = SettingsContract.State(appVersion = appVersion)
) {

    init {
        sendIntent(SettingsContract.Intent.LoadSettings)
    }

    override suspend fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.LoadSettings -> handleLoadSettings()
        }
    }

    private suspend fun handleLoadSettings() {
        reduce { copy(isLoading = true) }

        try {
            val appInfo = getAppInfoUseCase()

            reduce {
                copy(
                    isLoading = false,
                    latestDrawNo = appInfo.latestDrawNo,
                    needsUpdate = appInfo.needsUpdate,
                    appVersion = appVersion
                )
            }
        } catch (e: Exception) {
            reduce { copy(isLoading = false, appVersion = appVersion) }
        }
    }
}