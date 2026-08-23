package com.fitai.tracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dataType: String, // "weight", "steps", "heart_rate", "calories", "distance", "nutrition", "hydration"
    val value: Double,
    val unit: String, // "kg", "steps", "bpm", "kcal", "m", "ml", etc.
    val timestamp: Long,
    val source: String, // "user_input", "health_connect", "fit_app", "strava", etc.
    val synced: Boolean = false,
    val syncedTo: String = "" // "health_connect", "fit_app", "strava", etc.
)
