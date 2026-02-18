package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.data.repository.LottoRepository

class SyncDrawDataUseCase(
    private val repository: LottoRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.syncDrawData()
    }
}
