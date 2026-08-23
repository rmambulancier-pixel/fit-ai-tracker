package com.fitai.tracker

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import java.time.Instant

object HealthConnectHelper {
    val permissions = setOf(
        HealthPermission.getWritePermission(WeightRecord::class)
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

    fun requestPermissionsContract() = PermissionController.createRequestPermissionResultContract()
}
