package com.hsmomo.lottogen.data.repository

import com.hsmomo.lottogen.domain.model.Bookmark
import com.hsmomo.lottogen.domain.model.NumberStatistics
import com.hsmomo.lottogen.domain.model.WinningDraw
import kotlinx.coroutines.flow.Flow

interface LottoRepository {
    // Winning Draw
    fun getAllDrawsFlow(): Flow<List<WinningDraw>>
    suspend fun getAllDraws(): List<WinningDraw>
    suspend fun syncDrawData(): Result<Int>
    suspend fun getLatestDrawNo(): Int?
    suspend fun searchDrawsByNumbers(numbers: List<Int>): List<WinningDraw>

    // Statistics
    suspend fun getNumberStatistics(): List<NumberStatistics>

    // Bookmark
    fun getAllBookmarksFlow(): Flow<List<Bookmark>>
    suspend fun getAllBookmarks(): List<Bookmark>
    suspend fun addBookmark(numbers: List<Int>): Boolean
    suspend fun removeBookmark(numbers: List<Int>)
    suspend fun isBookmarked(numbers: List<Int>): Boolean
    fun isBookmarkedFlow(numbers: List<Int>): Flow<Boolean>

    // Meta
    suspend fun getLastSyncTime(): Long?
}
