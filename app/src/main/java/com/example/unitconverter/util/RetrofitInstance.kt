package com.example.unitconverter.util

import com.example.unitconverter.service.ExchangeRateService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.exchangerate-api.com/v4/latest/"

    val instance: ExchangeRateService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ExchangeRateService::class.java)
    }
}