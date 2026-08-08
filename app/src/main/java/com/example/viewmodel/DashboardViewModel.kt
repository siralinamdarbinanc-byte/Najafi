package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Delivery
import com.example.data.local.relation.DeliveryWithDetails
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.DriverRepository
import com.example.utils.JalaliCalendarHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardMetrics(
    val todayTotalMissions: Int = 0,
    val todayCompletedMissions: Int = 0,
    val todayInProgressMissions: Int = 0,
    val todayTotalDeliveryFee: Long = 0L
)

class DashboardViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    val recentMissions: StateFlow<List<DeliveryWithDetails>> =
        deliveryRepository.getRecentDeliveries(5)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val todayMetrics: StateFlow<DashboardMetrics> =
        deliveryRepository.allDeliveriesWithDetails
            .map { list ->
                val todayDateStr = JalaliCalendarHelper.getPersianDate()
                val todayDeliveries = list.filter { it.delivery.date == todayDateStr }

                val completed = todayDeliveries.count { it.delivery.status == Delivery.STATUS_COMPLETED }
                val inProgress = todayDeliveries.count { it.delivery.status == Delivery.STATUS_IN_PROGRESS }
                val totalFee = todayDeliveries
                    .filter { it.delivery.status != Delivery.STATUS_CANCELED }
                    .sumOf { it.delivery.deliveryFee ?: 0L }

                DashboardMetrics(
                    todayTotalMissions = todayDeliveries.size,
                    todayCompletedMissions = completed,
                    todayInProgressMissions = inProgress,
                    todayTotalDeliveryFee = totalFee
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardMetrics()
            )
}
