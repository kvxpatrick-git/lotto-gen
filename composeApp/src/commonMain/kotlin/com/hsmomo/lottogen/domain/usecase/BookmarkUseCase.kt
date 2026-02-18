package com.hsmomo.lottogen.domain.usecase

import com.hsmomo.lottogen.data.repository.LottoRepository
import com.hsmomo.lottogen.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

class BookmarkUseCase(
    private val repository: LottoRepository
) {
    fun getAllBookmarksFlow(): Flow<List<Bookmark>> {
        return repository.getAllBookmarksFlow()
    }

    suspend fun toggleBookmark(numbers: List<Int>): Boolean {
        val isCurrentlyBookmarked = repository.isBookmarked(numbers)
        return if (isCurrentlyBookmarked) {
            repository.removeBookmark(numbers)
            false
        } else {
            repository.addBookmark(numbers)
            true
        }
    }

    suspend fun addBookmark(numbers: List<Int>): Boolean {
        return repository.addBookmark(numbers)
    }

    suspend fun removeBookmark(numbers: List<Int>) {
        repository.removeBookmark(numbers)
    }

    suspend fun isBookmarked(numbers: List<Int>): Boolean {
        return repository.isBookmarked(numbers)
    }

    fun isBookmarkedFlow(numbers: List<Int>): Flow<Boolean> {
        return repository.isBookmarkedFlow(numbers)
    }
}
