package ca.gbc.comp3074.uiprototype.data.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import ca.gbc.comp3074.uiprototype.data.supabase.models.CommunityPost
import ca.gbc.comp3074.uiprototype.data.supabase.models.PostComment
import ca.gbc.comp3074.uiprototype.data.supabase.models.PostLike
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for managing community posts, likes, and comments with Supabase
 */
class SupabaseCommunityRepository(private val context: Context) {
    
    private val TAG = "CommunityRepository"
    private val client = SupabaseClientManager.client
    private val storageRepository = SupabaseStorageRepository(context)
    
    /**
     * Fetch all community posts with like status for current user
     */
    suspend fun getPosts(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            // Fetch all posts ordered by creation date
            val posts = client.postgrest["community_posts"]
                .select(Columns.ALL) {
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<CommunityPost>()
            
            // Try to fetch current user's likes (only if authenticated)
            try {
                val currentUserId = SupabaseClientManager.getCurrentUserId()
                
                if (currentUserId != null) {
                    val userLikes = client.postgrest["post_likes"]
                        .select(Columns.raw("post_id")) {
                            filter {
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<PostLike>()
                    
                    val likedPostIds = userLikes.map { it.postId }.toSet()
                    
                    // Update isLikedByCurrentUser for each post
                    posts.forEach { post ->
                        post.isLikedByCurrentUser = likedPostIds.contains(post.id)
                    }
                }
            } catch (e: Exception) {
                // If fetching likes fails (e.g., not authenticated), just skip it
                Log.w(TAG, "Could not fetch user likes, user may not be authenticated", e)
            }
            
            Log.d(TAG, "Fetched ${posts.size} posts successfully")
            Result.success(posts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch posts: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create a new community post with image upload
     */
    suspend fun createPost(
        placeName: String,
        caption: String,
        category: String,
        imageUri: Uri
    ): Result<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            // Get current user profile
            val authRepository = SupabaseAuthRepository()
            val profileResult = authRepository.getCurrentUserProfile()
            val userName = profileResult.getOrNull()?.fullName ?: "Anonymous"
            val userAvatarUrl = profileResult.getOrNull()?.avatarUrl
            
            // Upload image to storage
            val postId = UUID.randomUUID().toString()
            val imageFileName = "post_${postId}_${System.currentTimeMillis()}.jpg"
            val imageUploadResult = storageRepository.uploadFile(
                bucketName = "community-posts",
                filePath = "images/$imageFileName",
                uri = imageUri
            )
            
            val imageUrl = imageUploadResult.getOrElse {
                return@withContext Result.failure(Exception("Failed to upload image: ${it.message}"))
            }
            
            // Create post in database
            val newPost = CommunityPost(
                id = postId,
                userId = currentUserId,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                placeName = placeName,
                imageUrl = imageUrl,
                caption = caption,
                category = category,
                likesCount = 0,
                commentsCount = 0,
                createdAt = System.currentTimeMillis()
            )
            
            client.postgrest["community_posts"].insert(newPost)
            
            Log.d(TAG, "Post created successfully: $postId")
            Result.success(newPost)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create post", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle like on a post
     */
    suspend fun toggleLike(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            // Check if user already liked this post
            val existingLikes = client.postgrest["post_likes"]
                .select(Columns.ALL) {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", currentUserId)
                    }
                }
                .decodeList<PostLike>()
            
            val isLiked = if (existingLikes.isEmpty()) {
                // Add like
                val newLike = PostLike(
                    id = UUID.randomUUID().toString(),
                    postId = postId,
                    userId = currentUserId,
                    createdAt = System.currentTimeMillis()
                )
                client.postgrest["post_likes"].insert(newLike)
                true
            } else {
                // Remove like
                client.postgrest["post_likes"].delete {
                    filter {
                        eq("id", existingLikes.first().id)
                    }
                }
                false
            }
            
            // Update likes count in community_posts
            try {
                val likesCount = client.postgrest["post_likes"]
                    .select(Columns.raw("id")) {
                        filter {
                            eq("post_id", postId)
                        }
                    }
                    .decodeList<PostLike>().size
                
                client.postgrest["community_posts"].update(
                    mapOf("likes_count" to likesCount)
                ) {
                    filter {
                        eq("id", postId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update like count", e)
            }
            
            Log.d(TAG, "Like toggled: $isLiked for post $postId")
            Result.success(isLiked)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle like", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a comment/review to a post
     */
    suspend fun addComment(
        postId: String,
        comment: String,
        rating: Float
    ): Result<PostComment> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            // Get current user profile
            val authRepository = SupabaseAuthRepository()
            val profileResult = authRepository.getCurrentUserProfile()
            val userName = profileResult.getOrNull()?.fullName ?: "Anonymous"
            val userAvatarUrl = profileResult.getOrNull()?.avatarUrl
            
            // Create comment
            val newComment = PostComment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                userId = currentUserId,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                comment = comment,
                rating = rating,
                createdAt = System.currentTimeMillis()
            )
            
            client.postgrest["post_comments"].insert(newComment)
            
            // Update comments count in community_posts
            try {
                val commentsCount = client.postgrest["post_comments"]
                    .select(Columns.raw("id")) {
                        filter {
                            eq("post_id", postId)
                        }
                    }
                    .decodeList<PostComment>().size
                
                client.postgrest["community_posts"].update(
                    mapOf("comments_count" to commentsCount)
                ) {
                    filter {
                        eq("id", postId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update comment count", e)
                // Don't fail the whole operation if just the count update fails
            }
            
            Log.d(TAG, "Comment added successfully to post $postId")
            Result.success(newComment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add comment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get comments for a specific post
     */
    suspend fun getComments(postId: String): Result<List<PostComment>> = withContext(Dispatchers.IO) {
        try {
            val comments = client.postgrest["post_comments"]
                .select(Columns.ALL) {
                    filter {
                        eq("post_id", postId)
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<PostComment>()
            
            Log.d(TAG, "Fetched ${comments.size} comments for post $postId")
            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch comments", e)
            Result.failure(e)
        }
    }
    
    /**
     * Recalculate and fix comment/like counts for all posts
     * This ensures counts match the actual number of comments/likes
     */
    suspend fun syncPostCounts(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get all posts (just IDs and current counts to minimize data)
            val posts = client.postgrest["community_posts"]
                .select(Columns.raw("id, comments_count, likes_count"))
                .decodeList<CommunityPost>()
            
            Log.d(TAG, "Syncing counts for ${posts.size} posts...")
            
            for (post in posts) {
                try {
                    // Count actual comments
                    val actualCommentsCount = client.postgrest["post_comments"]
                        .select(Columns.raw("id")) {
                            filter {
                                eq("post_id", post.id)
                            }
                            count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                        }.countOrNull() ?: 0
                    
                    // Count actual likes
                    val actualLikesCount = client.postgrest["post_likes"]
                        .select(Columns.raw("id")) {
                            filter {
                                eq("post_id", post.id)
                            }
                            count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                        }.countOrNull() ?: 0
                    
                    // Update if counts don't match
                    if (post.commentsCount != actualCommentsCount.toInt() || post.likesCount != actualLikesCount.toInt()) {
                        Log.d(TAG, "Syncing post ${post.id}: comments ${post.commentsCount} -> $actualCommentsCount, likes ${post.likesCount} -> $actualLikesCount")
                        
                        client.postgrest["community_posts"].update(
                            mapOf(
                                "comments_count" to actualCommentsCount,
                                "likes_count" to actualLikesCount
                            )
                        ) {
                            filter {
                                eq("id", post.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync counts for post ${post.id}", e)
                }
            }
            
            Log.d(TAG, "Post counts synced successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync post counts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a post (only by post owner)
     */
    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            // Verify ownership
            val post = client.postgrest["community_posts"]
                .select(Columns.ALL) {
                    filter {
                        eq("id", postId)
                        eq("user_id", currentUserId)
                    }
                }
                .decodeSingleOrNull<CommunityPost>()
                ?: return@withContext Result.failure(Exception("Post not found or unauthorized"))
            
            // Delete associated likes and comments
            client.postgrest["post_likes"].delete {
                filter {
                    eq("post_id", postId)
                }
            }
            
            client.postgrest["post_comments"].delete {
                filter {
                    eq("post_id", postId)
                }
            }
            
            // Delete post
            client.postgrest["community_posts"].delete {
                filter {
                    eq("id", postId)
                }
            }
            
            // Delete image from storage
            try {
                val imageFileName = post.imageUrl.substringAfterLast("/")
                storageRepository.deleteFile("community-posts", "images/$imageFileName")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete image from storage", e)
            }
            
            Log.d(TAG, "Post deleted successfully: $postId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete post", e)
            Result.failure(e)
        }
    }
}
