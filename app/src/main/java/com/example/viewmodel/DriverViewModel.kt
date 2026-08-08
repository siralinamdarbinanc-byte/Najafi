package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Driver
import com.example.data.repository.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DriverFormState(
    val driverId: Long? = null,
    val name: String = "",
    val phone: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val nameError: String? = null
)

data class DriverUiState(
    val isFormOpen: Boolean = false,
    val formState: DriverFormState = DriverFormState(),
    val driverToDelete: Driver? = null
)

class DriverViewModel(
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    val drivers: StateFlow<List<Driver>> = driverRepository.allDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun openAddDriverForm() {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                formState = DriverFormState()
            )
        }
    }

    fun openEditDriverForm(driver: Driver) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                formState = DriverFormState(
                    driverId = driver.id,
                    name = driver.name,
                    phone = driver.phone,
                    description = driver.description,
                    isActive = driver.isActive
                )
            )
        }
    }

    fun closeForm() {
        _uiState.update { it.copy(isFormOpen = false) }
    }

    fun onFormNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(formState = state.formState.copy(name = name, nameError = null))
        }
    }

    fun onFormPhoneChanged(phone: String) {
        _uiState.update { state ->
            state.copy(formState = state.formState.copy(phone = phone))
        }
    }

    fun onFormDescriptionChanged(desc: String) {
        _uiState.update { state ->
            state.copy(formState = state.formState.copy(description = desc))
        }
    }

    fun onFormIsActiveChanged(isActive: Boolean) {
        _uiState.update { state ->
            state.copy(formState = state.formState.copy(isActive = isActive))
        }
    }

    fun saveDriver() {
        val form = _uiState.value.formState
        if (form.name.isBlank()) {
            _uiState.update { state ->
                state.copy(formState = state.formState.copy(nameError = "نام پیک الزامی است"))
            }
            return
        }

        viewModelScope.launch {
            val driver = Driver(
                id = form.driverId ?: 0L,
                name = form.name.trim(),
                phone = form.phone.trim(),
                description = form.description.trim(),
                isActive = form.isActive
            )

            if (form.driverId == null) {
                driverRepository.insertDriver(driver)
            } else {
                driverRepository.updateDriver(driver)
            }

            closeForm()
        }
    }

    fun toggleDriverActiveStatus(driver: Driver) {
        viewModelScope.launch {
            val updated = driver.copy(isActive = !driver.isActive)
            driverRepository.updateDriver(updated)
        }
    }

    fun requestDeleteDriver(driver: Driver) {
        _uiState.update { it.copy(driverToDelete = driver) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(driverToDelete = null) }
    }

    fun confirmDeleteDriver() {
        val target = _uiState.value.driverToDelete ?: return
        viewModelScope.launch {
            driverRepository.deleteDriver(target)
            _uiState.update { it.copy(driverToDelete = null) }
        }
    }
}
