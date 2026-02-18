package com.hsmomo.lottogen.presentation.bookmark

import androidx.lifecycle.viewModelScope
import com.hsmomo.lottogen.domain.model.Bookmark
import com.hsmomo.lottogen.domain.usecase.BookmarkUseCase
import com.hsmomo.lottogen.presentation.mvi.MviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val bookmarkUseCase: BookmarkUseCase
) : MviViewModel<BookmarkContract.State, BookmarkContract.Intent, BookmarkContract.Effect>(
    initialState = BookmarkContract.State()
) {

    init {
        sendIntent(BookmarkContract.Intent.LoadBookmarks)
    }

    override suspend fun handleIntent(intent: BookmarkContract.Intent) {
        when (intent) {
            is BookmarkContract.Intent.LoadBookmarks -> handleLoadBookmarks()
            is BookmarkContract.Intent.RemoveBookmark -> handleRemoveBookmark(intent.bookmark)
        }
    }

    private fun handleLoadBookmarks() {
        viewModelScope.launch {
            reduce { copy(isLoading = true) }
            bookmarkUseCase.getAllBookmarksFlow().collectLatest { bookmarks ->
                reduce { copy(isLoading = false, bookmarks = bookmarks) }
            }
        }
    }

    private suspend fun handleRemoveBookmark(bookmark: Bookmark) {
        bookmarkUseCase.removeBookmark(bookmark.numbers)
        postSideEffect(BookmarkContract.Effect.ShowMessage("북마크에서 삭제되었습니다"))
    }
}