package ca.gbc.comp3074.uiprototype.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.models.CommunityPost
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying community posts in RecyclerView
 */
class CommunityPostAdapter(
    private val onLikeClick: (CommunityPost) -> Unit,
    private val onCommentClick: (CommunityPost) -> Unit,
    private val onMoreClick: (CommunityPost) -> Unit,
    private val onUserClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, CommunityPostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUserAvatar: ShapeableImageView = itemView.findViewById(R.id.imgUserAvatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        private val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val chipCategory: Chip = itemView.findViewById(R.id.chipCategory)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val tvLikesCount: TextView = itemView.findViewById(R.id.tvLikesCount)
        private val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.tvCommentsCount)
        private val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)
        private val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)

        fun bind(post: CommunityPost) {
            // User info
            tvUserName.text = post.userName
            tvPlaceName.text = "at ${post.placeName}"
            
            // Load avatar
            if (!post.userAvatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.userAvatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .override(100, 100) // Resize to small avatar size
                    .circleCrop()
                    .into(imgUserAvatar)
            } else {
                imgUserAvatar.setImageResource(R.drawable.ic_profile)
            }
            
            // Load post image with thumbnail for faster loading
            Glide.with(itemView.context)
                .load(post.imageUrl)
                .thumbnail(0.25f) // Load a 25% quality thumbnail first
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .centerCrop()
                .into(imgPost)
            
            // Category
            chipCategory.text = when (post.category) {
                "food" -> "🍕 Food"
                "drink" -> "☕ Drink"
                "atmosphere" -> "✨ Atmosphere"
                "environment" -> "🌿 Environment"
                else -> post.category
            }
            
            // Like button
            val likeIcon = if (post.isLikedByCurrentUser) {
                R.drawable.ic_heart_filled
            } else {
                R.drawable.ic_heart_outline
            }
            btnLike.setImageResource(likeIcon)
            tvLikesCount.text = post.likesCount.toString()
            
            // Comments
            tvCommentsCount.text = post.commentsCount.toString()
            
            // Time ago
            tvTimeAgo.text = getTimeAgo(post.createdAt)
            
            // Caption
            tvCaption.text = post.caption
            
            // Click listeners
            btnLike.setOnClickListener { onLikeClick(post) }
            btnComment.setOnClickListener { onCommentClick(post) }
            btnMore.setOnClickListener { onMoreClick(post) }
            imgUserAvatar.setOnClickListener { onUserClick(post) }
            tvUserName.setOnClickListener { onUserClick(post) }
        }
        
        private fun getTimeAgo(timestamp: Long): String {
            if (timestamp == 0L) return "just now"
            
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "${minutes}m ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "${hours}h ago"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "${days}d ago"
                }
                else -> {
                    val date = Date(timestamp)
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<CommunityPost>() {
        override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem == newItem
        }
    }
}
