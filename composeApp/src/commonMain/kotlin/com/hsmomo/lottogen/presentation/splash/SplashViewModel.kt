package com.hsmomo.lottogen.presentation.splash

import androidx.lifecycle.viewModelScope
import com.hsmomo.lottogen.data.remote.ApiResponseSyncException
import com.hsmomo.lottogen.data.remote.LatestDrawResolutionException
import com.hsmomo.lottogen.data.remote.NetworkSyncException
import com.hsmomo.lottogen.domain.usecase.SyncDrawDataUseCase
import com.hsmomo.lottogen.presentation.mvi.MviViewModel
import com.hsmomo.lottogen.util.SyncLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val syncDrawDataUseCase: SyncDrawDataUseCase
) : MviViewModel<SplashContract.State, SplashContract.Intent, SplashContract.Effect>(
    initialState = SplashContract.State()
) {

    companion object {
        private const val TAG = "SplashViewModel"
        private const val MIN_SPLASH_DURATION_MS = 3000L
        private const val FADE_IN_DURATION_MS = 500L
    }

    init {
        sendIntent(SplashContract.Intent.StartSync)
    }

    override suspend fun handleIntent(intent: SplashContract.Intent) {
        when (intent) {
            is SplashContract.Intent.StartSync -> handleStartSync()
            is SplashContract.Intent.MinTimeElapsed -> handleMinTimeElapsed()
            is SplashContract.Intent.CheckNavigation -> checkAndNavigate()
        }
    }

    private fun handleStartSync() {
        SyncLog.i(TAG, "handleStartSync launched")
        viewModelScope.launch {
            animateFadeIn()
        }

        viewModelScope.launch {
            delay(MIN_SPLASH_DURATION_MS)
            sendIntent(SplashContract.Intent.MinTimeElapsed)
        }

        viewModelScope.launch {
            syncDrawDataUseCase().fold(
                onSuccess = {
                    SyncLog.i(TAG, "sync success")
                    reduce { copy(isSyncComplete = true, isLoading = false) }
                    sendIntent(SplashContract.Intent.CheckNavigation)
                },
                onFailure = { error ->
                    SyncLog.e(TAG, "sync failed", error)
                    reduce { copy(isSyncFailed = true, isLoading = false) }
                    postSideEffect(
                        SplashContract.Effect.ShowError(
                            getSyncErrorMessage(error)
                        )
                    )
                    sendIntent(SplashContract.Intent.CheckNavigation)
                }
            )
        }
    }

    private suspend fun animateFadeIn() {
        val steps = 10
        val stepDuration = FADE_IN_DURATION_MS / steps

        for (i in 1..steps) {
            delay(stepDuration)
            reduce { copy(logoAlpha = i.toFloat() / steps) }
        }
    }

    private fun handleMinTimeElapsed() {
        SyncLog.d(TAG, "minimum splash time elapsed")
        reduce { copy(isMinTimeElapsed = true) }
        sendIntent(SplashContract.Intent.CheckNavigation)
    }

    private fun checkAndNavigate() {
        SyncLog.d(TAG, "checkAndNavigate canNavigate=${currentState.canNavigate}")
        if (currentState.canNavigate) {
            SyncLog.i(TAG, "navigate to home")
            postSideEffect(SplashContract.Effect.NavigateToHome)
        }
    }

    private fun getSyncErrorMessage(error: Throwable): String {
        return when (error) {
            is NetworkSyncException ->
                "네트워크 오류로 최신 데이터 업데이트에 실패했습니다. 저장된 데이터를 표시합니다."
            is ApiResponseSyncException ->
                "서버 응답 처리 중 오류가 발생했습니다. 저장된 데이터를 표시합니다."
            is LatestDrawResolutionException ->
                "최신 회차 확인에 실패했습니다. 저장된 데이터를 표시합니다."
            else ->
                "최신 데이터 업데이트 실패, 저장된 데이터를 표시"
        }
    }
}
