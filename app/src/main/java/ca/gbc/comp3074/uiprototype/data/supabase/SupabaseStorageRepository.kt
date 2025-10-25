package ca.gbc.comp3074.uiprototype.data.supabase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Repository for managing file storage operations with Supabase Storage
 */
class SupabaseStorageRepository(private val context: Context) {
    
    private val TAG = "SupabaseStorageRepo"
    private val client = SupabaseClientManager.client
    
    companion object {
        const val AVATARS_BUCKET = "avatars"
        const val MAX_IMAGE_SIZE = 800 // pixels
        const val JPEG_QUALITY = 85
    }
    
    /**
     * Upload avatar image to Supabase Storage
     * Returns the public URL of the uploaded image
     */
    suspend fun uploadAvatar(userId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Read and process image
                val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Could not open image")
                
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                
                // Resize image to reduce size
                val resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE)
                
                // Convert to byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                
                // Clean up
                bitmap.recycle()
                resizedBitmap.recycle()
                
                // Generate unique filename with user folder for better security
                val fileName = "${userId}/avatar_${System.currentTimeMillis()}.jpg"
                
                // Upload to Supabase Storage
                val bucket = client.storage[AVATARS_BUCKET]
                bucket.upload(fileName, imageBytes, upsert = true)
                
                // Get public URL
                val publicUrl = bucket.publicUrl(fileName)
                
                Log.d(TAG, "Avatar uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Upload avatar error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete avatar from storage
     */
    suspend fun deleteAvatar(avatarUrl: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Extract filename from URL
                val fileName = avatarUrl.substringAfterLast("/")
                
                val bucket = client.storage[AVATARS_BUCKET]
                bucket.delete(fileName)
                
                Log.d(TAG, "Avatar deleted successfully: $fileName")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Delete avatar error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update user avatar - uploads new image and updates profile
     */
    suspend fun updateUserAvatar(userId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Get current profile to delete old avatar if exists
                val authRepo = SupabaseAuthRepository()
                val currentProfile = authRepo.getUserProfile(userId).getOrNull()
                
                // Upload new avatar
                val uploadResult = uploadAvatar(userId, imageUri)
                if (uploadResult.isFailure) {
                    return@withContext uploadResult
                }
                
                val newAvatarUrl = uploadResult.getOrThrow()
                
                // Update profile with new avatar URL
                val updateResult = authRepo.updateProfile(
                    userId,
                    ca.gbc.comp3074.uiprototype.data.supabase.models.UpdateProfileRequest(
                        avatarUrl = newAvatarUrl
                    )
                )
                
                if (updateResult.isFailure) {
                    // Rollback - delete uploaded image
                    deleteAvatar(newAvatarUrl)
                    return@withContext Result.failure(updateResult.exceptionOrNull()!!)
                }
                
                // Delete old avatar if exists
                currentProfile?.avatarUrl?.let { oldUrl ->
                    if (oldUrl.isNotEmpty()) {
                        deleteAvatar(oldUrl)
                    }
                }
                
                Result.success(newAvatarUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Update user avatar error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Generic file upload to any bucket
     */
    suspend fun uploadFile(bucketName: String, filePath: String, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Read and process image
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open file")
                
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                
                // Resize image to reduce size
                val resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE)
                
                // Convert to byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                
                // Clean up
                bitmap.recycle()
                resizedBitmap.recycle()
                
                // Upload to Supabase Storage
                val bucket = client.storage[bucketName]
                bucket.upload(filePath, imageBytes, upsert = true)
                
                // Get public URL
                val publicUrl = bucket.publicUrl(filePath)
                
                Log.d(TAG, "File uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Upload file error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Generic file deletion from any bucket
     */
    suspend fun deleteFile(bucketName: String, filePath: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val bucket = client.storage[bucketName]
                bucket.delete(filePath)
                
                Log.d(TAG, "File deleted successfully: $filePath")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Delete file error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Resize bitmap to fit within max size while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val scale = Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
