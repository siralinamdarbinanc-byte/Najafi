package com.example.data.repository

import com.example.data.local.dao.DeliveryDao
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.DeliveryPhoto
import com.example.data.local.relation.DeliveryWithDetails
import com.example.utils.PhotoManager
import kotlinx.coroutines.flow.Flow

class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    val allDeliveriesWithDetails: Flow<List<DeliveryWithDetails>> =
        deliveryDao.getAllDeliveriesWithDetails()

    fun getRecentDeliveries(limit: Int = 5): Flow<List<DeliveryWithDetails>> {
        return deliveryDao.getRecentDeliveries(limit)
    }

    fun getDeliveryWithDetailsById(id: Long): Flow<DeliveryWithDetails?> {
        return deliveryDao.getDeliveryWithDetailsById(id)
    }

    /**
     * Inserts a new delivery along with photo local paths
     */
    suspend fun saveDelivery(delivery: Delivery, photoPaths: List<String>): Long {
        val deliveryId = deliveryDao.insertDelivery(delivery)
        for (path in photoPaths) {
            deliveryDao.insertPhoto(
                DeliveryPhoto(
                    deliveryId = deliveryId,
                    localPath = path
                )
            )
        }
        return deliveryId
    }

    /**
     * Updates an existing delivery, adding new photo paths and deleting removed photos
     */
    suspend fun updateDelivery(
        delivery: Delivery,
        newPhotoPaths: List<String>,
        removedPhotoIds: List<Long>
    ) {
        deliveryDao.updateDelivery(delivery)

        // Handle removed photos
        for (photoId in removedPhotoIds) {
            // Get photo path to delete from storage
            val existingPhotos = deliveryDao.getPhotosForDeliverySync(delivery.id)
            val photoToDelete = existingPhotos.find { it.id == photoId }
            if (photoToDelete != null) {
                PhotoManager.deletePhotoFile(photoToDelete.localPath)
                deliveryDao.deletePhotoById(photoId)
            }
        }

        // Add new photos
        for (path in newPhotoPaths) {
            deliveryDao.insertPhoto(
                DeliveryPhoto(
                    deliveryId = delivery.id,
                    localPath = path
                )
            )
        }
    }

    /**
     * Deletes a delivery and deletes all associated photo files from physical disk
     */
    suspend fun deleteDelivery(deliveryWithDetails: DeliveryWithDetails) {
        // Delete photo files from disk
        for (photo in deliveryWithDetails.photos) {
            PhotoManager.deletePhotoFile(photo.localPath)
        }
        deliveryDao.deleteDelivery(deliveryWithDetails.delivery)
    }

    /**
     * Updates mission status and return time
     */
    suspend fun updateStatus(deliveryId: Long, status: String, returnTime: String) {
        deliveryDao.updateDeliveryStatus(deliveryId, status, returnTime)
    }

    suspend fun updateDeliveryFeePaymentStatus(deliveryId: Long, isPaid: Boolean) {
        deliveryDao.updateDeliveryFeePaymentStatus(deliveryId, isPaid)
    }

    suspend fun updateOrderAmountPaymentStatus(deliveryId: Long, isPaid: Boolean) {
        deliveryDao.updateOrderAmountPaymentStatus(deliveryId, isPaid)
    }
}
