package com.isekco.vestia.ui.main

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var totalPortfolioValueText: TextView
    private lateinit var menuButton: ImageButton
    private lateinit var assetRecyclerView: RecyclerView
    private lateinit var addTransactionFab: FloatingActionButton
    private lateinit var assetAdapter: AssetAdapter
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (application as VestiaApp).container.loadPortfolioSummaryUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupRecyclerView()
        setupClicks()
        observeUiState()
    }

    private fun bindViews() {
        totalPortfolioValueText = findViewById(R.id.totalPortfolioValueText)
        menuButton = findViewById(R.id.portfolioMenuButton)
        assetRecyclerView = findViewById(R.id.assetRecyclerView)
        addTransactionFab = findViewById(R.id.addTransactionFab)
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetAdapter()
        assetRecyclerView.layoutManager = LinearLayoutManager(this)
        assetRecyclerView.adapter = assetAdapter
    }

    private fun setupClicks() {
        addTransactionFab.setOnClickListener {
            Toast.makeText(this, "Add Transaction", Toast.LENGTH_SHORT).show()
        }

        menuButton.setOnClickListener {
            val sheet = ManagementBottomSheet()
            sheet.show(supportFragmentManager, "ManagementBottomSheet")
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: MainUiState) {
        totalPortfolioValueText.text = state.totalPortfolioValueText
        assetAdapter.submitList(state.assets)

        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
