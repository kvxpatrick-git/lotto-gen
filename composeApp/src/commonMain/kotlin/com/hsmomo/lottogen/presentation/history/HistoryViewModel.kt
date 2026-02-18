package com.hsmomo.lottogen.presentation.history

import androidx.lifecycle.viewModelScope
import com.hsmomo.lottogen.domain.usecase.GetDrawHistoryUseCase
import com.hsmomo.lottogen.presentation.mvi.MviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val getDrawHistoryUseCase: GetDrawHistoryUseCase
) : MviViewModel<HistoryContract.State, HistoryContract.Intent, HistoryContract.Effect>(
    initialState = HistoryContract.State()
) {

    init {
        sendIntent(HistoryContract.Intent.LoadDraws)
    }

    override suspend fun handleIntent(intent: HistoryContract.Intent) {
        when (intent) {
            is HistoryContract.Intent.LoadDraws -> handleLoadDraws()
            is HistoryContract.Intent.UpdateSearchQuery -> handleUpdateSearchQuery(intent.query)
            is HistoryContract.Intent.Search -> handleSearch()
            is HistoryContract.Intent.ClearSearch -> handleClearSearch()
        }
    }

    private fun handleLoadDraws() {
        viewModelScope.launch {
            reduce { copy(isLoading = true) }
            getDrawHistoryUseCase.getAllDrawsFlow().collectLatest { draws ->
                reduce { copy(isLoading = false, draws = draws) }
            }
        }
    }

    private fun handleUpdateSearchQuery(query: String) {
        reduce { copy(searchQuery = query) }
    }

    private suspend fun handleSearch() {
        val query = currentState.searchQuery.trim()
        if (query.isEmpty()) {
            handleClearSearch()
            return
        }

        val numbers = try {
            query.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .filter { it in 1..45 }
                .distinct()
        } catch (e: NumberFormatException) {
            postSideEffect(HistoryContract.Effect.ShowError("올바른 숫자를 입력해주세요"))
            return
        }

        if (numbers.isEmpty()) {
            postSideEffect(HistoryContract.Effect.ShowError("1~45 사이의 숫자를 입력해주세요"))
            return
        }

        reduce { copy(isLoading = true, searchNumbers = numbers) }

        val filteredDraws = getDrawHistoryUseCase.searchByNumbers(numbers)

        reduce {
            copy(isLoading = false, filteredDraws = filteredDraws, isSearchActive = true)
        }
    }

    private fun handleClearSearch() {
        reduce {
            copy(searchQuery = "", searchNumbers = emptyList(), filteredDraws = emptyList(), isSearchActive = false)
        }
    }
}