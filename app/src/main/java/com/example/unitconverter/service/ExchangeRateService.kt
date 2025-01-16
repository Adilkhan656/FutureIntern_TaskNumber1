package com.example.unitconverter.service

import com.example.unitconverter.data.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateService {
    @GET("latest")
    suspend fun getLatestRates(@Query("base") base: String): Response<ExchangeRateResponse>
}