package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.DriverRepository

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val db by lazy { AppDatabase.getDatabase(context.applicationContext) }
    private val deliveryRepository by lazy { DeliveryRepository(db.deliveryDao()) }
    private val driverRepository by lazy { DriverRepository(db.driverDao()) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(deliveryRepository, driverRepository) as T
            }
            modelClass.isAssignableFrom(NewMissionViewModel::class.java) -> {
                NewMissionViewModel(deliveryRepository, driverRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(deliveryRepository, driverRepository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(deliveryRepository) as T
            }
            modelClass.isAssignableFrom(DriverViewModel::class.java) -> {
                DriverViewModel(driverRepository) as T
            }
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(deliveryRepository, driverRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
