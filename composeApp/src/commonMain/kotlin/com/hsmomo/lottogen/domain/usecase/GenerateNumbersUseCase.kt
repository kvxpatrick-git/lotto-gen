package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.domain.model.NumberSet
import com.hsmomo.lottogen.domain.strategy.HighProbabilityStrategy
import com.hsmomo.lottogen.domain.strategy.LowProbabilityStrategy
import com.hsmomo.lottogen.domain.strategy.MixedStrategy
import com.hsmomo.lottogen.domain.strategy.RandomStrategy

enum class GenerationType {
    RECOMMENDED,
    HIGH_PROBABILITY,
    LOW_PROBABILITY,
    MIXED
}

class GenerateNumbersUseCase(
    private val randomStrategy: RandomStrategy,
    private val highProbabilityStrategy: HighProbabilityStrategy,
    private val lowProbabilityStrategy: LowProbabilityStrategy,
    private val mixedStrategy: MixedStrategy
) {
    suspend fun generate(type: GenerationType, count: Int = 5): List<NumberSet> {
        return when (type) {
            GenerationType.RECOMMENDED -> randomStrategy.generate(count)
            GenerationType.HIGH_PROBABILITY -> highProbabilityStrategy.generate(count)
            GenerationType.LOW_PROBABILITY -> lowProbabilityStrategy.generate(count)
            GenerationType.MIXED -> mixedStrategy.generate(count)
        }
    }

    suspend fun generateMixed(selectedNumbers: List<Int>): NumberSet {
        mixedStrategy.setSelectedNumbers(selectedNumbers)
        return mixedStrategy.generateSingle()
    }

    fun fillRemaining(selectedNumbers: List<Int>): NumberSet {
        return randomStrategy.fillRemaining(selectedNumbers)
    }
}
