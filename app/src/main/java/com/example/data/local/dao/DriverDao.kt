package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Driver
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    @Query("SELECT * FROM drivers ORDER BY isActive DESC, name ASC")
    fun getAllDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getDriverById(id: Long): Driver?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: Driver): Long

    @Update
    suspend fun updateDriver(driver: Driver)

    @Delete
    suspend fun deleteDriver(driver: Driver)

    @Query("SELECT COUNT(*) FROM drivers")
    suspend fun getDriverCount(): Int
}
