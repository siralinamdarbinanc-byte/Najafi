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
import kotlinx.coroutines.launch

enum class DateFilter(val title: String) {
    ALL("همه تواریخ"),
    TODAY("امروز"),
    YESTERDAY("دیروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه")
}

enum class StatusFilter(val title: String, val statusCode: String?) {
    ALL("همه وضعیت‌ها", null),
    IN_PROGRESS("در حال انجام", Delivery.STATUS_IN_PROGRESS),
    COMPLETED("انجام شد", Delivery.STATUS_COMPLETED),
    CANCELED("لغو شد", Delivery.STATUS_CANCELED)
}

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedDriverId: Long? = null,
    val selectedDateFilter: DateFilter = DateFilter.ALL,
    val selectedStatusFilter: StatusFilter = StatusFilter.ALL,
    val deliveryToDelete: DeliveryWithDetails? = null
)

class HistoryViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    val drivers: StateFlow<List<Driver>> = driverRepository.allDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredDeliveries: StateFlow<List<DeliveryWithDetails>> = combine(
        deliveryRepository.allDeliveriesWithDetails,
        _uiState
    ) { all, state ->
        all.filter { item ->
            val d = item.delivery

            // Search filter
            val query = state.searchQuery.trim()
            val matchesSearch = query.isBlank() ||
                    d.destination.contains(query, ignoreCase = true) ||
                    d.orderDescription.contains(query, ignoreCase = true) ||
                    d.notes.contains(query, ignoreCase = true) ||
                    (item.driver?.name?.contains(query, ignoreCase = true) == true)

            // Driver filter
            val matchesDriver = state.selectedDriverId == null || d.driverId == state.selectedDriverId

            // Date filter
            val matchesDate = when (state.selectedDateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> JalaliCalendarHelper.isToday(d.date)
                DateFilter.YESTERDAY -> JalaliCalendarHelper.isYesterday(d.date)
                DateFilter.THIS_WEEK -> JalaliCalendarHelper.isThisWeek(d.date)
                DateFilter.THIS_MONTH -> JalaliCalendarHelper.isThisMonth(d.date)
            }

            // Status filter
            val matchesStatus = state.selectedStatusFilter.statusCode == null || d.status == state.selectedStatusFilter.statusCode

            matchesSearch && matchesDriver && matchesDate && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDriverFilterSelected(driverId: Long?) {
        _uiState.update { it.copy(selectedDriverId = driverId) }
    }

    fun onDateFilterSelected(filter: DateFilter) {
        _uiState.update { it.copy(selectedDateFilter = filter) }
    }

    fun onStatusFilterSelected(filter: StatusFilter) {
        _uiState.update { it.copy(selectedStatusFilter = filter) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedDriverId = null,
                selectedDateFilter = DateFilter.ALL,
                selectedStatusFilter = StatusFilter.ALL
            )
        }
    }

    fun requestDeleteDelivery(delivery: DeliveryWithDetails) {
        _uiState.update { it.copy(deliveryToDelete = delivery) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(deliveryToDelete = null) }
    }

    fun confirmDeleteDelivery() {
        val target = _uiState.value.deliveryToDelete ?: return
        viewModelScope.launch {
            deliveryRepository.deleteDelivery(target)
            _uiState.update { it.copy(deliveryToDelete = null) }
        }
    }

    fun quickMarkAsCompleted(deliveryId: Long) {
        viewModelScope.launch {
            val returnTime = JalaliCalendarHelper.getCurrentTimeString()
            deliveryRepository.updateStatus(deliveryId, Delivery.STATUS_COMPLETED, returnTime)
        }
    }
}
