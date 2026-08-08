package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.DeliveryCard
import com.example.viewmodel.DateFilter
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.StatusFilter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val deliveries by viewModel.filteredDeliveries.collectAsStateWithLifecycle()

    var isDriverFilterExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سوابق مأموریت‌ها", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
        ) {
            // Search Input
            PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجو در آدرس، شرح، یادداشت یا نام پیک...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Filters Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Driver filter dropdown
                ExposedDropdownMenuBox(
                    expanded = isDriverFilterExpanded,
                    onExpandedChange = { isDriverFilterExpanded = !isDriverFilterExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedDriverName = drivers.find { it.id == state.selectedDriverId }?.name ?: "همه پیک‌ها"

                    OutlinedTextField(
                        value = "فیلتر پیک: $selectedDriverName",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.DirectionsBike, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDriverFilterExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isDriverFilterExpanded,
                        onDismissRequest = { isDriverFilterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("همه پیک‌ها", fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.onDriverFilterSelected(null)
                                isDriverFilterExpanded = false
                            }
                        )
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text(driver.name) },
                                onClick = {
                                    viewModel.onDriverFilterSelected(driver.id)
                                    isDriverFilterExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(DateFilter.entries) { filter ->
                        val isSelected = state.selectedDateFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDateFilterSelected(filter) },
                            label = { Text(filter.title, fontSize = 12.sp) }
                        )
                    }
                }

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(StatusFilter.entries) { filter ->
                        val isSelected = state.selectedStatusFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusFilterSelected(filter) },
                            label = { Text(filter.title, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deliveries List
            if (deliveries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "مأموریتی با این مشخصات یافت نشد",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deliveries, key = { it.delivery.id }) { item ->
                        DeliveryCard(
                            deliveryWithDetails = item,
                            onClick = { onNavigateToDetail(item.delivery.id) },
                            onDeleteClick = { viewModel.requestDeleteDelivery(item) },
                            onQuickMarkCompleted = { viewModel.quickMarkAsCompleted(item.delivery.id) }
                        )
                    }
                }
            }
        }

        // Delete Confirm Dialog
        if (state.deliveryToDelete != null) {
            ConfirmDialog(
                title = "حذف مأموریت",
                message = "آیا از حذف مأموریت مربوط به «${state.deliveryToDelete!!.delivery.destination}» اطمینان دارید؟",
                onConfirm = { viewModel.confirmDeleteDelivery() },
                onDismiss = { viewModel.dismissDeleteDialog() }
            )
        }
    }
}
