package com.hsmomo.lottogen.domain.strategy

import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.domain.model.NumberSet
import kotlin.random.Random

class HighProbabilityStrategy(
    private val repository: LottoRepository
) : NumberGenerationStrategy {

    companion object {
        private const val TOP_K = 15
    }

    override suspend fun generate(count: Int): List<NumberSet> {
        val statistics = repository.getNumberStatistics()

        if (statistics.isEmpty()) {
            return (1..count).map {
                val numbers = (1..45).shuffled(Random).take(6).sorted()
                NumberSet(numbers)
            }
        }

        val topNumbers = statistics
            .sortedByDescending { it.count }
            .take(TOP_K)
            .map { it.number }

        return (1..count).map {
            generateWeightedSet(topNumbers, statistics.associate { it.number to it.count })
        }
    }

    private fun generateWeightedSet(
        priorityNumbers: List<Int>,
        weights: Map<Int, Int>
    ): NumberSet {
        val selected = mutableSetOf<Int>()

        val weightedPool = priorityNumbers.flatMap { num ->
            val weight = weights[num] ?: 1
            List(weight) { num }
        }.shuffled(Random)

        for (num in weightedPool) {
            if (selected.size >= 6) break
            selected.add(num)
        }

        while (selected.size < 6) {
            val remaining = (1..45).filter { it !in selected }
            selected.add(remaining.random(Random))
        }

        return NumberSet(selected.toList().sorted())
    }
}
