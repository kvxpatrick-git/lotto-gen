package com.hsmomo.lottogen.presentation.generate

import androidx.lifecycle.viewModelScope
import com.hsmomo.lottogen.domain.model.NumberSet
import com.hsmomo.lottogen.domain.usecase.BookmarkUseCase
import com.hsmomo.lottogen.domain.usecase.GenerateNumbersUseCase
import com.hsmomo.lottogen.domain.usecase.GenerationType
import com.hsmomo.lottogen.presentation.mvi.MviViewModel
import kotlinx.coroutines.launch

class GenerateViewModel(
    private val generateNumbersUseCase: GenerateNumbersUseCase,
    private val bookmarkUseCase: BookmarkUseCase
) : MviViewModel<GenerateContract.State, GenerateContract.Intent, GenerateContract.Effect>(
    initialState = GenerateContract.State()
) {

    override suspend fun handleIntent(intent: GenerateContract.Intent) {
        when (intent) {
            is GenerateContract.Intent.Generate -> handleGenerate(intent.type)
            is GenerateContract.Intent.EnterMixedMode -> handleEnterMixedMode()
            is GenerateContract.Intent.ExitMixedMode -> handleExitMixedMode()
            is GenerateContract.Intent.ToggleNumber -> handleToggleNumber(intent.number)
            is GenerateContract.Intent.ResetSelection -> handleResetSelection()
            is GenerateContract.Intent.GenerateMixed -> handleGenerateMixed()
            is GenerateContract.Intent.ToggleBookmark -> handleToggleBookmark(intent.numberSet)
        }
    }

    private suspend fun handleGenerate(type: GenerationType) {
        reduce { copy(isLoading = true, isMixedMode = false, currentGenerationType = type) }

        val numberSets = generateNumbersUseCase.generate(type, 5)
        val setsWithBookmark = numberSets.map { numberSet ->
            GenerateContract.NumberSetWithBookmark(
                numberSet = numberSet,
                isBookmarked = bookmarkUseCase.isBookmarked(numberSet.numbers)
            )
        }

        reduce {
            copy(
                isLoading = false,
                generatedSets = setsWithBookmark,
                selectedNumbers = emptySet()
            )
        }
    }

    private fun handleEnterMixedMode() {
        reduce {
            copy(
                isMixedMode = true,
                generatedSets = emptyList(),
                selectedNumbers = emptySet(),
                currentGenerationType = GenerationType.MIXED
            )
        }
    }

    private fun handleExitMixedMode() {
        reduce { copy(isMixedMode = false, selectedNumbers = emptySet()) }
    }

    private fun handleToggleNumber(number: Int) {
        val currentSelected = currentState.selectedNumbers
        val newSelected = if (number in currentSelected) {
            currentSelected - number
        } else {
            if (currentSelected.size >= 6) {
                postSideEffect(GenerateContract.Effect.ShowMessage("최대 6개까지 선택할 수 있습니다"))
                return
            }
            currentSelected + number
        }
        reduce { copy(selectedNumbers = newSelected) }
    }

    private fun handleResetSelection() {
        reduce { copy(selectedNumbers = emptySet(), generatedSets = emptyList()) }
    }

    private suspend fun handleGenerateMixed() {
        val selected = currentState.selectedNumbers.toList()
        if (selected.isEmpty()) {
            postSideEffect(GenerateContract.Effect.ShowMessage("번호를 선택해주세요"))
            return
        }

        reduce { copy(isLoading = true) }

        val numberSet = generateNumbersUseCase.fillRemaining(selected)
        val isBookmarked = bookmarkUseCase.isBookmarked(numberSet.numbers)

        val newSet = GenerateContract.NumberSetWithBookmark(
            numberSet = numberSet,
            isBookmarked = isBookmarked
        )

        reduce { copy(isLoading = false, generatedSets = generatedSets + newSet) }
    }

    private fun handleToggleBookmark(numberSet: NumberSet) {
        viewModelScope.launch {
            val isNowBookmarked = bookmarkUseCase.toggleBookmark(numberSet.numbers)

            val updatedSets = currentState.generatedSets.map { setWithBookmark ->
                if (setWithBookmark.numberSet.numbersSortedText == numberSet.numbersSortedText) {
                    setWithBookmark.copy(isBookmarked = isNowBookmarked)
                } else {
                    setWithBookmark
                }
            }

            reduce { copy(generatedSets = updatedSets) }

            val message = if (isNowBookmarked) "북마크에 추가되었습니다" else "북마크에서 삭제되었습니다"
            postSideEffect(GenerateContract.Effect.ShowMessage(message))
        }
    }
}