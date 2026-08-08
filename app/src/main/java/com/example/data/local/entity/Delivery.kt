package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = Driver::class,
            parentColumns = ["id"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("driverId")]
)
data class Delivery(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val driverId: Long,
    val date: String, // format: YYYY/MM/DD (Persian or Gregorian standard)
    val departureTime: String, // format: HH:mm
    val returnTime: String = "", // format: HH:mm
    val destination: String,
    val orderDescription: String = "",
    val orderAmount: Long? = null,
    val isOrderAmountPaid: Boolean = true,
    val deliveryFee: Long? = null,
    val isDeliveryFeePaid: Boolean = true,
    val status: String = STATUS_IN_PROGRESS, // IN_PROGRESS, COMPLETED, CANCELED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELED = "CANCELED"
    }
}
