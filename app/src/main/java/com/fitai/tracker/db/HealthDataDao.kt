package com.fitai.tracker.db

import androidx.room.*

@Dao
interface HealthDataDao {
    @Insert
    suspend fun insert(healthData: HealthDataEntity)

    @Insert
    suspend fun insertAll(healthDataList: List<HealthDataEntity>)

    @Update
    suspend fun update(healthData: HealthDataEntity)

    @Delete
    suspend fun delete(healthData: HealthDataEntity)

    @Query("SELECT * FROM health_data WHERE dataType = :dataType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByType(dataType: String, limit: Int = 100): List<HealthDataEntity>

    @Query("SELECT * FROM health_data WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getByTimeRange(startTime: Long, endTime: Long): List<HealthDataEntity>

    @Query("SELECT * FROM health_data WHERE synced = 0")
    suspend fun getUnsynced(): List<HealthDataEntity>

    @Query("SELECT * FROM health_data WHERE synced = 0 AND dataType = :dataType")
    suspend fun getUnsyncedByType(dataType: String): List<HealthDataEntity>

    @Query("UPDATE health_data SET synced = 1, syncedTo = :syncedTo WHERE id = :id")
    suspend fun markAsSynced(id: Long, syncedTo: String)

    @Query("DELETE FROM health_data WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    @Query("SELECT * FROM health_data ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentData(limit: Int = 50): List<HealthDataEntity>

    @Query("SELECT * FROM health_data WHERE source = :source ORDER BY timestamp DESC")
    suspend fun getBySource(source: String): List<HealthDataEntity>
}
