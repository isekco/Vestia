package com.isekco.vestia.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isekco.vestia.R
import com.isekco.vestia.VestiaApp
import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.Rates
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var totalPortfolioValueText: TextView
    private lateinit var assetRecyclerView: RecyclerView
    private lateinit var addTransactionFab: FloatingActionButton

    private lateinit var assetAdapter: AssetAdapter

    private val assetUiMapper = AssetUiMapper()

    private val viewModel: MainViewModel by viewModels {
        val container = (application as VestiaApp).container
        MainViewModelFactory(container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupRecyclerView()
        setupActions()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboard(forceRefresh = true)
    }

    private fun bindViews() {
        totalPortfolioValueText = findViewById(R.id.totalPortfolioValueText)
        assetRecyclerView = findViewById(R.id.assetRecyclerView)
        addTransactionFab = findViewById(R.id.addTransactionFab)
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetAdapter()
        assetRecyclerView.layoutManager = LinearLayoutManager(this)
        assetRecyclerView.adapter = assetAdapter
    }

    private fun setupActions() {
        addTransactionFab.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
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
        if (state.isLoading && state.positions.isEmpty()) {
            totalPortfolioValueText.text = getString(R.string.portfolio_value_unavailable)
            assetAdapter.submitList(emptyList())
            return
        }

        renderDashboard(
            positions = state.positions,
            rates = state.rates
        )
    }

    private fun renderDashboard(
        positions: List<Position>,
        rates: Rates?
    ) {
        val assetItems = assetUiMapper.map(
            positions = positions,
            rates = rates
        )
        assetAdapter.submitList(assetItems)

        val portfolioTotal = assetUiMapper.calculatePortfolioTotalTry(
            positions = positions,
            rates = rates
        )

        totalPortfolioValueText.text = if (portfolioTotal == null) {
            getString(R.string.portfolio_value_unavailable)
        } else {
            formatTry(portfolioTotal)
        }
    }

    private fun formatTry(value: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val df = DecimalFormat("₺#,##0.00", symbols)
        return df.format(value.setScale(2, RoundingMode.HALF_UP))
    }
}