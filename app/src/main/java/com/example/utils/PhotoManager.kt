package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoManager {

    private const val PHOTO_DIR_NAME = "delivery_photos"

    /**
     * Gets or creates the directory for storing delivery photos inside app internal storage.
     */
    fun getPhotoDirectory(context: Context): File {
        val dir = File(context.filesDir, PHOTO_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates a temp file for camera capture.
     */
    fun createTempCameraFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "JPEG_${timeStamp}_"
        val storageDir = getPhotoDirectory(context)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    /**
     * Copies image from a Uri (from gallery or camera) into internal app directory
     * and returns the absolute local path string.
     */
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val file = File(getPhotoDirectory(context), "IMG_${timeStamp}.jpg")

            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a local photo file from storage given its file path string.
     */
    fun deletePhotoFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Loads a downsampled bitmap thumbnail from local file path to conserve memory.
     */
    fun loadThumbnail(path: String, reqWidth: Int = 200, reqHeight: Int = 200): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
