package com.isekco.vestia

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import android.util.Log

class MainActivity : ComponentActivity() {

    private lateinit var tvCounter: TextView

    private lateinit var tvLabel: TextView
    private lateinit var btnInc: Button
    private lateinit var viewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCounter = findViewById(R.id.tvCounter)
        tvLabel = findViewById(R.id.tvLabel)
        btnInc = findViewById(R.id.btnInc)

        viewModel = ViewModelProvider(this)[CounterViewModel::class.java]

        btnInc.setOnClickListener {
            viewModel.increment()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { value ->
                    tvCounter.text = "Counter: ${value.counter}"
                    tvLabel.text = value.label
                }
            }
        }
    }
}
