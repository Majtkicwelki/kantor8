package com.example.kantor8

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    @GET("exchangerates/tables/A/?format=json")
    suspend fun getExchangeRates(): List<CurrencyTable>

    @GET("exchangerates/rates/A/{USD}/last/7?format=json")
    suspend fun getHistoricalRates(@Path("USD") currencyCode: String): CurrencyHistory

    companion object {
        private const val BASE_URL = "https://api.nbp.pl/api/"
        fun create(): CurrencyApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApi::class.java)
    }
}

data class CurrencyHistory(
    val rates: List<CurrencyRateHistory>
)

data class CurrencyRateHistory(
    val effectiveDate: String,
    val mid: Double
    )

data class CurrencyRate(
    val code: String,
    val currency: String,
    val mid: Double
)

data class CurrencyTable(
    val table: String,
    val no: String,
    val effectiveDate: String,
    val rates: List<CurrencyRate>
)