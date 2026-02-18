package com.hsmomo.lottogen.data.remote

import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.util.SyncLog

class LottoRemoteDataSource(
    private val apiService: LottoApiService
) {
    companion object {
        private const val TAG = "LottoRemoteDataSource"
        private const val INITIAL_SYNC_FALLBACK_RANGE = 200
    }

    /**
     * 모든 회차 데이터 조회 (1회차부터 최신까지)
     */
    suspend fun fetchAllDraws(): Result<List<WinningDraw>> {
        SyncLog.i(TAG, "fetchAllDraws start")
        return apiService.getLatestAvailableDrawNo().fold(
            onSuccess = { latestDrawNo ->
                SyncLog.i(TAG, "fetchAllDraws latestAvailable=$latestDrawNo")
                val fullSync = apiService.fetchDraws(1, latestDrawNo)
                if (fullSync.isSuccess) {
                    SyncLog.i(TAG, "fetchAllDraws full sync success count=${fullSync.getOrNull()?.size ?: 0}")
                    return@fold fullSync
                }
                SyncLog.w(TAG, "fetchAllDraws full sync failed; switching to fallback recent=$INITIAL_SYNC_FALLBACK_RANGE")

                // 첫 동기화 대량 요청이 실패할 경우 최신 구간이라도 확보해 앱 사용성을 유지한다.
                val fallbackStart = maxOf(1, latestDrawNo - INITIAL_SYNC_FALLBACK_RANGE + 1)
                SyncLog.i(TAG, "fetchAllDraws fallback range=$fallbackStart..$latestDrawNo")
                apiService.fetchDraws(fallbackStart, latestDrawNo)
            },
            onFailure = { error ->
                SyncLog.e(TAG, "fetchAllDraws failed while resolving latest", error)
                Result.failure(error)
            }
        )
    }

    /**
     * 특정 회차 이후의 데이터 조회
     */
    suspend fun fetchDrawsAfter(drawNo: Int): Result<List<WinningDraw>> {
        SyncLog.i(TAG, "fetchDrawsAfter start localLatest=$drawNo")
        return apiService.getLatestAvailableDrawNo().fold(
            onSuccess = { latestDrawNo ->
                SyncLog.i(TAG, "fetchDrawsAfter latestAvailable=$latestDrawNo")
                if (drawNo >= latestDrawNo) {
                    SyncLog.i(TAG, "fetchDrawsAfter no update required local=$drawNo latest=$latestDrawNo")
                    return@fold Result.success(emptyList())
                }
                SyncLog.i(TAG, "fetchDrawsAfter fetch range=${drawNo + 1}..$latestDrawNo")
                apiService.fetchDraws(drawNo + 1, latestDrawNo)
            },
            onFailure = { error ->
                SyncLog.e(TAG, "fetchDrawsAfter failed while resolving latest localLatest=$drawNo", error)
                Result.failure(error)
            }
        )
    }

    /**
     * 최신 회차 번호 조회
     */
    suspend fun getLatestDrawNo(): Result<Int> {
        return apiService.getLatestAvailableDrawNo()
    }
}
