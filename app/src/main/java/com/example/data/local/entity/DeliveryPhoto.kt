package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_photos",
    foreignKeys = [
        ForeignKey(
            entity = Delivery::class,
            parentColumns = ["id"],
            childColumns = ["deliveryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deliveryId")]
)
data class DeliveryPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deliveryId: Long,
    val localPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
