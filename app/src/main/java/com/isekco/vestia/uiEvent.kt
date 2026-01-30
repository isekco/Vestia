package com.isekco.vestia

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}
