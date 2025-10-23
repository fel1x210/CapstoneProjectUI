package ca.gbc.comp3074.uiprototype.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.models.PostComment
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying post comments in RecyclerView
 */
class CommentsAdapter : ListAdapter<PostComment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUserAvatar: ShapeableImageView = itemView.findViewById(R.id.imgUserAvatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)

        fun bind(comment: PostComment) {
            tvUserName.text = comment.userName
            tvComment.text = comment.comment
            ratingBar.rating = comment.rating
            tvTimeAgo.text = getTimeAgo(comment.createdAt)
            
            // Load avatar
            if (!comment.userAvatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(comment.userAvatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imgUserAvatar)
            } else {
                imgUserAvatar.setImageResource(R.drawable.ic_profile)
            }
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

    private class CommentDiffCallback : DiffUtil.ItemCallback<PostComment>() {
        override fun areItemsTheSame(oldItem: PostComment, newItem: PostComment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostComment, newItem: PostComment): Boolean {
            return oldItem == newItem
        }
    }
}
