package com.hsmomo.lottogen.data.remote

import com.hsmomo.lottogen.domain.model.WinningDraw
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.random.Random

/**
 * Fake Remote Data Source for development/testing.
 * Replace with actual API implementation when ready.
 *
 * TODO: Replace with actual API (e.g., 동행복권 API) when available
 */
class FakeRemoteDataSource {

    companion object {
        private const val TOTAL_DRAWS = 200
        private const val SIMULATE_FAILURE_RATE = 0.1 // 10% failure rate for testing
        private const val NETWORK_DELAY_MS = 500L
    }

    private val fakeData: List<WinningDraw> by lazy {
        generateFakeDrawData()
    }

    /**
     * Simulates fetching all draw data from remote server
     */
    suspend fun fetchAllDraws(): Result<List<WinningDraw>> {
        delay(NETWORK_DELAY_MS)

        if (Random.nextDouble() < SIMULATE_FAILURE_RATE) {
            return Result.failure(Exception("Network error: Failed to fetch data"))
        }

        return Result.success(fakeData)
    }

    /**
     * Simulates fetching draws after a specific draw number
     */
    suspend fun fetchDrawsAfter(drawNo: Int): Result<List<WinningDraw>> {
        delay(NETWORK_DELAY_MS)

        if (Random.nextDouble() < SIMULATE_FAILURE_RATE) {
            return Result.failure(Exception("Network error: Failed to fetch data"))
        }

        val newDraws = fakeData.filter { it.drawNo > drawNo }
        return Result.success(newDraws)
    }

    /**
     * Get the latest draw number available
     */
    suspend fun getLatestDrawNo(): Result<Int> {
        delay(100)
        return Result.success(TOTAL_DRAWS)
    }

    private fun generateFakeDrawData(): List<WinningDraw> {
        val draws = mutableListOf<WinningDraw>()
        var currentDate = LocalDate(2002, 12, 7) // First Lotto draw date in Korea

        repeat(TOTAL_DRAWS) { index ->
            val drawNo = index + 1
            val numbers = generateUniqueNumbers(6, 1, 45).sorted()
            val bonus = generateUniqueNumbers(1, 1, 45, numbers).first()

            draws.add(
                WinningDraw(
                    drawNo = drawNo,
                    drawDate = "${currentDate.year}-${currentDate.monthNumber.toString().padStart(2, '0')}-${currentDate.dayOfMonth.toString().padStart(2, '0')}",
                    numbers = numbers,
                    bonus = bonus,
                    firstPrizeAmount = Random.nextLong(1_000_000_000L, 30_000_000_000L)
                )
            )

            currentDate = currentDate.plus(DatePeriod(days = 7))
        }

        return draws
    }

    private fun generateUniqueNumbers(
        count: Int,
        min: Int,
        max: Int,
        exclude: List<Int> = emptyList()
    ): List<Int> {
        val available = (min..max).filter { it !in exclude }.toMutableList()
        val result = mutableListOf<Int>()

        repeat(count) {
            val index = Random.nextInt(available.size)
            result.add(available.removeAt(index))
        }

        return result
    }
}
