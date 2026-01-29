package com.isekco.vestia

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Button
import android.util.Log

class MainActivity : ComponentActivity() {

    companion object {
        const val KEY_COUNTER = "cnt"
    }

    private lateinit var tvCounter: TextView
    private lateinit var btnInc: Button
    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCounter = findViewById(R.id.tvCounter)
        btnInc = findViewById(R.id.btnInc)

        counter = savedInstanceState?.getInt(KEY_COUNTER) ?: 0

        Log.d("VestiaState", "onCreate(savedInstanceState=${savedInstanceState != null}) counter=$counter")
        render()

        btnInc.setOnClickListener{
            counter++
            Log.d("VestiaState", "click -> counter=$counter")
            render()

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_COUNTER, counter)



    }

    private fun render(){
        tvCounter.text = "Counter : $counter"
    }
}
