package com.fitai.tracker

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Energy
import java.time.Instant
import kotlin.reflect.KClass

object HealthConnectHelper {
    // Toutes les permissions READ & WRITE
    @Suppress("UNCHECKED_CAST")
    val permissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(WeightRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(StepsRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(StepsRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(HeartRateRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(HeartRateRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(NutritionRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(NutritionRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(HydrationRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(HydrationRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(DistanceRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(DistanceRecord::class as KClass<Record>),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class as KClass<Record>),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class as KClass<Record>),
    )

    fun isAvailable(context: Context): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun getClient(context: Context): HealthConnectClient? {
        return if (isAvailable(context)) HealthConnectClient.getOrCreate(context) else null
    }

    // ==================== WRITE OPERATIONS ====================
    
    suspend fun writeWeight(context: Context, weightKg: Double) {
        val client = getClient(context) ?: return
        val record = WeightRecord(
            weight = Mass.kilograms(weightKg),
            time = Instant.now(),
            zoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    suspend fun writeSteps(context: Context, steps: Long, timestamp: Instant = Instant.now()) {
        val client = getClient(context) ?: return
        val record = StepsRecord(
            count = steps,
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(3600), // 1 heure
            endZoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    suspend fun writeHeartRate(context: Context, bpm: Long, timestamp: Instant = Instant.now()) {
        val client = getClient(context) ?: return
        val record = HeartRateRecord(
            samples = listOf(
                HeartRateRecord.Sample(timestamp, bpm)
            ),
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(60),
            endZoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    suspend fun writeCalories(context: Context, caloriesBurned: Double, timestamp: Instant = Instant.now()) {
        val client = getClient(context) ?: return
        val record = TotalCaloriesBurnedRecord(
            energy = Energy.kilocalories(caloriesBurned),
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(3600),
            endZoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    suspend fun writeDistance(context: Context, distanceMeters: Double, timestamp: Instant = Instant.now()) {
        val client = getClient(context) ?: return
        val record = DistanceRecord(
            distance = Length.meters(distanceMeters),
            startTime = timestamp,
            startZoneOffset = null,
            endTime = timestamp.plusSeconds(3600),
            endZoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    // ==================== READ OPERATIONS ====================

    @Suppress("UNCHECKED_CAST")
    suspend fun readWeightRecords(context: Context, hoursBack: Int = 24): List<WeightRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = WeightRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<WeightRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readStepsRecords(context: Context, hoursBack: Int = 24): List<StepsRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<StepsRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readHeartRateRecords(context: Context, hoursBack: Int = 24): List<HeartRateRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<HeartRateRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readCaloriesRecords(context: Context, hoursBack: Int = 24): List<TotalCaloriesBurnedRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<TotalCaloriesBurnedRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readDistanceRecords(context: Context, hoursBack: Int = 24): List<DistanceRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = DistanceRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<DistanceRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readNutritionRecords(context: Context, hoursBack: Int = 24): List<NutritionRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<NutritionRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun readHydrationRecords(context: Context, hoursBack: Int = 24): List<HydrationRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds((hoursBack * 3600).toLong()),
                Instant.now()
            )
            val request = ReadRecordsRequest(
                recordType = HydrationRecord::class as KClass<Record>,
                timeRangeFilter = timeRangeFilter
            ) as ReadRecordsRequest<HydrationRecord>
            client.readRecords(request).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun requestPermissionsContract() = PermissionController.createRequestPermissionResultContract()
}
