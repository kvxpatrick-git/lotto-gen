package com.hsmomo.lottogen.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<State : UiState, Intent : UiIntent, Effect : UiSideEffect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected val currentState: State
        get() = _state.value

    private val _intent = MutableSharedFlow<Intent>()

    private val _sideEffect = Channel<Effect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            _intent.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    protected abstract suspend fun handleIntent(intent: Intent)

    protected fun reduce(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    protected fun postSideEffect(effect: Effect) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }
}
