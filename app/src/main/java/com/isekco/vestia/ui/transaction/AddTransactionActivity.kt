package com.isekco.vestia.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddTransactionActivity : AppCompatActivity() {

    private data class OwnerOpt(val id: String, val name: String)
    private data class AccountOpt(val id: String, val ownerId: String, val name: String, val assetToken: String, val qtyUnit: String)

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

    private var selectedDate: LocalDate = LocalDate.now()
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        // Owner spinner
        spOwner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            owners.map { it.name }
        )

        // Type spinner
        spType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("BUY", "SELL")
        )

        spOwner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val ownerId = owners[position].id
                val accForOwner = accounts.filter { it.ownerId == ownerId }
                spAccount.adapter = ArrayAdapter(
                    this@AddTransactionActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    accForOwner.map { it.name }
                )
                // account değişince asset de değişsin
                applyAccountDerivedFields(accForOwner.firstOrNull())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val ownerId = owners[spOwner.selectedItemPosition].id
                val accForOwner = accounts.filter { it.ownerId == ownerId }
                applyAccountDerivedFields(accForOwner.getOrNull(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyAccountDerivedFields(account: AccountOpt?) {
        if (account == null) {
            tvAsset.text = "—"
            tvQtyUnit.text = "—"
            return
        }
        tvAsset.text = account.assetToken
        tvQtyUnit.text = account.qtyUnit

        // UnitPrice currency hep TRY
        tvPriceCurrency.text = "TRY"
        updateTotalCost()
    }

    private fun setupDate() {
        tvSelectedDate.text = selectedDate.format(dateFmt)
        btnPickDate.text = "Pick date"

        btnPickDate.setOnClickListener {
            val d = selectedDate
            DatePickerDialog(
                this,
                { _, y, m, day ->
                    selectedDate = LocalDate.of(y, m + 1, day)
                    tvSelectedDate.text = selectedDate.format(dateFmt)
                },
                d.year, d.monthValue - 1, d.dayOfMonth
            ).show()
        }
    }

    private fun setupWatchers() {
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateTotalCost()
            }
        }
        etQuantity.addTextChangedListener(watcher)
        etUnitPrice.addTextChangedListener(watcher)
    }

    private fun updateTotalCost() {
        val qty = etQuantity.text?.toString()?.trim().orEmpty()
        val price = etUnitPrice.text?.toString()?.trim().orEmpty()

        val total = runCatching {
            if (qty.isBlank() || price.isBlank()) return@runCatching BigDecimal.ZERO
            BigDecimal(qty).multiply(BigDecimal(price))
        }.getOrDefault(BigDecimal.ZERO)

        tvTotalCost.text = "${total.toPlainString()} TRY"
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

        val accForOwner = accounts.filter { it.ownerId == ownerId }
        val account = accForOwner.getOrNull(spAccount.selectedItemPosition)
            ?: return toastNull("Account selection invalid")

        val txType = spType.selectedItem?.toString()?.trim().orEmpty()

        val qty = etQuantity.text?.toString()?.trim().orEmpty()
        val unitPrice = etUnitPrice.text?.toString()?.trim().orEmpty()

        if (qty.isBlank()) return toastNull("Quantity is required")
        if (unitPrice.isBlank()) return toastNull("Unit price is required")

        // epochMs: seçilen günün start-of-day (local)
        val epochMs = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

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
                        Toast.makeText(this@AddTransactionActivity, "Transaction added.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun toastNull(msg: String): AddTransactionInput? {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        return null
    }
}