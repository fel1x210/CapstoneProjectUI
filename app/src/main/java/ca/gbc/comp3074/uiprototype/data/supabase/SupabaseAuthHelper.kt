package ca.gbc.comp3074.uiprototype.data.supabase

import ca.gbc.comp3074.uiprototype.data.supabase.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Java-friendly wrapper for Supabase authentication
 */
class SupabaseAuthHelper {
    private val authRepository = SupabaseAuthRepository()

    interface AuthCallback {
        fun onSuccess(profile: UserProfile)
        fun onError(error: String)
    }

    interface SignUpCallback {
        fun onSuccess()
        fun onError(error: String)
    }

    fun signIn(email: String, password: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.signIn(email, password)
                }
                result.onSuccess { profile ->
                    callback.onSuccess(profile)
                }.onFailure { error ->
                    callback.onError(error.message ?: "Login failed")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Login failed")
            }
        }
    }

    fun signUp(email: String, password: String, fullName: String, callback: SignUpCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.signUp(email, password, fullName)
                }
                result.onSuccess {
                    callback.onSuccess()
                }.onFailure { error ->
                    callback.onError(error.message ?: "Sign up failed")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Sign up failed")
            }
        }
    }

    fun signOut(callback: SignUpCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.signOut()
                }
                result.onSuccess {
                    callback.onSuccess()
                }.onFailure { error ->
                    callback.onError(error.message ?: "Sign out failed")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Sign out failed")
            }
        }
    }
    
    /**
     * Check if user is currently logged in (with callback for async handling)
     */
    fun checkIfUserLoggedIn(callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val isLoggedIn = withContext(Dispatchers.IO) {
                    SupabaseClientManager.isUserAuthenticated()
                }
                callback(isLoggedIn)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    /**
     * Waits for potential session restoration and checks login status
     * Useful for app startup where session might be loading asynchronously
     */
    fun waitForSessionAndCheck(callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Try up to 5 times with 300ms delay (1.5s max wait)
                // This gives enough time for the Auth plugin to restore session from disk
                var isLoggedIn = false
                var attempts = 0
                while (attempts < 5) {
                    isLoggedIn = withContext(Dispatchers.IO) {
                        SupabaseClientManager.isUserAuthenticated()
                    }
                    if (isLoggedIn) break
                    
                    kotlinx.coroutines.delay(300)
                    attempts++
                }
                callback(isLoggedIn)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun getCurrentUserProfile(callback: AuthCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // First get the current user ID
                val userId = withContext(Dispatchers.IO) {
                    SupabaseClientManager.getCurrentUserId()
                }
                
                if (userId == null) {
                    callback.onError("No user logged in")
                    return@launch
                }
                
                // Then get the profile
                val result = withContext(Dispatchers.IO) {
                    authRepository.getUserProfile(userId)
                }
                result.onSuccess { profile ->
                    callback.onSuccess(profile)
                }.onFailure { error ->
                    callback.onError(error.message ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(newName: String, callback: SignUpCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get current user ID
                val userId = withContext(Dispatchers.IO) {
                    SupabaseClientManager.getCurrentUserId()
                }
                
                if (userId == null) {
                    callback.onError("No user logged in")
                    return@launch
                }
                
                // Create update request
                val updateRequest = ca.gbc.comp3074.uiprototype.data.supabase.models.UpdateProfileRequest(
                    fullName = newName
                )
                
                val result = withContext(Dispatchers.IO) {
                    authRepository.updateProfile(userId, updateRequest)
                }
                result.onSuccess {
                    callback.onSuccess()
                }.onFailure { error ->
                    callback.onError(error.message ?: "Failed to update profile")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Failed to update profile")
            }
        }
    }

    interface AvatarCallback {
        fun onSuccess(avatarUrl: String)
        fun onError(error: String)
    }

    fun uploadAvatar(context: android.content.Context, imageUri: android.net.Uri, callback: AvatarCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get current user ID
                val userId = withContext(Dispatchers.IO) {
                    SupabaseClientManager.getCurrentUserId()
                }
                
                if (userId == null) {
                    callback.onError("No user logged in")
                    return@launch
                }
                
                // Use updateUserAvatar which uploads AND updates the profile
                val storageRepo = ca.gbc.comp3074.uiprototype.data.supabase.SupabaseStorageRepository(context)
                val result = withContext(Dispatchers.IO) {
                    storageRepo.updateUserAvatar(userId, imageUri)
                }
                result.onSuccess { avatarUrl ->
                    callback.onSuccess(avatarUrl)
                }.onFailure { error ->
                    callback.onError(error.message ?: "Failed to upload avatar")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Failed to upload avatar")
            }
        }
    }

    interface SimpleCallback {
        fun onSuccess()
        fun onError(error: String)
    }

    fun changePassword(currentPassword: String, newPassword: String, callback: SimpleCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.changePassword(currentPassword, newPassword)
                }
                result.onSuccess {
                    callback.onSuccess()
                }.onFailure { error ->
                    callback.onError(error.message ?: "Failed to change password")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Failed to change password")
            }
        }
    }

    fun updateEmail(newEmail: String, password: String, callback: SimpleCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.updateEmail(newEmail, password)
                }
                result.onSuccess {
                    callback.onSuccess()
                }.onFailure { error ->
                    callback.onError(error.message ?: "Failed to update email")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Failed to update email")
            }
        }
    }
}
