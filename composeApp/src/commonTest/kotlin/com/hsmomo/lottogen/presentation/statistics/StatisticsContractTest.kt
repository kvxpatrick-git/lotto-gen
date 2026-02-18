package com.hsmomo.lottogen.presentation.statistics

import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsContractTest {

    @Test
    fun normalizedMaxCount_returnsZero_whenMaxIsZero() {
        val state = StatisticsContract.State(maxCount = 0)
        assertEquals(0, state.normalizedMaxCount)
    }

    @Test
    fun normalizedMaxCount_keepsExactMultiple_forStep10() {
        val state = StatisticsContract.State(maxCount = 100)
        assertEquals(100, state.normalizedMaxCount)
    }

    @Test
    fun normalizedMaxCount_roundsUp_forStep10() {
        val state = StatisticsContract.State(maxCount = 101)
        assertEquals(150, state.normalizedMaxCount)
    }

    @Test
    fun normalizedMaxCount_keepsExactMultiple_forStep50() {
        val state = StatisticsContract.State(maxCount = 150)
        assertEquals(150, state.normalizedMaxCount)
    }
}
