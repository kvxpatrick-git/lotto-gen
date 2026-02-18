package com.hsmomo.lottogen.domain.strategy

import com.hsmomo.lottogen.domain.model.NumberSet

interface NumberGenerationStrategy {
    suspend fun generate(count: Int = 5): List<NumberSet>
}
