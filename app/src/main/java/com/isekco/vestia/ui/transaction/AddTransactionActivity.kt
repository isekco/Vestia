package com.isekco.vestia.ui.transaction

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var edtOwnerId: EditText
    private lateinit var edtAccountId: EditText
    private lateinit var edtTransactionType: EditText
    private lateinit var edtAssetTypeToken: EditText
    private lateinit var edtQuantity: EditText
    private lateinit var edtUnitPrice: EditText
    private lateinit var edtPriceCurrency: EditText
    private lateinit var edtEpochMs: EditText
    private lateinit var edtTags: EditText

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private val viewModel: AddTransactionViewModel by viewModels {
        val container = (application as VestiaApp).container
        AddTransactionViewModelFactory(container.addTransactionUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        bindViews()
        bindActions()
        observeState()
    }

    private fun bindViews() {
        edtOwnerId = findViewById(R.id.edtOwnerId)
        edtAccountId = findViewById(R.id.edtAccountId)
        edtTransactionType = findViewById(R.id.edtTransactionType)
        edtAssetTypeToken = findViewById(R.id.edtAssetTypeToken)
        edtQuantity = findViewById(R.id.edtQuantity)
        edtUnitPrice = findViewById(R.id.edtUnitPrice)
        edtPriceCurrency = findViewById(R.id.edtPriceCurrency)
        edtEpochMs = findViewById(R.id.edtEpochMs)
        edtTags = findViewById(R.id.edtTags)

        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun bindActions() {
        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val input = buildInputOrShowError() ?: return@setOnClickListener
            viewModel.submit(input)
        }
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

    private fun buildInputOrShowError(): AddTransactionInput? {
        val ownerId = edtOwnerId.text?.toString()?.trim().orEmpty()
        val accountId = edtAccountId.text?.toString()?.trim().orEmpty()
        val txType = edtTransactionType.text?.toString()?.trim().orEmpty()
        val assetToken = edtAssetTypeToken.text?.toString()?.trim().orEmpty()
        val qty = edtQuantity.text?.toString()?.trim().orEmpty()
        val price = edtUnitPrice.text?.toString()?.trim().orEmpty()
        val priceCur = edtPriceCurrency.text?.toString()?.trim().orEmpty()
        val epochRaw = edtEpochMs.text?.toString()?.trim().orEmpty()
        val tags = edtTags.text?.toString()?.trim().orEmpty()

        if (ownerId.isBlank()) return toastAndNull("ownerId is required (e.g., o1)")
        if (accountId.isBlank()) return toastAndNull("accountId is required (e.g., a2)")
        if (txType.isBlank()) return toastAndNull("transactionType is required (BUY/SELL)")
        if (assetToken.isBlank()) return toastAndNull("assetTypeToken is required (XAU/USD/EUR/TRY/GBP)")
        if (qty.isBlank()) return toastAndNull("quantity is required")
        if (price.isBlank()) return toastAndNull("unitPrice is required")
        if (priceCur.isBlank()) return toastAndNull("priceCurrency is required (TRY/USD/EUR/GBP)")

        val epochMs: Long? = if (epochRaw.isBlank()) null else epochRaw.toLongOrNull()
        if (epochRaw.isNotBlank() && epochMs == null) {
            return toastAndNull("epochMs must be a number")
        }

        return AddTransactionInput(
            ownerId = ownerId,
            accountId = accountId,
            transactionType = txType.uppercase(),
            assetTypeToken = assetToken.uppercase(),
            quantity = qty,
            unitPrice = price,
            priceCurrency = priceCur.uppercase(),
            epochMs = epochMs,
            tags = tags.takeIf { it.isNotBlank() }
        )
    }

    private fun toastAndNull(message: String): AddTransactionInput? {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        return null
    }
}