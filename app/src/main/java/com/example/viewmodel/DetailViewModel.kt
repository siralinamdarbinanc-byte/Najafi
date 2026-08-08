package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Delivery
import com.example.data.local.relation.DeliveryWithDetails
import com.example.data.repository.DeliveryRepository
import com.example.utils.JalaliCalendarHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val selectedPhotoIndex: Int? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val isDeleted: Boolean = false
)

class DetailViewModel(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _deliveryId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val deliveryDetails: StateFlow<DeliveryWithDetails?> = _deliveryId
        .flatMapLatest { id ->
            if (id != null) {
                deliveryRepository.getDeliveryWithDetailsById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setDeliveryId(id: Long) {
        _deliveryId.value = id
    }

    fun openPhotoViewer(index: Int) {
        _uiState.update { it.copy(selectedPhotoIndex = index) }
    }

    fun closePhotoViewer() {
        _uiState.update { it.copy(selectedPhotoIndex = null) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun markAsCompleted() {
        val details = deliveryDetails.value ?: return
        viewModelScope.launch {
            val currentTime = JalaliCalendarHelper.getCurrentTimeString()
            deliveryRepository.updateStatus(details.delivery.id, Delivery.STATUS_COMPLETED, currentTime)
        }
    }

    fun markAsCanceled() {
        val details = deliveryDetails.value ?: return
        viewModelScope.launch {
            deliveryRepository.updateStatus(details.delivery.id, Delivery.STATUS_CANCELED, details.delivery.returnTime)
        }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val details = deliveryDetails.value ?: return
        viewModelScope.launch {
            deliveryRepository.deleteDelivery(details)
            _uiState.update { it.copy(showDeleteConfirmDialog = false, isDeleted = true) }
            onDeleted()
        }
    }
}
