package com.example.unitconverter.data

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("base")
    val base: String,

    @SerializedName("rates")
    val rates: Map<String, Double>
)