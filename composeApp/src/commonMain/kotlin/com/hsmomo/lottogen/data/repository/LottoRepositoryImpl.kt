package com.hsmomo.lottogen.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hsmomo.lottogen.data.remote.LottoSyncException
import com.hsmomo.lottogen.data.remote.LottoRemoteDataSource
import com.hsmomo.lottogen.data.remote.NetworkSyncException
import com.hsmomo.lottogen.db.LottoDatabase
import com.hsmomo.lottogen.domain.model.Bookmark
import com.hsmomo.lottogen.domain.model.NumberStatistics
import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.util.SyncLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class LottoRepositoryImpl(
    private val database: LottoDatabase,
    private val remoteDataSource: LottoRemoteDataSource
) : LottoRepository {
    private val queries get() = database.lottoDatabaseQueries

    // Winning Draw
    override fun getAllDrawsFlow(): Flow<List<WinningDraw>> {
        return queries.selectAllDraws()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { draws -> draws.map { it.toDomain() } }
    }

    override suspend fun getAllDraws(): List<WinningDraw> = withContext(Dispatchers.IO) {
        queries.selectAllDraws().executeAsList().map { it.toDomain() }
    }

    override suspend fun syncDrawData(): Result<Int> = withContext(Dispatchers.IO) {
        val localLatest = queries.selectLatestDrawNo().executeAsOneOrNull()?.MAX?.toInt() ?: 0
        SyncLog.i(TAG, "syncDrawData start localLatest=$localLatest mode=${if (localLatest == 0) "initial" else "incremental"}")

        val result = retrySyncFetch {
            if (localLatest == 0) {
                remoteDataSource.fetchAllDraws()
            } else {
                remoteDataSource.fetchDrawsAfter(localLatest)
            }
        }

        result.fold(
            onSuccess = { draws ->
                SyncLog.i(TAG, "syncDrawData fetch success drawCount=${draws.size}")
                draws.forEach { draw ->
                    queries.insertDraw(
                        drawNo = draw.drawNo.toLong(),
                        drawDate = draw.drawDate,
                        n1 = draw.numbers[0].toLong(),
                        n2 = draw.numbers[1].toLong(),
                        n3 = draw.numbers[2].toLong(),
                        n4 = draw.numbers[3].toLong(),
                        n5 = draw.numbers[4].toLong(),
                        n6 = draw.numbers[5].toLong(),
                        bonus = draw.bonus.toLong(),
                        firstPrizeAmount = draw.firstPrizeAmount,
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                }
                if (draws.isNotEmpty()) {
                    val newLatest = draws.maxOf { it.drawNo }
                    queries.insertMeta(KEY_LATEST_DRAW_NO, newLatest.toString())
                    queries.insertMeta(KEY_LAST_SYNC_AT, Clock.System.now().toEpochMilliseconds().toString())
                    SyncLog.i(TAG, "syncDrawData persisted drawCount=${draws.size} newLatest=$newLatest")
                } else {
                    SyncLog.i(TAG, "syncDrawData no new draws (already up to date)")
                }
                Result.success(draws.size)
            },
            onFailure = { error ->
                SyncLog.e(TAG, "syncDrawData failed after retries localLatest=$localLatest", error)
                Result.failure(error)
            }
        )
    }

    override suspend fun getLatestDrawNo(): Int? = withContext(Dispatchers.IO) {
        queries.selectLatestDrawNo().executeAsOneOrNull()?.MAX?.toInt()
    }

    override suspend fun getRemoteLatestDrawNo(): Result<Int> = withContext(Dispatchers.IO) {
        remoteDataSource.getLatestDrawNo()
    }

    override suspend fun searchDrawsByNumbers(numbers: List<Int>): List<WinningDraw> = withContext(Dispatchers.IO) {
        if (numbers.isEmpty()) {
            return@withContext getAllDraws()
        }

        val allDraws = queries.selectAllDraws().executeAsList()
        allDraws.filter { draw ->
            val drawNumbers = listOf(
                draw.n1.toInt(), draw.n2.toInt(), draw.n3.toInt(),
                draw.n4.toInt(), draw.n5.toInt(), draw.n6.toInt()
            )
            numbers.all { it in drawNumbers }
        }.map { it.toDomain() }
    }

    // Statistics
    override suspend fun getNumberStatistics(): List<NumberStatistics> = withContext(Dispatchers.IO) {
        val draws = queries.selectAllDraws().executeAsList()
        val countMap = mutableMapOf<Int, Int>()

        (1..45).forEach { countMap[it] = 0 }

        draws.forEach { draw ->
            listOf(draw.n1, draw.n2, draw.n3, draw.n4, draw.n5, draw.n6).forEach { num ->
                countMap[num.toInt()] = (countMap[num.toInt()] ?: 0) + 1
            }
        }

        countMap.map { (number, count) ->
            NumberStatistics(number = number, count = count)
        }.sortedBy { it.number }
    }

    // Bookmark
    override fun getAllBookmarksFlow(): Flow<List<Bookmark>> {
        return queries.selectAllBookmarks()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { bookmarks -> bookmarks.map { it.toDomain() } }
    }

    override suspend fun getAllBookmarks(): List<Bookmark> = withContext(Dispatchers.IO) {
        queries.selectAllBookmarks().executeAsList().map { it.toDomain() }
    }

    override suspend fun addBookmark(numbers: List<Int>): Boolean = withContext(Dispatchers.IO) {
        val sortedText = numbers.sorted().joinToString(",")
        queries.insertBookmark(sortedText, Clock.System.now().toEpochMilliseconds())
        true
    }

    override suspend fun removeBookmark(numbers: List<Int>) = withContext(Dispatchers.IO) {
        val sortedText = numbers.sorted().joinToString(",")
        queries.deleteBookmarkByNumbers(sortedText)
    }

    override suspend fun isBookmarked(numbers: List<Int>): Boolean = withContext(Dispatchers.IO) {
        val sortedText = numbers.sorted().joinToString(",")
        queries.selectBookmarkByNumbers(sortedText).executeAsOneOrNull() != null
    }

    override fun isBookmarkedFlow(numbers: List<Int>): Flow<Boolean> {
        val sortedText = numbers.sorted().joinToString(",")
        return queries.selectBookmarkByNumbers(sortedText)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.isNotEmpty() }
    }

    // Meta
    override suspend fun getLastSyncTime(): Long? = withContext(Dispatchers.IO) {
        queries.selectMetaValue(KEY_LAST_SYNC_AT).executeAsOneOrNull()?.toLongOrNull()
    }

    // Mappers
    private fun com.hsmomo.lottogen.db.WinningDraw.toDomain(): WinningDraw {
        return WinningDraw(
            drawNo = drawNo.toInt(),
            drawDate = drawDate,
            numbers = listOf(n1.toInt(), n2.toInt(), n3.toInt(), n4.toInt(), n5.toInt(), n6.toInt()),
            bonus = bonus.toInt(),
            firstPrizeAmount = firstPrizeAmount
        )
    }

    private fun com.hsmomo.lottogen.db.Bookmark.toDomain(): Bookmark {
        return Bookmark(
            id = id,
            numbers = numbersSortedText.split(",").map { it.toInt() },
            createdAt = createdAt
        )
    }

    companion object {
        private const val TAG = "LottoRepository"
        private const val KEY_LATEST_DRAW_NO = "latest_draw_no"
        private const val KEY_LAST_SYNC_AT = "last_sync_at"
        private const val MAX_SYNC_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 500L
    }

    private suspend fun retrySyncFetch(
        block: suspend () -> Result<List<WinningDraw>>
    ): Result<List<WinningDraw>> {
        var retryDelayMs = INITIAL_RETRY_DELAY_MS
        var lastResult: Result<List<WinningDraw>> = Result.failure(
            LottoSyncException("Sync failed before execution")
        )

        repeat(MAX_SYNC_RETRIES) { attempt ->
            val tryNo = attempt + 1
            SyncLog.i(TAG, "retrySyncFetch attempt=$tryNo/$MAX_SYNC_RETRIES")
            val result = block()
            if (result.isSuccess) {
                SyncLog.i(TAG, "retrySyncFetch success attempt=$tryNo")
                return result
            }

            lastResult = result
            val error = result.exceptionOrNull()
            val canRetry = error is NetworkSyncException && attempt < MAX_SYNC_RETRIES - 1
            SyncLog.w(
                TAG,
                "retrySyncFetch failed attempt=$tryNo retryable=$canRetry type=${error?.let { it::class.simpleName }} msg=${error?.message}"
            )
            if (!canRetry) {
                return result
            }

            SyncLog.i(TAG, "retrySyncFetch backoff delayMs=$retryDelayMs")
            delay(retryDelayMs)
            retryDelayMs *= 2
        }

        return lastResult
    }
}
