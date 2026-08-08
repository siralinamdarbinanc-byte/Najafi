package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.DeliveryPhoto
import com.example.data.local.relation.DeliveryWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {

    @Transaction
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun getAllDeliveriesWithDetails(): Flow<List<DeliveryWithDetails>>

    @Transaction
    @Query("SELECT * FROM deliveries WHERE id = :id")
    fun getDeliveryWithDetailsById(id: Long): Flow<DeliveryWithDetails?>

    @Transaction
    @Query("SELECT * FROM deliveries WHERE id = :id")
    suspend fun getDeliveryWithDetailsByIdSync(id: Long): DeliveryWithDetails?

    @Transaction
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDeliveries(limit: Int): Flow<List<DeliveryWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery): Long

    @Update
    suspend fun updateDelivery(delivery: Delivery)

    @Delete
    suspend fun deleteDelivery(delivery: Delivery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: DeliveryPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: DeliveryPhoto)

    @Query("DELETE FROM delivery_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    @Query("DELETE FROM delivery_photos WHERE deliveryId = :deliveryId")
    suspend fun deletePhotosForDelivery(deliveryId: Long)

    @Query("SELECT * FROM delivery_photos WHERE deliveryId = :deliveryId")
    suspend fun getPhotosForDeliverySync(deliveryId: Long): List<DeliveryPhoto>

    @Query("UPDATE deliveries SET status = :status, returnTime = :returnTime, updatedAt = :updatedAt WHERE id = :deliveryId")
    suspend fun updateDeliveryStatus(deliveryId: Long, status: String, returnTime: String, updatedAt: Long = System.currentTimeMillis())
}
