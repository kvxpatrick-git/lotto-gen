package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.domain.model.WinningDraw
import kotlinx.coroutines.flow.Flow

class GetDrawHistoryUseCase(
    private val repository: LottoRepository
) {
    fun getAllDrawsFlow(): Flow<List<WinningDraw>> {
        return repository.getAllDrawsFlow()
    }

    suspend fun searchByNumbers(numbers: List<Int>): List<WinningDraw> {
        return repository.searchDrawsByNumbers(numbers)
    }
}
