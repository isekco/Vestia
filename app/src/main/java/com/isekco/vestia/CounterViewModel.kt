package com.isekco.vestia

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.isekco.vestia.UiEvent
import android.util.Log

data class CounterUiState(
    val counter: Int = 0,
    val label: String = "Ready"
)
class CounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())

    private val _uiEvent = MutableSharedFlow<UiEvent>()

    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    val uiEvent = _uiEvent.asSharedFlow()

    fun increment() {
        Log.d("CounterViewModel", "increment called")

        var cnt = "Nothing"

        _uiState.update {current ->
            val newCounter = current.counter + 1
            cnt = newCounter.toString()
            current.copy(
                counter = newCounter,
                label = "Counter: $newCounter"
            )
        }

        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast("Counter increased to $cnt"))
        }
    }
}
