package com.isekco.vestia.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isekco.vestia.R
import com.isekco.vestia.VestiaApp
import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.ui.transaction.AddTransactionActivity
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var totalPortfolioValueText: TextView
    private lateinit var pieChart: PieChart
    private lateinit var assetRecyclerView: RecyclerView
    private lateinit var addTransactionFab: FloatingActionButton

    private lateinit var legendUsdUnderline: View
    private lateinit var legendXauUnderline: View
    private lateinit var legendGbpUnderline: View
    private lateinit var legendEurUnderline: View

    private lateinit var assetAdapter: AssetAdapter

    private var latestPositions: List<Position> = emptyList()
    private var selectedAssetKey: String? = null

    private val viewModel: MainViewModel by viewModels {
        val container = (application as VestiaApp).container
        MainViewModelFactory(container.loadPositionsUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupRecyclerView()
        setupPieChart()
        setupActions()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPositions(forceRefresh = true)
    }

    private fun bindViews() {
        totalPortfolioValueText = findViewById(R.id.totalPortfolioValueText)
        pieChart = findViewById(R.id.pieChart)
        assetRecyclerView = findViewById(R.id.assetRecyclerView)
        addTransactionFab = findViewById(R.id.addTransactionFab)

        legendUsdUnderline = findViewById(R.id.legendUsdUnderline)
        legendXauUnderline = findViewById(R.id.legendXauUnderline)
        legendGbpUnderline = findViewById(R.id.legendGbpUnderline)
        legendEurUnderline = findViewById(R.id.legendEurUnderline)
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetAdapter()
        assetRecyclerView.layoutManager = LinearLayoutManager(this)
        assetRecyclerView.adapter = assetAdapter
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.isRotationEnabled = false
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 62f
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.vestia_white))
        pieChart.setTransparentCircleColor(ContextCompat.getColor(this, R.color.vestia_white))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.setExtraOffsets(8f, 8f, 8f, 8f)

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val assetKey = (e as? PieEntry)?.label ?: return
                selectedAssetKey = assetKey
                renderDashboard(latestPositions)
            }

            override fun onNothingSelected() {
                selectedAssetKey = null
                renderDashboard(latestPositions)
            }
        })
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
        when {
            state.isLoading -> {
                latestPositions = emptyList()
                totalPortfolioValueText.text = getString(R.string.sample_total_value)
                assetAdapter.submitList(emptyList())
                pieChart.clear()
                updateLegendSelection(null)
            }

            state.errorMessage != null -> {
                latestPositions = emptyList()
                totalPortfolioValueText.text = getString(R.string.sample_total_value)
                assetAdapter.submitList(emptyList())
                pieChart.clear()
                updateLegendSelection(null)
            }

            else -> {
                latestPositions = state.positions

                if (selectedAssetKey != null &&
                    state.positions.none { it.key.assetKey == selectedAssetKey }
                ) {
                    selectedAssetKey = null
                }

                renderDashboard(state.positions)
            }
        }
    }

    private fun renderDashboard(positions: List<Position>) {
        val assetItems = AssetUiMapper.toAssetUiModels(
            positions = positions,
            selectedAssetKey = selectedAssetKey
        )

        assetAdapter.submitList(assetItems)

        val portfolioTotal = AssetUiMapper.calculatePortfolioTotalTry(assetItems)
        totalPortfolioValueText.text = formatTry(portfolioTotal)

        updateLegendSelection(selectedAssetKey)
        renderPieChart(assetItems, portfolioTotal)
    }

    private fun renderPieChart(
        assetItems: List<AssetUiModel>,
        portfolioTotal: BigDecimal
    ) {
        if (assetItems.isEmpty()) {
            pieChart.clear()
            return
        }

        val entries = assetItems.map { item ->
            PieEntry(item.totalValueTry.toFloat(), item.assetKey)
        }

        val colors = assetItems.map { item ->
            ContextCompat.getColor(this, colorResForAsset(item.assetKey))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 12f
        }

        val pieData = PieData(dataSet).apply {
            setDrawValues(false)
        }

        pieChart.data = pieData
        pieChart.centerText = formatTry(portfolioTotal)
        pieChart.setCenterTextSize(16f)
        pieChart.animateY(900)

        pieChart.invalidate()
    }

    private fun updateLegendSelection(selectedAsset: String?) {
        legendUsdUnderline.visibility =
            if (selectedAsset == "USD") View.VISIBLE else View.INVISIBLE

        legendXauUnderline.visibility =
            if (selectedAsset == "XAU") View.VISIBLE else View.INVISIBLE

        legendGbpUnderline.visibility =
            if (selectedAsset == "GBP") View.VISIBLE else View.INVISIBLE

        legendEurUnderline.visibility =
            if (selectedAsset == "EUR") View.VISIBLE else View.INVISIBLE
    }

    private fun colorResForAsset(assetKey: String): Int {
        return when (assetKey.uppercase(Locale.US)) {
            "USD" -> R.color.asset_usd
            "XAU" -> R.color.asset_xau
            "GBP" -> R.color.asset_gbp
            "EUR" -> R.color.asset_eur
            else -> R.color.vestia_border
        }
    }

    private fun formatTry(value: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val df = DecimalFormat("₺#,##0.00", symbols)
        return df.format(value)
    }
}