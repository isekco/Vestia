package com.isekco.vestia

import androidx.lifecycle.ViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CounterUiState(
    val counter: Int = 0,
    val label: String = "Ready"
)
class CounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())

    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    fun increment() {

        _uiState.update { current ->
            val newCounter = current.counter + 1
            current.copy(
                counter = newCounter,
                label = "Counter: $newCounter"
            )
        }

    }

}
