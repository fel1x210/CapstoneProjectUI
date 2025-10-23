package ca.gbc.comp3074.uiprototype.data.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model for community posts stored in Supabase
 */
@Serializable
data class CommunityPost(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("user_id")
    val userId: String = "",
    
    @SerialName("user_name")
    val userName: String = "",
    
    @SerialName("user_avatar_url")
    val userAvatarUrl: String? = null,
    
    @SerialName("place_name")
    val placeName: String = "",
    
    @SerialName("image_url")
    val imageUrl: String = "",
    
    @SerialName("caption")
    val caption: String = "",
    
    @SerialName("category")
    val category: String = "", // "food", "drink", "atmosphere", "environment"
    
    @SerialName("likes_count")
    val likesCount: Int = 0,
    
    @SerialName("comments_count")
    val commentsCount: Int = 0,
    
    @SerialName("created_at")
    val createdAt: Long = 0L,
    
    // Client-side only (not stored in DB)
    @kotlinx.serialization.Transient
    var isLikedByCurrentUser: Boolean = false
)

@Serializable
data class PostComment(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("post_id")
    val postId: String = "",
    
    @SerialName("user_id")
    val userId: String = "",
    
    @SerialName("user_name")
    val userName: String = "",
    
    @SerialName("user_avatar_url")
    val userAvatarUrl: String? = null,
    
    @SerialName("comment")
    val comment: String = "",
    
    @SerialName("rating")
    val rating: Float = 0f,
    
    @SerialName("created_at")
    val createdAt: Long = 0L
)

@Serializable
data class PostLike(
    @SerialName("id")
    val id: String = "",
    
    @SerialName("post_id")
    val postId: String = "",
    
    @SerialName("user_id")
    val userId: String = "",
    
    @SerialName("created_at")
    val createdAt: Long = 0L
)
