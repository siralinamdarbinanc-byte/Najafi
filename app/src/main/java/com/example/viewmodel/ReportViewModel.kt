package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.Driver
import com.example.data.local.relation.DeliveryWithDetails
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.DriverRepository
import com.example.utils.JalaliCalendarHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class ReportTimeFrame(val title: String) {
    TODAY("امروز"),
    YESTERDAY("دیروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه"),
    ALL("همه زمان‌ها")
}

data class DriverReportSummary(
    val driverName: String,
    val totalMissions: Int,
    val completedMissions: Int,
    val totalFee: Long
)

data class ReportUiState(
    val selectedTimeFrame: ReportTimeFrame = ReportTimeFrame.TODAY,
    val totalMissionsCount: Int = 0,
    val completedMissionsCount: Int = 0,
    val canceledMissionsCount: Int = 0,
    val inProgressMissionsCount: Int = 0,
    val totalDeliveryFee: Long = 0L,
    val paidDeliveryFee: Long = 0L,
    val unpaidDeliveryFee: Long = 0L,
    val paidOrderAmount: Long = 0L,
    val unpaidOrderAmount: Long = 0L,
    val driverReports: List<DriverReportSummary> = emptyList()
)

class ReportViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _timeFrame = MutableStateFlow(ReportTimeFrame.TODAY)
    val timeFrame: StateFlow<ReportTimeFrame> = _timeFrame.asStateFlow()

    val reportUiState: StateFlow<ReportUiState> = combine(
        deliveryRepository.allDeliveriesWithDetails,
        driverRepository.allDrivers,
        _timeFrame
    ) { deliveries, drivers, frame ->
        val filteredDeliveries = deliveries.filter { item ->
            val dateStr = item.delivery.date
            when (frame) {
                ReportTimeFrame.TODAY -> JalaliCalendarHelper.isToday(dateStr)
                ReportTimeFrame.YESTERDAY -> JalaliCalendarHelper.isYesterday(dateStr)
                ReportTimeFrame.THIS_WEEK -> JalaliCalendarHelper.isThisWeek(dateStr)
                ReportTimeFrame.THIS_MONTH -> JalaliCalendarHelper.isThisMonth(dateStr)
                ReportTimeFrame.ALL -> true
            }
        }

        val total = filteredDeliveries.size
        val completed = filteredDeliveries.count { it.delivery.status == Delivery.STATUS_COMPLETED }
        val canceled = filteredDeliveries.count { it.delivery.status == Delivery.STATUS_CANCELED }
        val inProgress = filteredDeliveries.count { it.delivery.status == Delivery.STATUS_IN_PROGRESS }

        val nonCanceled = filteredDeliveries.filter { it.delivery.status != Delivery.STATUS_CANCELED }
        val totalFee = nonCanceled.sumOf { it.delivery.deliveryFee ?: 0L }
        val paidFee = nonCanceled.filter { it.delivery.isDeliveryFeePaid }.sumOf { it.delivery.deliveryFee ?: 0L }
        val unpaidFee = nonCanceled.filter { !it.delivery.isDeliveryFeePaid }.sumOf { it.delivery.deliveryFee ?: 0L }

        val paidAmount = nonCanceled.filter { it.delivery.isOrderAmountPaid }.sumOf { it.delivery.orderAmount ?: 0L }
        val unpaidAmount = nonCanceled.filter { !it.delivery.isOrderAmountPaid }.sumOf { it.delivery.orderAmount ?: 0L }

        // Group by Driver
        val driverMap = mutableMapOf<Long, MutableList<DeliveryWithDetails>>()
        for (item in filteredDeliveries) {
            val dId = item.delivery.driverId
            driverMap.getOrPut(dId) { mutableListOf() }.add(item)
        }

        val driverSummaries = drivers.map { driver ->
            val driverDeliveries = driverMap[driver.id] ?: emptyList()
            val driverTotal = driverDeliveries.size
            val driverCompleted = driverDeliveries.count { it.delivery.status == Delivery.STATUS_COMPLETED }
            val driverFee = driverDeliveries
                .filter { it.delivery.status != Delivery.STATUS_CANCELED }
                .sumOf { it.delivery.deliveryFee ?: 0L }

            DriverReportSummary(
                driverName = driver.name,
                totalMissions = driverTotal,
                completedMissions = driverCompleted,
                totalFee = driverFee
            )
        }.filter { it.totalMissions > 0 || frame == ReportTimeFrame.TODAY }

        ReportUiState(
            selectedTimeFrame = frame,
            totalMissionsCount = total,
            completedMissionsCount = completed,
            canceledMissionsCount = canceled,
            inProgressMissionsCount = inProgress,
            totalDeliveryFee = totalFee,
            paidDeliveryFee = paidFee,
            unpaidDeliveryFee = unpaidFee,
            paidOrderAmount = paidAmount,
            unpaidOrderAmount = unpaidAmount,
            driverReports = driverSummaries
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun onTimeFrameSelected(frame: ReportTimeFrame) {
        _timeFrame.value = frame
    }
}
