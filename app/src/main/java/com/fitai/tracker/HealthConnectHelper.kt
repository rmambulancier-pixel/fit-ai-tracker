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

object HealthConnectHelper {
    val permissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
    )

    fun isAvailable(context: Context): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun getClient(context: Context): HealthConnectClient? {
        return if (isAvailable(context)) HealthConnectClient.getOrCreate(context) else null
    }

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
            endTime = timestamp.plusSeconds(3600),
            endZoneOffset = null
        )
        client.insertRecords(listOf(record))
    }

    suspend fun writeHeartRate(context: Context, bpm: Long, timestamp: Instant = Instant.now()) {
        val client = getClient(context) ?: return
        val record = HeartRateRecord(
            samples = listOf(HeartRateRecord.Sample(timestamp, bpm)),
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

    suspend fun readWeightRecords(context: Context, hoursBack: Int = 24): List<WeightRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readStepsRecords(context: Context, hoursBack: Int = 24): List<StepsRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readHeartRateRecords(context: Context, hoursBack: Int = 24): List<HeartRateRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readCaloriesRecords(context: Context, hoursBack: Int = 24): List<TotalCaloriesBurnedRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readDistanceRecords(context: Context, hoursBack: Int = 24): List<DistanceRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = DistanceRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readNutritionRecords(context: Context, hoursBack: Int = 24): List<NutritionRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readHydrationRecords(context: Context, hoursBack: Int = 24): List<HydrationRecord> {
        val client = getClient(context) ?: return emptyList()
        return try {
            val request = ReadRecordsRequest(
                recordType = HydrationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minusSeconds((hoursBack * 3600).toLong()), Instant.now()
                )
            )
            client.readRecords(request).records
        } catch (e: Exception) { emptyList() }
    }

    fun requestPermissionsContract() = PermissionController.createRequestPermissionResultContract()
}
