package com.isekco.vestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Button
import android.util.Log

class MainActivity : ComponentActivity() {

    private lateinit var tvLifeCycle: TextView

    private fun mark(event: String) {
        Log.d("VestiaLC", event)

        // TextView hazır değilse (onCreate öncesi) sadece log bas
        if (!::tvLifeCycle.isInitialized) return

        val prev = tvLifeCycle.text?.toString().orEmpty()
        val line = "• $event"

        tvLifeCycle.text = if (prev.isBlank()) line else "$line\n$prev"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        tvLifeCycle = findViewById(R.id.tvLifecycle)

        mark("onCreate (savedInstanseState=${savedInstanceState != null})")
    }
    override fun onStart() {
        super.onStart()
        mark("onStart")
    }

    override fun onResume() {
        super.onResume()
        mark("onResume")
    }

    override fun onPause() {
        mark("onPause")
        super.onPause()
    }

    override fun onStop() {
        mark("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        mark("onDestroy")
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        mark("onRestart")
    }
}
