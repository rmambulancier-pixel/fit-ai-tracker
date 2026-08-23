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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var healthConnectAvailable by mutableStateOf(true)
        private set

    private var _weightHistory by mutableStateOf<List<WeightRecord>>(emptyList())
    val weightHistory: List<WeightRecord>
        get() = _weightHistory

    private var _scanHistory by mutableStateOf<List<ScanRecord>>(emptyList())
    val scanHistory: List<ScanRecord>
        get() = _scanHistory

    private val smoothingFactor = 0.1

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    fun onWeightInputChange(value: String) {
        rawWeightInput = value
    }

    fun logWeight() {
        val newWeight = rawWeightInput.replace(",", ".").toDoubleOrNull() ?: return
        lastRawWeight = newWeight
        trendWeight = trendWeight + smoothingFactor * (newWeight - trendWeight)
        
        val newRecord = WeightRecord(System.currentTimeMillis(), newWeight)
        _weightHistory = _weightHistory + newRecord
        
        rawWeightInput = ""
    }

    fun onHealthPermissionsResult(grantedPermissions: Set<String>) {
        // Callback sécurisé pour le résultat des permissions Health Connect
        // Tu peux ajouter ici une logique métier plus tard (sync, état UI, etc.)
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
                val result = response.text ?: "Pas de réponse de Gemini."
                scanResult = result
                
                val scanRecord = ScanRecord(System.currentTimeMillis(), result)
                _scanHistory = _scanHistory + scanRecord
            } catch (e: Exception) {
                scanResult = "Erreur : ${e.message}"
            } finally {
                isScanning = false
            }
        }
    }

    companion object {
        fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
 
