package ca.gbc.comp3074.uiprototype.data.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Profile model matching the Supabase profiles table
 */
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("full_name")
    val fullName: String? = null,
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerialName("bio")
    val bio: String? = null,
    
    @SerialName("places_visited")
    val placesVisited: Int = 0,
    
    @SerialName("reviews_count")
    val reviewsCount: Int = 0,
    
    @SerialName("followers_count")
    val followersCount: Int = 0,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request model for updating user profile
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("full_name")
    val fullName: String? = null,
    
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerialName("bio")
    val bio: String? = null
)

/**
 * Request model for user authentication
 */
@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

/**
 * Request model for user registration
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)
