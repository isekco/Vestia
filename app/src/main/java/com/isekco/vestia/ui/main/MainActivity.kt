package com.isekco.vestia.ui.main

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.isekco.vestia.VestiaApp
import com.isekco.vestia.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvContent: TextView

    private val viewModel: MainViewModel by viewModels {
        val container = (application as VestiaApp).container
        MainViewModelFactory(container.loadPositionsUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContent = findViewById(R.id.tvContent)

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: MainUiState) {

        when {
            state.isLoading -> {
                tvContent.text = "Loading positions..."
            }

            state.errorMessage != null -> {
                tvContent.text = "Error:\n${state.errorMessage}"
            }

            else -> {
                if (state.positions.isEmpty()) {
                    tvContent.text = "No positions."
                    return
                }

                val builder = StringBuilder()

                state.positions.forEach { position ->
                    builder.appendLine("Owner   : ${position.key.ownerId}")
                    builder.appendLine("Account : ${position.key.accountId}")
                    builder.appendLine("Asset   : ${position.key.assetKey}")
                    builder.appendLine("Qty     : ${position.quantity}")
                    builder.appendLine("WAC     : ${position.weightedAverageCost}")
                    builder.appendLine("Cost    : ${position.totalCost}")
                    builder.appendLine("-------------------------------")
                }

                tvContent.text = builder.toString()
            }
        }
    }
}
