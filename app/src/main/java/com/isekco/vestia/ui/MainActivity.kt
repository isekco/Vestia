package com.isekco.vestia.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.isekco.vestia.di.AppContainer


class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer
        get() = (application as VestiaApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
