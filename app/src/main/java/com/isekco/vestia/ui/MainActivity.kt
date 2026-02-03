package com.isekco.vestia.ui
import com.isekco.vestia.R
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.isekco.vestia.di.AppContainer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var tvOutput: TextView
    private lateinit var btnRefresh: Button

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        tvOutput = findViewById(R.id.tvOutput)
        btnRefresh = findViewById(R.id.btnRefresh)

        // 1) AppContainer: wiring
        val container = AppContainer(applicationContext)

        // 2) ViewModelFactory: ViewModel'e UseCase enjekte eder
        val factory = MainViewModelFactory(container.loadTransactionsUseCase)

        // 3) ViewModel'i sistemin mekanizmasıyla al
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // 4) UI event -> ViewModel niyeti
        btnRefresh.setOnClickListener {
            viewModel.load()
        }

        // 5) StateFlow observe -> render
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }

        // İlk yükleme
        viewModel.load()
    }

    private fun render(state: MainUiState) {
        val text = buildString {
            appendLine("Loading: ${state.isLoading}")
            state.errorMessage?.let { appendLine("Error: $it") }
            appendLine("----")
            state.items.forEach { t ->
                appendLine("#${t.id} ${t.title}  ${t.amount} ${t.currency}")
            }
        }
        tvOutput.text = text
    }
}
