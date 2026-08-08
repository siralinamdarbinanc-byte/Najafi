package com.example.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.DeliveryPhoto
import com.example.data.local.entity.Driver

data class DeliveryWithDetails(
    @Embedded val delivery: Delivery,

    @Relation(
        parentColumn = "driverId",
        entityColumn = "id"
    )
    val driver: Driver?,

    @Relation(
        parentColumn = "id",
        entityColumn = "deliveryId"
    )
    val photos: List<DeliveryPhoto>
)
