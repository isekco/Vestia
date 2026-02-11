package com.isekco.vestia.ui

import android.app.Application
import com.isekco.vestia.di.AppContainer

class VestiaApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
