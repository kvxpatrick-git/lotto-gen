package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.domain.model.NumberStatistics

class GetStatisticsUseCase(
    private val repository: LottoRepository
) {
    suspend operator fun invoke(): List<NumberStatistics> {
        return repository.getNumberStatistics()
    }
}
