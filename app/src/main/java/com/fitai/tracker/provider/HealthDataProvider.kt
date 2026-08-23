package com.fitai.tracker.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.fitai.tracker.db.HealthDataDatabase
import kotlinx.coroutines.runBlocking

/**
 * ContentProvider pour permettre aux autres apps de santé
 * de lire et écrire les données de Fit AI Tracker
 */
class HealthDataProvider : ContentProvider() {
    private var database: HealthDataDatabase? = null
    
    companion object {
        private const val AUTHORITY = "com.fitai.tracker.health"
        private const val WEIGHT = 1
        private const val STEPS = 2
        private const val HEART_RATE = 3
        private const val CALORIES = 4
        private const val DISTANCE = 5
        private const val HYDRATION = 6
        private const val ALL_DATA = 7
        
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "weight", WEIGHT)
            addURI(AUTHORITY, "weight/#", WEIGHT)
            addURI(AUTHORITY, "steps", STEPS)
            addURI(AUTHORITY, "steps/#", STEPS)
            addURI(AUTHORITY, "heart_rate", HEART_RATE)
            addURI(AUTHORITY, "heart_rate/#", HEART_RATE)
            addURI(AUTHORITY, "calories", CALORIES)
            addURI(AUTHORITY, "calories/#", CALORIES)
            addURI(AUTHORITY, "distance", DISTANCE)
            addURI(AUTHORITY, "distance/#", DISTANCE)
            addURI(AUTHORITY, "hydration", HYDRATION)
            addURI(AUTHORITY, "hydration/#", HYDRATION)
            addURI(AUTHORITY, "all", ALL_DATA)
        }
    }

    override fun onCreate(): Boolean {
        database = context?.let { HealthDataDatabase.getInstance(it) }
        return true
    }

    /**
     * Lire les données de santé
     */
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = database ?: return null
        val dao = db.healthDataDao()
        
        return runBlocking {
            val cursor = MatrixCursor(
                arrayOf("_id", "dataType", "value", "unit", "timestamp", "source", "synced")
            )

            when (uriMatcher.match(uri)) {
                WEIGHT -> {
                    val data = dao.getByType("weight", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                STEPS -> {
                    val data = dao.getByType("steps", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                HEART_RATE -> {
                    val data = dao.getByType("heart_rate", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                CALORIES -> {
                    val data = dao.getByType("calories", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                DISTANCE -> {
                    val data = dao.getByType("distance", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                HYDRATION -> {
                    val data = dao.getByType("hydration", limit = 100)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
                ALL_DATA -> {
                    val data = dao.getRecentData(limit = 500)
                    data.forEach { entity ->
                        cursor.addRow(
                            arrayOf(
                                entity.id, entity.dataType, entity.value,
                                entity.unit, entity.timestamp, entity.source, entity.synced
                            )
                        )
                    }
                }
            }
            cursor
        }
    }

    /**
     * Écrire des données de santé
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        
        val db = database ?: return null
        val dao = db.healthDataDao()

        return runBlocking {
            try {
                val dataType = values.getAsString("dataType") ?: return@runBlocking null
                val value = values.getAsDouble("value") ?: 0.0
                val unit = values.getAsString("unit") ?: ""
                val timestamp = values.getAsLong("timestamp") ?: System.currentTimeMillis()
                val source = values.getAsString("source") ?: "external_app"

                val entity = com.fitai.tracker.db.HealthDataEntity(
                    dataType = dataType,
                    value = value,
                    unit = unit,
                    timestamp = timestamp,
                    source = source,
                    synced = false
                )

                val id = dao.insert(entity)
                Uri.parse("content://$AUTHORITY/$dataType/$id")
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // Pas de mise à jour directe, la sync gère ça
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Pas de suppression depuis ContentProvider
        return 0
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            WEIGHT, STEPS, HEART_RATE, CALORIES, DISTANCE, HYDRATION, ALL_DATA -> {
                "vnd.android.cursor.dir/vnd.$AUTHORITY.data"
            }
            else -> null
        }
    }
}
