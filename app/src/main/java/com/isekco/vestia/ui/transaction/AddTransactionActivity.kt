package com.isekco.vestia.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.isekco.vestia.R
import com.isekco.vestia.VestiaApp
import com.isekco.vestia.domain.usecase.AddTransactionInput
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private data class OwnerOpt(
        val id: String,
        val name: String
    )

    private data class AccountOpt(
        val id: String,
        val ownerId: String,
        val name: String,
        val assetToken: String,
        val qtyUnit: String
    )

    private lateinit var spOwner: Spinner
    private lateinit var spAccount: Spinner
    private lateinit var spType: Spinner

    private lateinit var tvAsset: TextView
    private lateinit var etQuantity: EditText
    private lateinit var tvQtyUnit: TextView
    private lateinit var etUnitPrice: EditText
    private lateinit var tvPriceCurrency: TextView
    private lateinit var tvTotalCost: TextView

    private lateinit var btnPickDate: Button
    private lateinit var tvSelectedDate: TextView

    private lateinit var etTags: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private val owners = listOf(
        OwnerOpt("o1", "Owner1"),
        OwnerOpt("o2", "Owner2"),
        OwnerOpt("o3", "Owner3")
    )

    private val accounts = listOf(
        AccountOpt("AccountUSD_o1", "o1", "AccountUSD_o1", "USD", "USD"),
        AccountOpt("AccountXAU_o1", "o1", "AccountXAU_o1", "XAU", "g"),
        AccountOpt("AccountUSD_o2", "o2", "AccountUSD_o2", "USD", "USD"),
        AccountOpt("AccountGBP_o3", "o3", "AccountGBP_o3", "GBP", "GBP")
    )

    private val selectedCalendar: Calendar = Calendar.getInstance()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val viewModel: AddTransactionViewModel by viewModels {
        val container = (application as VestiaApp).container
        AddTransactionViewModelFactory(container.addTransactionUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        bindViews()
        setupStaticSpinners()
        setupDate()
        setupWatchers()
        bindActions()
        observeState()
    }

    private fun bindViews() {
        spOwner = findViewById(R.id.spOwner)
        spAccount = findViewById(R.id.spAccount)
        spType = findViewById(R.id.spType)

        tvAsset = findViewById(R.id.tvAsset)
        etQuantity = findViewById(R.id.etQuantity)
        tvQtyUnit = findViewById(R.id.tvQtyUnit)
        etUnitPrice = findViewById(R.id.etUnitPrice)
        tvPriceCurrency = findViewById(R.id.tvPriceCurrency)
        tvTotalCost = findViewById(R.id.tvTotalCost)

        btnPickDate = findViewById(R.id.btnPickDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)

        etTags = findViewById(R.id.etTags)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupStaticSpinners() {
        spOwner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            owners.map { it.name }
        )

        spType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("BUY", "SELL")
        )

        spOwner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val ownerId = owners[position].id
                val accountsForOwner = accounts.filter { it.ownerId == ownerId }

                spAccount.adapter = ArrayAdapter(
                    this@AddTransactionActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    accountsForOwner.map { it.name }
                )

                applyAccountDerivedFields(accountsForOwner.firstOrNull())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val ownerId = owners[spOwner.selectedItemPosition].id
                val accountsForOwner = accounts.filter { it.ownerId == ownerId }
                applyAccountDerivedFields(accountsForOwner.getOrNull(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun applyAccountDerivedFields(account: AccountOpt?) {
        if (account == null) {
            tvAsset.text = getString(R.string.placeholder_dash)
            tvQtyUnit.text = getString(R.string.placeholder_dash)
            return
        }

        tvAsset.text = account.assetToken
        tvQtyUnit.text = account.qtyUnit
        tvPriceCurrency.text = getString(R.string.currency_try)

        updateTotalCost()
    }

    private fun setupDate() {
        tvSelectedDate.text = dateFormatter.format(selectedCalendar.time)

        btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(Calendar.YEAR, year)
                    selectedCalendar.set(Calendar.MONTH, month)
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    selectedCalendar.set(Calendar.MINUTE, 0)
                    selectedCalendar.set(Calendar.SECOND, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)

                    tvSelectedDate.text = dateFormatter.format(selectedCalendar.time)
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                updateTotalCost()
            }
        }

        etQuantity.addTextChangedListener(watcher)
        etUnitPrice.addTextChangedListener(watcher)
    }

    private fun updateTotalCost() {
        val qtyText = etQuantity.text?.toString()?.trim().orEmpty()
        val priceText = etUnitPrice.text?.toString()?.trim().orEmpty()

        val total = runCatching {
            if (qtyText.isBlank() || priceText.isBlank()) {
                BigDecimal.ZERO
            } else {
                BigDecimal(qtyText).multiply(BigDecimal(priceText))
            }
        }.getOrDefault(BigDecimal.ZERO)

        tvTotalCost.text = getString(
            R.string.total_cost_value,
            total.toPlainString(),
            getString(R.string.currency_try)
        )
    }

    private fun bindActions() {
        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val input = buildInputOrToast() ?: return@setOnClickListener
            viewModel.submit(input)
        }
    }

    private fun buildInputOrToast(): AddTransactionInput? {
        val owner = owners[spOwner.selectedItemPosition]
        val ownerId = owner.id

        val accountsForOwner = accounts.filter { it.ownerId == ownerId }
        val account = accountsForOwner.getOrNull(spAccount.selectedItemPosition)
            ?: return toastNull(getString(R.string.error_invalid_account_selection))

        val txType = spType.selectedItem?.toString()?.trim().orEmpty()
        val qty = etQuantity.text?.toString()?.trim().orEmpty()
        val unitPrice = etUnitPrice.text?.toString()?.trim().orEmpty()

        if (qty.isBlank()) return toastNull(getString(R.string.error_quantity_required))
        if (unitPrice.isBlank()) return toastNull(getString(R.string.error_unit_price_required))

        val epochMs = selectedCalendar.timeInMillis
        val tags = etTags.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        return AddTransactionInput(
            ownerId = ownerId,
            accountId = account.id,
            transactionType = txType,
            quantity = qty,
            unitPrice = unitPrice,
            epochMs = epochMs,
            tags = tags
        )
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    btnSave.isEnabled = !state.isSaving
                    btnCancel.isEnabled = !state.isSaving

                    state.errorMessage?.let {
                        Toast.makeText(this@AddTransactionActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.consumeError()
                    }

                    if (state.isSuccess) {
                        Toast.makeText(
                            this@AddTransactionActivity,
                            getString(R.string.transaction_added),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun toastNull(message: String): AddTransactionInput? {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        return null
    }
}