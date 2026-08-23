package com.fitai.tracker.sync

import android.content.Context
import com.fitai.tracker.HealthConnectHelper
import com.fitai.tracker.db.HealthDataDatabase
import com.fitai.tracker.db.HealthDataEntity
import kotlinx.coroutines.*
import java.time.Instant

class HealthDataSyncManager(private val context: Context) {
    private val database = HealthDataDatabase.getInstance(context)
    private val dao = database.healthDataDao()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * Synchroniser les données DEPUIS Health Connect
     * (Lire les données des autres apps de santé)
     */
    suspend fun syncFromHealthConnect() {
        try {
            // Lire les poids
            val weights = HealthConnectHelper.readWeightRecords(context, hoursBack = 7 * 24)
            weights.forEach { record ->
                val entity = HealthDataEntity(
                    dataType = "weight",
                    value = record.weight.inKilograms,
                    unit = "kg",
                    timestamp = record.time.toEpochMilli(),
                    source = "health_connect"
                )
                dao.insert(entity)
            }

            // Lire les pas
            val steps = HealthConnectHelper.readStepsRecords(context, hoursBack = 7 * 24)
            steps.forEach { record ->
                val entity = HealthDataEntity(
                    dataType = "steps",
                    value = record.count.toDouble(),
                    unit = "steps",
                    timestamp = record.startTime.toEpochMilli(),
                    source = "health_connect"
                )
                dao.insert(entity)
            }

            // Lire la fréquence cardiaque
            val heartRates = HealthConnectHelper.readHeartRateRecords(context, hoursBack = 7 * 24)
            heartRates.forEach { record ->
                record.samples.forEach { sample ->
                    val entity = HealthDataEntity(
                        dataType = "heart_rate",
                        value = sample.beatsPerMinute.toDouble(),
                        unit = "bpm",
                        timestamp = sample.time.toEpochMilli(),
                        source = "health_connect"
                    )
                    dao.insert(entity)
                }
            }

            // Lire les calories
            val calories = HealthConnectHelper.readCaloriesRecords(context, hoursBack = 7 * 24)
            calories.forEach { record ->
                val entity = HealthDataEntity(
                    dataType = "calories",
                    value = record.energy.inKilocalories,
                    unit = "kcal",
                    timestamp = record.startTime.toEpochMilli(),
                    source = "health_connect"
                )
                dao.insert(entity)
            }

            // Lire la distance
            val distances = HealthConnectHelper.readDistanceRecords(context, hoursBack = 7 * 24)
            distances.forEach { record ->
                val entity = HealthDataEntity(
                    dataType = "distance",
                    value = record.distance.inMeters,
                    unit = "m",
                    timestamp = record.startTime.toEpochMilli(),
                    source = "health_connect"
                )
                dao.insert(entity)
            }

            // Lire l'hydratation
            val hydration = HealthConnectHelper.readHydrationRecords(context, hoursBack = 7 * 24)
            hydration.forEach { record ->
                val entity = HealthDataEntity(
                    dataType = "hydration",
                    value = record.volume.inMilliliters,
                    unit = "ml",
                    timestamp = record.startTime.toEpochMilli(),
                    source = "health_connect"
                )
                dao.insert(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Synchroniser les données VERS Health Connect
     * (Écrire nos données de santé pour les autres apps)
     */
    suspend fun syncToHealthConnect() {
        try {
            val unsyncedData = dao.getUnsynced()
            
            unsyncedData.forEach { data ->
                try {
                    when (data.dataType) {
                        "weight" -> {
                            HealthConnectHelper.writeWeight(context, data.value)
                            dao.markAsSynced(data.id, "health_connect")
                        }
                        "steps" -> {
                            HealthConnectHelper.writeSteps(
                                context,
                                data.value.toLong(),
                                Instant.ofEpochMilli(data.timestamp)
                            )
                            dao.markAsSynced(data.id, "health_connect")
                        }
                        "heart_rate" -> {
                            HealthConnectHelper.writeHeartRate(
                                context,
                                data.value.toLong(),
                                Instant.ofEpochMilli(data.timestamp)
                            )
                            dao.markAsSynced(data.id, "health_connect")
                        }
                        "calories" -> {
                            HealthConnectHelper.writeCalories(
                                context,
                                data.value,
                                Instant.ofEpochMilli(data.timestamp)
                            )
                            dao.markAsSynced(data.id, "health_connect")
                        }
                        "distance" -> {
                            HealthConnectHelper.writeDistance(
                                context,
                                data.value,
                                Instant.ofEpochMilli(data.timestamp)
                            )
                            dao.markAsSynced(data.id, "health_connect")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sync bidirectionnelle complète
     */
    suspend fun fullSync() {
        syncFromHealthConnect()
        syncToHealthConnect()
    }

    /**
     * Ajouter une donnée de santé (depuis saisie utilisateur ou scan)
     */
    suspend fun addHealthData(
        dataType: String,
        value: Double,
        unit: String,
        source: String = "user_input"
    ) {
        val entity = HealthDataEntity(
            dataType = dataType,
            value = value,
            unit = unit,
            timestamp = System.currentTimeMillis(),
            source = source,
            synced = false
        )
        dao.insert(entity)
        
        // Essayer de synchroniser immédiatement
        try {
            when (dataType) {
                "weight" -> HealthConnectHelper.writeWeight(context, value)
                "steps" -> HealthConnectHelper.writeSteps(context, value.toLong())
                "heart_rate" -> HealthConnectHelper.writeHeartRate(context, value.toLong())
                "calories" -> HealthConnectHelper.writeCalories(context, value)
                "distance" -> HealthConnectHelper.writeDistance(context, value)
            }
            dao.markAsSynced(entity.id, "health_connect")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Récupérer l'historique des données
     */
    suspend fun getHistory(dataType: String, limit: Int = 100): List<HealthDataEntity> {
        return dao.getByType(dataType, limit)
    }

    /**
     * Démarrer la synchronisation automatique périodique
     */
    fun startPeriodicSync(intervalMinutes: Long = 30) {
        scope.launch {
            while (isActive) {
                delay(intervalMinutes * 60 * 1000)
                fullSync()
            }
        }
    }

    /**
     * Arrêter la synchronisation
     */
    fun stopSync() {
        scope.cancel()
    }

    fun cleanup() {
        stopSync()
    }
}
