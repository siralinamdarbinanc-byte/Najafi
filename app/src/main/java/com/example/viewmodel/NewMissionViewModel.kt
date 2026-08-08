package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.Driver
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.DriverRepository
import com.example.utils.JalaliCalendarHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhotoItem(
    val id: Long = 0, // 0 for newly added photos
    val path: String
)

data class NewMissionUiState(
    val editingDeliveryId: Long? = null,
    val selectedDriverId: Long? = null,
    val date: String = JalaliCalendarHelper.getPersianDate(),
    val departureTime: String = JalaliCalendarHelper.getCurrentTimeString(),
    val returnTime: String = "",
    val destination: String = "",
    val orderDescription: String = "",
    val orderAmountStr: String = "",
    val isOrderAmountPaid: Boolean = true,
    val deliveryFeeStr: String = "",
    val isDeliveryFeePaid: Boolean = true,
    val status: String = Delivery.STATUS_IN_PROGRESS,
    val notes: String = "",
    val photos: List<PhotoItem> = emptyList(),
    val removedPhotoIds: List<Long> = emptyList(),
    val driverError: String? = null,
    val destinationError: String? = null,
    val isSaved: Boolean = false,
    val isQuickAddDriverDialogOpen: Boolean = false
)

class NewMissionViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewMissionUiState())
    val uiState: StateFlow<NewMissionUiState> = _uiState.asStateFlow()

    val activeDrivers: StateFlow<List<Driver>> = driverRepository.activeDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadMissionForEditing(deliveryId: Long) {
        viewModelScope.launch {
            val deliveryWithDetails = deliveryRepository.getDeliveryWithDetailsById(deliveryId).firstOrNull()
            if (deliveryWithDetails != null) {
                val d = deliveryWithDetails.delivery
                _uiState.update {
                    it.copy(
                        editingDeliveryId = d.id,
                        selectedDriverId = d.driverId,
                        date = d.date,
                        departureTime = d.departureTime,
                        returnTime = d.returnTime,
                        destination = d.destination,
                        orderDescription = d.orderDescription,
                        orderAmountStr = d.orderAmount?.toString() ?: "",
                        isOrderAmountPaid = d.isOrderAmountPaid,
                        deliveryFeeStr = d.deliveryFee?.toString() ?: "",
                        isDeliveryFeePaid = d.isDeliveryFeePaid,
                        status = d.status,
                        notes = d.notes,
                        photos = deliveryWithDetails.photos.map { p -> PhotoItem(id = p.id, path = p.localPath) }
                    )
                }
            }
        }
    }

    fun onDriverSelected(driverId: Long) {
        _uiState.update { it.copy(selectedDriverId = driverId, driverError = null) }
    }

    fun onDateChanged(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun onDepartureTimeChanged(time: String) {
        _uiState.update { it.copy(departureTime = time) }
    }

    fun onReturnTimeChanged(time: String) {
        _uiState.update { it.copy(returnTime = time) }
    }

    fun onDestinationChanged(destination: String) {
        _uiState.update { it.copy(destination = destination, destinationError = null) }
    }

    fun onOrderDescriptionChanged(desc: String) {
        _uiState.update { it.copy(orderDescription = desc) }
    }

    fun onOrderAmountChanged(amountStr: String) {
        if (amountStr.isEmpty() || amountStr.all { it.isDigit() }) {
            _uiState.update { it.copy(orderAmountStr = amountStr) }
        }
    }

    fun onDeliveryFeeChanged(feeStr: String) {
        if (feeStr.isEmpty() || feeStr.all { it.isDigit() }) {
            _uiState.update { it.copy(deliveryFeeStr = feeStr) }
        }
    }

    fun onOrderAmountPaidChanged(isPaid: Boolean) {
        _uiState.update { it.copy(isOrderAmountPaid = isPaid) }
    }

    fun onDeliveryFeePaidChanged(isPaid: Boolean) {
        _uiState.update { it.copy(isDeliveryFeePaid = isPaid) }
    }

    fun onStatusChanged(newStatus: String) {
        val currentReturnTime = _uiState.value.returnTime
        val updatedReturnTime = if (newStatus == Delivery.STATUS_COMPLETED && currentReturnTime.isBlank()) {
            JalaliCalendarHelper.getCurrentTimeString()
        } else currentReturnTime

        _uiState.update {
            it.copy(
                status = newStatus,
                returnTime = updatedReturnTime
            )
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun addPhotoPath(path: String) {
        _uiState.update {
            it.copy(photos = it.photos + PhotoItem(id = 0, path = path))
        }
    }

    fun removePhoto(photo: PhotoItem) {
        _uiState.update { state ->
            val updatedPhotos = state.photos.filterNot { it == photo }
            val updatedRemovedIds = if (photo.id > 0) state.removedPhotoIds + photo.id else state.removedPhotoIds
            state.copy(photos = updatedPhotos, removedPhotoIds = updatedRemovedIds)
        }
    }

    fun openQuickAddDriverDialog() {
        _uiState.update { it.copy(isQuickAddDriverDialogOpen = true) }
    }

    fun closeQuickAddDriverDialog() {
        _uiState.update { it.copy(isQuickAddDriverDialogOpen = false) }
    }

    fun quickAddDriver(name: String, phone: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val newDriver = Driver(name = name.trim(), phone = phone.trim(), isActive = true)
                val newId = driverRepository.insertDriver(newDriver)
                onDriverSelected(newId)
                closeQuickAddDriverDialog()
            }
        }
    }

    fun saveMission(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        var hasError = false
        var driverErr: String? = null
        var destErr: String? = null

        if (currentState.selectedDriverId == null) {
            driverErr = "لطفاً پیک را انتخاب کنید"
            hasError = true
        }

        if (currentState.destination.trim().isBlank()) {
            destErr = "لطفاً آدرس / مقصد را وارد کنید"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    driverError = driverErr,
                    destinationError = destErr
                )
            }
            return
        }

        viewModelScope.launch {
            val amount = currentState.orderAmountStr.toLongOrNull()
            val fee = currentState.deliveryFeeStr.toLongOrNull()

            val delivery = Delivery(
                id = currentState.editingDeliveryId ?: 0L,
                driverId = currentState.selectedDriverId!!,
                date = currentState.date.ifBlank { JalaliCalendarHelper.getPersianDate() },
                departureTime = currentState.departureTime.ifBlank { JalaliCalendarHelper.getCurrentTimeString() },
                returnTime = currentState.returnTime,
                destination = currentState.destination.trim(),
                orderDescription = currentState.orderDescription.trim(),
                orderAmount = amount,
                isOrderAmountPaid = currentState.isOrderAmountPaid,
                deliveryFee = fee,
                isDeliveryFeePaid = currentState.isDeliveryFeePaid,
                status = currentState.status,
                notes = currentState.notes.trim()
            )

            if (currentState.editingDeliveryId == null) {
                // Save new
                val newPhotoPaths = currentState.photos.map { it.path }
                deliveryRepository.saveDelivery(delivery, newPhotoPaths)
            } else {
                // Update existing
                val newPhotoPaths = currentState.photos.filter { it.id == 0L }.map { it.path }
                deliveryRepository.updateDelivery(delivery, newPhotoPaths, currentState.removedPhotoIds)
            }

            _uiState.update { it.copy(isSaved = true) }
            onSuccess()
        }
    }
}
