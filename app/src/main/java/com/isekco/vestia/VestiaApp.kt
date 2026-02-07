package com.isekco.vestia.ui

import android.app.Application
import com.isekco.vestia.di.AppContainer

class VestiaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
