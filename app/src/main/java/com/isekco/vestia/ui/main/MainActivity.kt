package com.isekco.vestia.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.isekco.vestia.R
import com.isekco.vestia.VestiaApp
import com.isekco.vestia.ui.transaction.AddTransactionActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvContent: TextView
    private lateinit var btnAddTransaction: Button

    private val viewModel: MainViewModel by viewModels {
        val container = (application as VestiaApp).container
        MainViewModelFactory(container.loadPositionsUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContent = findViewById(R.id.tvContent)
        btnAddTransaction = findViewById(R.id.btnAddTransaction)

        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        observeState()
    }

    override fun onResume() {
        super.onResume()
        // AddTransaction'dan dönünce pozisyonları tazele
        viewModel.loadPositions(forceRefresh = true)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: MainUiState) {
        when {
            state.isLoading -> tvContent.text = "Loading positions..."
            state.errorMessage != null -> tvContent.text = "Error:\n${state.errorMessage}"
            state.positions.isEmpty() -> tvContent.text = "No positions."
            else -> {
                val b = StringBuilder()
                state.positions.forEach { p ->
                    b.appendLine("Owner   : ${p.key.ownerId}")
                    b.appendLine("Account : ${p.key.accountId}")
                    b.appendLine("Asset   : ${p.key.assetKey}")
                    b.appendLine("Qty     : ${p.quantity}")
                    b.appendLine("WAC     : ${p.weightedAverageCost}")
                    b.appendLine("Cost    : ${p.totalCost}")
                    b.appendLine("-------------------------------")
                }
                tvContent.text = b.toString()
            }
        }
    }
}