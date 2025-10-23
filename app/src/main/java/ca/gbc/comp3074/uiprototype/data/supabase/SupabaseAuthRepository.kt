package ca.gbc.comp3074.uiprototype.data.supabase

import android.util.Log
import ca.gbc.comp3074.uiprototype.data.supabase.models.UpdateProfileRequest
import ca.gbc.comp3074.uiprototype.data.supabase.models.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing user authentication and profile operations with Supabase
 */
class SupabaseAuthRepository {
    
    private val TAG = "SupabaseAuthRepository"
    private val client = SupabaseClientManager.client
    
    /**
     * Sign up a new user with email and password
     */
    suspend fun signUp(email: String, password: String, fullName: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                // Create auth user
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                // Get user ID from the current session after signup
                val userId = client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User ID not found after signup")
                
                // Create profile in database
                val profile = UserProfile(
                    id = userId,
                    email = email,
                    fullName = fullName,
                    placesVisited = 0,
                    reviewsCount = 0,
                    followersCount = 0
                )
                
                client.postgrest["profiles"].insert(profile)
                
                Result.success(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Sign up error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sign in existing user with email and password
     */
    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                // Sign in user
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                // Get user profile
                val userId = client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("User not authenticated")
                
                val profile = getUserProfile(userId).getOrThrow()
                
                Result.success(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Sign in error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signOut()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Sign out error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user profile by ID
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val profile = client.postgrest["profiles"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<UserProfile>()
                
                Result.success(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Get profile error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(userId: String, update: UpdateProfileRequest): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                client.postgrest["profiles"].update(update) {
                    filter {
                        eq("id", userId)
                    }
                }
                
                // Fetch updated profile
                val profile = getUserProfile(userId).getOrThrow()
                
                Result.success(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Update profile error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get current authenticated user profile
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("No authenticated user")
                
                getUserProfile(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Get current profile error", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Reset password for user email
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Reset password error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Change password for current user
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = client.auth.currentUserOrNull() 
                    ?: throw Exception("No authenticated user")
                
                // Re-authenticate with current password first
                val email = currentUser.email ?: throw Exception("User email not found")
                
                try {
                    client.auth.signInWith(Email) {
                        this.email = email
                        this.password = currentPassword
                    }
                } catch (e: Exception) {
                    throw Exception("Current password is incorrect")
                }
                
                // Update to new password
                client.auth.updateUser {
                    password = newPassword
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Change password error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Update email for current user
     */
    suspend fun updateEmail(newEmail: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = client.auth.currentUserOrNull() 
                    ?: throw Exception("No authenticated user")
                
                // Re-authenticate with password first
                val currentEmail = currentUser.email ?: throw Exception("User email not found")
                
                try {
                    client.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = password
                    }
                } catch (e: Exception) {
                    throw Exception("Password is incorrect")
                }
                
                // Update email
                client.auth.updateUser {
                    email = newEmail
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Update email error", e)
                Result.failure(e)
            }
        }
    }
}
