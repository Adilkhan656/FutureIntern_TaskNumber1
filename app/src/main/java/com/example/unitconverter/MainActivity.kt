package com.example.unitconverter

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unitconverter.data.ExchangeRateResponse
import com.example.unitconverter.util.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private lateinit var inputValueEditText: EditText
    private lateinit var fromUnitSpinner: Spinner
    private lateinit var toUnitSpinner: Spinner
    private lateinit var convertButton: TextView
    private lateinit var resultTextView: TextView
    private lateinit var categorySpinner: Spinner
    private lateinit var categoryImageView: ImageView

    private var exchangeRates: Map<String, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputValueEditText = findViewById(R.id.inputValueEditText)
        fromUnitSpinner = findViewById(R.id.fromUnitSpinner)
        toUnitSpinner = findViewById(R.id.toUnitSpinner)
        convertButton = findViewById(R.id.convertButton)
        resultTextView = findViewById(R.id.resultTextView)
        categorySpinner = findViewById(R.id.categorySpinner)
        categoryImageView = findViewById(R.id.categoryImageView)








        val categories = arrayOf("Temperature", "Weight", "Scale", "Currency")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                setupSpinner(position)
                updateCategoryImage(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        lifecycleScope.launch {
            fetchExchangeRates()
        }


        convertButton.setOnClickListener {
            try {
                val inputText = inputValueEditText.text.toString()
                if (inputText.isNotEmpty()) {
                    val inputValue = inputText.toDouble()
                    val fromUnit = fromUnitSpinner.selectedItem.toString()
                    val toUnit = toUnitSpinner.selectedItem.toString()

                    val result = when (categorySpinner.selectedItemPosition) {
                        0 -> convertTemperature(inputValue, fromUnit, toUnit)
                        1 -> convertWeight(inputValue, fromUnit, toUnit)
                        2 -> convertScale(inputValue, fromUnit, toUnit)
                        3 -> convertCurrency(inputValue, fromUnit, toUnit)
                        else -> inputValue
                    }
                    resultTextView.text = "Result: ${round(result * 100) / 100} $toUnit"
                } else {
                    resultTextView.text = "Result: Enter a valid number"
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during conversion", e)
                resultTextView.text = "Result: An error occurred"
            }
        }

        // category
        setupSpinner(categorySpinner.selectedItemPosition)
        updateCategoryImage(categorySpinner.selectedItemPosition)
    }

    private fun setupSpinner(categoryPosition: Int) {
        val unitsArrayId = when (categoryPosition) {
            0 -> R.array.temperature_units
            1 -> R.array.weight_units
            2 -> R.array.scale_units
            3 -> R.array.currency_units
            else -> R.array.temperature_units
        }

        val units = resources.getStringArray(unitsArrayId)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromUnitSpinner.adapter = adapter
        toUnitSpinner.adapter = adapter
    }

    private fun convertTemperature(value: Double, fromUnit: String, toUnit: String): Double {
        return when (fromUnit) {
            "Celsius" -> when (toUnit) {
                "Fahrenheit" -> value * 9 / 5 + 32
                "Kelvin" -> value + 273.15
                else -> value
            }
            "Fahrenheit" -> when (toUnit) {
                "Celsius" -> (value - 32) * 5 / 9
                "Kelvin" -> (value - 32) * 5 / 9 + 273.15
                else -> value
            }
            "Kelvin" -> when (toUnit) {
                "Celsius" -> value - 273.15
                "Fahrenheit" -> (value - 273.15) * 9 / 5 + 32
                else -> value
            }
            else -> value
        }
    }

    private fun convertWeight(value: Double, fromUnit: String, toUnit: String): Double {
        return when (fromUnit) {
            "Grams" -> when (toUnit) {
                "Kilograms" -> value / 1000
                "Pounds" -> value / 453.592
                else -> value
            }
            "Kilograms" -> when (toUnit) {
                "Grams" -> value * 1000
                "Pounds" -> value * 2.20462
                else -> value
            }
            "Pounds" -> when (toUnit) {
                "Grams" -> value * 453.592
                "Kilograms" -> value / 2.20462
                else -> value
            }
            else -> value
        }
    }

    private fun convertScale(value: Double, fromUnit: String, toUnit: String): Double {
        return when (fromUnit) {
            "Meters" -> when (toUnit) {
                "Centimeters" -> value * 100
                "Inches" -> value * 39.3701
                "Feet" -> value * 3.28084
                "Yards" -> value * 1.09361
                else -> value
            }
            "Centimeters" -> when (toUnit) {
                "Meters" -> value / 100
                "Inches" -> value / 2.54
                "Feet" -> value / 30.48
                "Yards" -> value / 91.44
                else -> value
            }
            "Inches" -> when (toUnit) {
                "Meters" -> value / 39.3701
                "Centimeters" -> value * 2.54
                "Feet" -> value / 12
                "Yards" -> value / 36
                else -> value
            }
            "Feet" -> when (toUnit) {
                "Meters" -> value / 3.28084
                "Centimeters" -> value * 30.48
                "Inches" -> value * 12
                "Yards" -> value / 3
                else -> value
            }
            "Yards" -> when (toUnit) {
                "Meters" -> value / 1.09361
                "Centimeters" -> value * 91.44
                "Inches" -> value * 36
                "Feet" -> value * 3
                else -> value
            }
            else -> value
        }
    }

    private fun convertCurrency(value: Double, fromUnit: String, toUnit: String): Double {
        if (exchangeRates == null || !exchangeRates!!.containsKey(fromUnit) || !exchangeRates!!.containsKey(toUnit)) {
            return value
        }

        val fromRate = exchangeRates!![fromUnit] ?: 1.0
        val toRate = exchangeRates!![toUnit] ?: 1.0

        return value * (toRate / fromRate)
    }

    private suspend fun fetchExchangeRates() {
        try {
            val response: Response<ExchangeRateResponse> = RetrofitInstance.instance.getLatestRates("USD")
            if (response.isSuccessful && response.body() != null) {
                exchangeRates = response.body()?.rates
                Log.d("MainActivity", "Exchange rates fetched successfully: $exchangeRates")
            } else {
                Log.e("MainActivity", "Failed to fetch exchange rates")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error fetching exchange rates", e)
        }
    }

    private fun updateCategoryImage(categoryPosition: Int) {
        val drawableResId = when (categoryPosition) {
            0 -> R.drawable.ic_temperature
            1 -> R.drawable.ic_weight
            2 -> R.drawable.ic_scale
            3 -> R.drawable.ic_currency
            else -> R.drawable.ic_temperature
        }
        categoryImageView.setImageResource(drawableResId)
    }
}