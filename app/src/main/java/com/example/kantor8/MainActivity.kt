package com.example.kantor8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kantor8.CurrencyApi
import com.example.kantor8.CurrencyRate
import com.example.kantor8.CurrencyRateHistory
import com.example.kantor8.CurrencyRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyConverterApp()
        }
    }
}

@Composable
fun CurrencyConverterApp() {
    val api = CurrencyApi.create()
    val repository = CurrencyRepository(api)
    val coroutineScope = rememberCoroutineScope()

    var rates by remember { mutableStateOf<List<CurrencyRate>>(emptyList()) }
    var historicalRates by remember { mutableStateOf<List<CurrencyRateHistory>>(emptyList()) }
    var inputAmount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf<CurrencyRate?>(null) }
    var result by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            rates = repository.fetchRates()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kantor wymiany walut", style = MaterialTheme.typography.titleLarge)

        BasicTextField(
            value = inputAmount,
            onValueChange = { inputAmount = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(selectedCurrency, rates) { currency ->
            selectedCurrency = currency
            coroutineScope.launch {
                if (currency != null) {
                    historicalRates = repository.fetchHistoricalRates(currency.code)
                }
            }
        }

        Button(onClick = {
            val amount = inputAmount.toDoubleOrNull()
            if (amount != null && selectedCurrency != null) {
                result = if (selectedCurrency!!.code == "PLN") {
                    "${amount / selectedCurrency!!.mid} ${selectedCurrency!!.code}"
                } else {
                    "${amount * selectedCurrency!!.mid} PLN"
                }
            }
        }) {
            Text("Przelicz")
        }

        Text("Wynik: $result")

        if (historicalRates.isNotEmpty()) {
            CurrencyRateChart(historicalRates)
        }
    }
}

@Composable
fun CurrencyRateChart(historicalRates: List<CurrencyRateHistory>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        val padding = 16f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val maxValue = historicalRates.maxOf { it.mid }
        val minValue = historicalRates.minOf { it.mid }

        val path = Path()
        historicalRates.forEachIndexed { index, rate ->
            val x = padding + (index.toFloat() / (historicalRates.size - 1)) * width
            val y = padding + (1 - (rate.mid - minValue) / (maxValue - minValue)) * height
            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }

        drawPath(path, color = Color.Blue, style = Stroke(width = 3f))

        // Optional: Add labels and axes if needed.
    }
}

@Composable
fun DropdownMenu(
    selectedCurrency: CurrencyRate?,
    rates: List<CurrencyRate>,
    onCurrencySelected: (CurrencyRate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedCurrency?.code ?: "Wybierz walutę")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rates.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.code) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}
