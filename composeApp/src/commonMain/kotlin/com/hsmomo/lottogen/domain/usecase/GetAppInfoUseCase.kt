package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.domain.model.AppInfo
import kotlinx.datetime.Clock

class GetAppInfoUseCase(
    private val repository: LottoRepository
) {
    suspend operator fun invoke(): AppInfo {
        val localLatestDrawNo = repository.getLatestDrawNo()
        val remoteLatestDrawNoResult = repository.getRemoteLatestDrawNo()
        val lastSyncTime = repository.getLastSyncTime()

        val needsUpdate = if (localLatestDrawNo == null) {
            true
        } else {
            remoteLatestDrawNoResult.getOrNull()?.let { remoteLatestDrawNo ->
                localLatestDrawNo < remoteLatestDrawNo
            } ?: (
                lastSyncTime == null ||
                    (Clock.System.now().toEpochMilliseconds() - lastSyncTime) > 7 * 24 * 60 * 60 * 1000L
                )
        }

        return AppInfo(
            latestDrawNo = localLatestDrawNo,
            lastSyncTime = lastSyncTime,
            needsUpdate = needsUpdate
        )
    }
}
