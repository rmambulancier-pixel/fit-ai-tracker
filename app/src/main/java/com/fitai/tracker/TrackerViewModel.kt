package com.fitai.tracker

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class TrackerViewModel : ViewModel() {
    var rawWeightInput by mutableStateOf("")
        private set
    var trendWeight by mutableStateOf(82.1)
        private set
    var lastRawWeight by mutableStateOf(82.4)
        private set
    var estimatedTDEE by mutableStateOf(2450)
        private set

    var scanResult by mutableStateOf<String?>(null)
        private set
    var isScanning by mutableStateOf(false)
        private set

    private val smoothingFactor = 0.1

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun onWeightInputChange(value: String) {
        rawWeightInput = value
    }

    fun logWeight() {
        val newWeight = rawWeightInput.replace(",", ".").toDoubleOrNull() ?: return
        lastRawWeight = newWeight
        trendWeight = trendWeight + smoothingFactor * (newWeight - trendWeight)
        rawWeightInput = ""
    }

    fun scanMeal(bitmap: Bitmap) {
        isScanning = true
        scanResult = null
        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(
                            "Identifie ce repas ou cette boisson. Donne en français, " +
                            "de façon concise : le nom du plat, une estimation des calories, " +
                            "et la répartition protéines/glucides/lipides."
                        )
                    }
                )
                scanResult = response.text ?: "Pas de réponse de Gemini."
            } catch (e: Exception) {
                scanResult = "Erreur : ${e.message}"
            } finally {
                isScanning = false
            }
        }
    }
}
