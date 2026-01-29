package com.isekco.vestia

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Button
import android.util.Log
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private lateinit var tvCounter: TextView
    private lateinit var btnInc: Button

    private lateinit var viewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCounter = findViewById(R.id.tvCounter)
        btnInc = findViewById(R.id.btnInc)

        viewModel = ViewModelProvider(this)[CounterViewModel::class.java]

        Log.d(
            "VestiaTrace",
            "onCreate actHash=${System.identityHashCode(this)} " +
                    "vmHash=${System.identityHashCode(viewModel)} " +
                    "saved=${savedInstanceState != null}"
        )

        render()

        btnInc.setOnClickListener{
            viewModel.increment()
            render()
        }
    }

    override fun onDestroy() {
        Log.d("VestiaTrace", "onDestroy actHash=${System.identityHashCode(this)} isFinishing=$isFinishing")
        super.onDestroy()
    }

    private fun render(){
        tvCounter.text = "Counter: ${viewModel.counter}"
    }
}
