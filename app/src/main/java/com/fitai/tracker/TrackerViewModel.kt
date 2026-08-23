package com.fitai.tracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TrackerViewModel : ViewModel() {
    var rawWeightInput by mutableStateOf("")
        private set
    var trendWeight by mutableStateOf(82.1)
        private set
    var lastRawWeight by mutableStateOf(82.4)
        private set
    var estimatedTDEE by mutableStateOf(2450)
        private set

    private val smoothingFactor = 0.1 // plus petit = courbe plus lisse

    fun onWeightInputChange(value: String) {
        rawWeightInput = value
    }

    fun logWeight() {
        val newWeight = rawWeightInput.replace(",", ".").toDoubleOrNull() ?: return
        lastRawWeight = newWeight
        trendWeight = trendWeight + smoothingFactor * (newWeight - trendWeight)
        rawWeightInput = ""
    }
}
