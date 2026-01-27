package com.isekco.vestia

import android.app.Application
import android.util.Log

class VestiaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Checkpoint-2: bilinçli olarak boş

        Log.d("VestiaApp", "Application created")
    }
}
