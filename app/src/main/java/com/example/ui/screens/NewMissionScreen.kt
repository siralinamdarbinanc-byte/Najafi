package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Delivery
import com.example.ui.components.PhotoPicker
import com.example.ui.components.QuickAddDriverDialog
import com.example.utils.JalaliCalendarHelper
import com.example.viewmodel.NewMissionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewMissionScreen(
    viewModel: NewMissionViewModel,
    deliveryIdToEdit: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeDrivers by viewModel.activeDrivers.collectAsStateWithLifecycle()

    LaunchedEffect(deliveryIdToEdit) {
        if (deliveryIdToEdit != null && deliveryIdToEdit > 0) {
            viewModel.loadMissionForEditing(deliveryIdToEdit)
        }
    }

    var isDriverDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.editingDeliveryId == null) "ثبت مأموریت جدید" else "ویرایش مأموریت",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Driver Selection
            Column {
                Text(
                    text = "انتخاب پیک *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = isDriverDropdownExpanded,
                        onExpandedChange = { isDriverDropdownExpanded = !isDriverDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        val selectedDriverName = activeDrivers.find { it.id == state.selectedDriverId }?.name
                            ?: "یک پیک انتخاب کنید"

                        OutlinedTextField(
                            value = selectedDriverName,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsBike, contentDescription = null)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDriverDropdownExpanded)
                            },
                            isError = state.driverError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isDriverDropdownExpanded,
                            onDismissRequest = { isDriverDropdownExpanded = false }
                        ) {
                            if (activeDrivers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("هیچ پیک فعالی یافت نشد") },
                                    onClick = {}
                                )
                            } else {
                                activeDrivers.forEach { driver ->
                                    DropdownMenuItem(
                                        text = { Text(driver.name, fontWeight = FontWeight.SemiBold) },
                                        onClick = {
                                            viewModel.onDriverSelected(driver.id)
                                            isDriverDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.openQuickAddDriverDialog() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن پیک")
                    }
                }

                if (state.driverError != null) {
                    Text(
                        text = state.driverError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // 2. Date & Departure Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.date,
                    onValueChange = { viewModel.onDateChanged(it) },
                    label = { Text("تاریخ (شمسی)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.departureTime,
                    onValueChange = { viewModel.onDepartureTimeChanged(it) },
                    label = { Text("ساعت خروج") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 3. Destination / Address *
            OutlinedTextField(
                value = state.destination,
                onValueChange = { viewModel.onDestinationChanged(it) },
                label = { Text("مقصد / آدرس کامل *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                isError = state.destinationError != null,
                supportingText = state.destinationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // 4. Order Description
            OutlinedTextField(
                value = state.orderDescription,
                onValueChange = { viewModel.onOrderDescriptionChanged(it) },
                label = { Text("شرح سفارش") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // 5. Order Photos
            Column {
                Text(
                    text = "عکس سفارش",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                PhotoPicker(
                    photos = state.photos,
                    onPhotoAdded = { viewModel.addPhotoPath(it) },
                    onPhotoRemoved = { viewModel.removePhoto(it) }
                )
            }

            // 6. Amounts: Order Amount & Delivery Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.orderAmountStr,
                    onValueChange = { viewModel.onOrderAmountChanged(it) },
                    label = { Text("مبلغ سفارش (تومان)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.deliveryFeeStr,
                    onValueChange = { viewModel.onDeliveryFeeChanged(it) },
                    label = { Text("کرایه پیک (تومان)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 7. Mission Status
            Column {
                Text(
                    text = "وضعیت مأموریت",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf(
                        Delivery.STATUS_IN_PROGRESS to "در حال انجام",
                        Delivery.STATUS_COMPLETED to "انجام شد",
                        Delivery.STATUS_CANCELED to "لغو شد"
                    )

                    statusOptions.forEach { (statusCode, label) ->
                        val isSelected = state.status == statusCode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusChanged(statusCode) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 8. Return Time
            OutlinedTextField(
                value = state.returnTime,
                onValueChange = { viewModel.onReturnTimeChanged(it) },
                label = { Text("ساعت برگشت (در صورت تکمیل)") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 9. Notes / Explanations
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onNotesChanged(it) },
                label = { Text("توضیحات تکمیلی") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.saveMission(onSuccess = onBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (state.editingDeliveryId == null) "ثبت مأموریت" else "ذخیره تغییرات",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Quick Add Driver Dialog
        if (state.isQuickAddDriverDialogOpen) {
            QuickAddDriverDialog(
                onDismiss = { viewModel.closeQuickAddDriverDialog() },
                onAdd = { name, phone -> viewModel.quickAddDriver(name, phone) }
            )
        }
    }
}
