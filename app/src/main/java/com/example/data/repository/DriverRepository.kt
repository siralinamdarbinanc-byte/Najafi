package com.example.data.repository

import com.example.data.local.dao.DriverDao
import com.example.data.local.entity.Driver
import kotlinx.coroutines.flow.Flow

class DriverRepository(private val driverDao: DriverDao) {

    val allDrivers: Flow<List<Driver>> = driverDao.getAllDrivers()
    val activeDrivers: Flow<List<Driver>> = driverDao.getActiveDrivers()

    suspend fun getDriverById(id: Long): Driver? {
        return driverDao.getDriverById(id)
    }

    suspend fun insertDriver(driver: Driver): Long {
        return driverDao.insertDriver(driver)
    }

    suspend fun updateDriver(driver: Driver) {
        driverDao.updateDriver(driver)
    }

    suspend fun deleteDriver(driver: Driver) {
        driverDao.deleteDriver(driver)
    }
}
