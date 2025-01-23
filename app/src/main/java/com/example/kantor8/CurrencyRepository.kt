package com.example.kantor8

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencyRepository(private val api: CurrencyApi) {
    suspend fun fetchRates(): List<CurrencyRate> = withContext(Dispatchers.IO) {
        val response = api.getExchangeRates()
        response.firstOrNull()?.rates ?: emptyList()
    }

    suspend fun fetchHistoricalRates(currencyCode: String): List<CurrencyRateHistory> = withContext(Dispatchers.IO) {
        val response = api.getHistoricalRates(currencyCode)
        response.rates
    }
}

