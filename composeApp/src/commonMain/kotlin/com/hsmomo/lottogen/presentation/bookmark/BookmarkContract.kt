package com.hsmomo.lottogen.presentation.bookmark

import com.hsmomo.lottogen.domain.model.Bookmark
import com.hsmomo.lottogen.presentation.mvi.UiIntent
import com.hsmomo.lottogen.presentation.mvi.UiSideEffect
import com.hsmomo.lottogen.presentation.mvi.UiState

object BookmarkContract {

    data class State(
        val isLoading: Boolean = false,
        val bookmarks: List<Bookmark> = emptyList()
    ) : UiState

    sealed class Intent : UiIntent {
        data object LoadBookmarks : Intent()
        data class RemoveBookmark(val bookmark: Bookmark) : Intent()
    }

    sealed class Effect : UiSideEffect {
        data class ShowMessage(val message: String) : Effect()
    }
}