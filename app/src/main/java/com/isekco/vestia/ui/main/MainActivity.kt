package com.isekco.vestia.ui.main

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isekco.vestia.R

class MainActivity : AppCompatActivity() {

    private lateinit var totalPortfolioValueText: TextView
    private lateinit var assetRecyclerView: RecyclerView
    private lateinit var addTransactionFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupRecyclerView()
        setupClicks()
        setupInitialUi()
    }

    private fun bindViews() {
        totalPortfolioValueText = findViewById(R.id.totalPortfolioValueText)
        assetRecyclerView = findViewById(R.id.assetRecyclerView)
        addTransactionFab = findViewById(R.id.addTransactionFab)
    }

    private fun setupRecyclerView() {
        assetRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClicks() {
        addTransactionFab.setOnClickListener {
            Toast.makeText(this, "Add Transaction daha bağlanmadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupInitialUi() {
        totalPortfolioValueText.text = "---"
    }
}