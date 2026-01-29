package com.isekco.vestia

import androidx.lifecycle.ViewModel
import android.util.Log

class CounterViewModel : ViewModel() {

    var counter: Int = 0
        private set

    init {
        Log.d("VestiaTrace", "init vmHash=${System.identityHashCode(this)}")
    }

    fun increment() {
        counter += 1
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("VestiaTrace", "onCleared vmHash=${System.identityHashCode(this)}")
    }
}
