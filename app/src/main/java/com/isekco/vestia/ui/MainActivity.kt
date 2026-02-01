package com.isekco.vestia.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.isekco.vestia.CounterViewModel
import com.isekco.vestia.R
import com.isekco.vestia.UiEvent
import kotlinx.coroutines.launch

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

                launch {
                    viewModel.uiState.collect { value ->
                        tvCounter.text = "Counter: ${value.counter}"
                        tvLabel.text = value.label
                    }
                }

                // 2) EVENT
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is UiEvent.ShowToast -> {
                                Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

    }
}