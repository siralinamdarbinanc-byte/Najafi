package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.Delivery
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.FullscreenImageViewer
import com.example.ui.components.StatusChip
import com.example.utils.JalaliCalendarHelper
import com.example.viewmodel.DetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MissionDetailScreen(
    viewModel: DetailViewModel,
    deliveryId: Long,
    onNavigateToEdit: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val details by viewModel.deliveryDetails.collectAsStateWithLifecycle()

    LaunchedEffect(deliveryId) {
        viewModel.setDeliveryId(deliveryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات مأموریت", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    if (details != null) {
                        IconButton(onClick = { onNavigateToEdit(details!!.delivery.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (details == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("اطلاعات مأموریت بارگیری نشد", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val d = details!!.delivery
            val driver = details!!.driver
            val photos = details!!.photos

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Header Card with Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "کد مأموریت: #${d.id}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تاریخ: ${JalaliCalendarHelper.toPersianDigits(d.date)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        StatusChip(status = d.status)
                    }
                }

                // Driver & Address Details
                DetailSectionCard(title = "اطلاعات تحویل") {
                    DetailRowItem(
                        icon = Icons.Default.DirectionsBike,
                        label = "نام پیک",
                        value = driver?.name ?: "نامشخص (${driver?.phone ?: ""})"
                    )

                    DetailRowItem(
                        icon = Icons.Default.LocationOn,
                        label = "مقصد / آدرس",
                        value = d.destination
                    )

                    DetailRowItem(
                        icon = Icons.Default.Schedule,
                        label = "ساعت خروج",
                        value = JalaliCalendarHelper.toPersianDigits(d.departureTime)
                    )

                    if (d.returnTime.isNotBlank()) {
                        DetailRowItem(
                            icon = Icons.Default.Schedule,
                            label = "ساعت برگشت",
                            value = JalaliCalendarHelper.toPersianDigits(d.returnTime)
                        )
                    }
                }

                // Order & Financial Details
                DetailSectionCard(title = "اطلاعات سفارش و مبالغ") {
                    if (d.orderDescription.isNotBlank()) {
                        DetailRowItem(
                            icon = Icons.Default.Description,
                            label = "شرح سفارش",
                            value = d.orderDescription
                        )
                    }

                    DetailRowItem(
                        icon = Icons.Default.AttachMoney,
                        label = "مبلغ سفارش",
                        value = JalaliCalendarHelper.formatToman(d.orderAmount)
                    )

                    DetailRowItem(
                        icon = Icons.Default.AttachMoney,
                        label = "کرایه پیک",
                        value = JalaliCalendarHelper.formatToman(d.deliveryFee)
                    )

                    if (d.notes.isNotBlank()) {
                        DetailRowItem(
                            icon = Icons.Default.Notes,
                            label = "توضیحات تکمیلی",
                            value = d.notes
                        )
                    }
                }

                // Photos Section
                if (photos.isNotEmpty()) {
                    DetailSectionCard(title = "عکس‌های سفارش (${photos.size} عدد)") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(photos) { index, photo ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.openPhotoViewer(index) }
                                ) {
                                    AsyncImage(
                                        model = File(photo.localPath),
                                        contentDescription = "عکس سفارش",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Status Update Quick Action Buttons
                if (d.status == Delivery.STATUS_IN_PROGRESS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markAsCompleted() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تکمیل و ثبت برگشت")
                        }

                        OutlinedButton(
                            onClick = { viewModel.markAsCanceled() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("لغو مأموریت")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lightbox Fullscreen Photo Viewer Dialog
            if (state.selectedPhotoIndex != null) {
                FullscreenImageViewer(
                    photos = photos,
                    initialIndex = state.selectedPhotoIndex!!,
                    onDismiss = { viewModel.closePhotoViewer() }
                )
            }

            // Delete Confirm Dialog
            if (state.showDeleteConfirmDialog) {
                ConfirmDialog(
                    title = "حذف مأموریت",
                    message = "آیا از حذف کامل این مأموریت از سیستم اطمینان دارید؟",
                    onConfirm = {
                        viewModel.confirmDelete(onDeleted = onBack)
                    },
                    onDismiss = { viewModel.hideDeleteDialog() }
                )
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun DetailRowItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
