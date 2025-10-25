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
                Log.d(TAG, "Starting signup process for: $email")
                
                // Create auth user
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                Log.d(TAG, "Auth signup completed, checking for user session...")
                
                // Small delay to ensure session is established
                kotlinx.coroutines.delay(500)
                
                // Get user ID from current session
                val userId = client.auth.currentUserOrNull()?.id
                
                if (userId == null) {
                    Log.w(TAG, "User ID not immediately available - may need email confirmation")
                    throw Exception("User ID not found after signup.\n\nPlease:\n1. Disable email confirmation in Supabase\n2. Or check your email to confirm")
                }
                
                Log.d(TAG, "Got user ID: $userId")
                
                // Check if profile already exists (in case of retry)
                try {
                    val existingProfile = client.postgrest["profiles"]
                        .select(columns = Columns.ALL) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingleOrNull<UserProfile>()
                    
                    if (existingProfile != null) {
                        Log.d(TAG, "Profile already exists, returning existing profile")
                        return@withContext Result.success(existingProfile)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "No existing profile found, creating new one")
                }
                
                // Create profile in database
                val profile = UserProfile(
                    id = userId,
                    email = email,
                    fullName = fullName,
                    placesVisited = 0,
                    reviewsCount = 0,
                    followersCount = 0
                )
                
                Log.d(TAG, "Inserting profile into database...")
                client.postgrest["profiles"].insert(profile)
                
                Log.d(TAG, "Signup completed successfully!")
                Result.success(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Sign up error: ${e.message}", e)
                
                // Provide more helpful error messages
                val errorMessage = when {
                    e.message?.contains("JWT", ignoreCase = true) == true -> 
                        "Authentication error. Please disable email confirmation in Supabase settings."
                    e.message?.contains("profiles", ignoreCase = true) == true -> 
                        "Database error. Please ensure the 'profiles' table exists in Supabase."
                    e.message?.contains("duplicate", ignoreCase = true) == true -> 
                        "Account already exists. Please try logging in."
                    e.message?.contains("not found after signup", ignoreCase = true) == true ->
                        e.message // Use the detailed message we provided
                    else -> e.message ?: "Unknown error occurred"
                }
                
                Result.failure(Exception(errorMessage))
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
