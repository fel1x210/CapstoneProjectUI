package ca.gbc.comp3074.uiprototype.ui.community

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseCommunityRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Activity for viewing and adding comments to a post
 */
class PostCommentsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRating: TextView
    private lateinit var etComment: TextInputEditText
    private lateinit var fabSendComment: FloatingActionButton
    
    private lateinit var communityRepository: SupabaseCommunityRepository
    private lateinit var adapter: CommentsAdapter
    private var postId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_comments)
        
        // Initialize Supabase
        SupabaseClientManager.initialize()
        communityRepository = SupabaseCommunityRepository(this)
        
        // Get post ID from intent
        postId = intent.getStringExtra("POST_ID") ?: ""
        val placeName = intent.getStringExtra("POST_PLACE_NAME") ?: "Post"
        
        if (postId.isEmpty()) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        emptyState = findViewById(R.id.emptyState)
        ratingBar = findViewById(R.id.ratingBar)
        tvRating = findViewById(R.id.tvRating)
        etComment = findViewById(R.id.etComment)
        fabSendComment = findViewById(R.id.fabSendComment)
        
        // Setup toolbar
        toolbar.title = "$placeName - Comments"
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Setup RecyclerView
        adapter = CommentsAdapter()
        recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(this@PostCommentsActivity)
            adapter = this@PostCommentsActivity.adapter
            setHasFixedSize(true)
        }
        
        // Setup rating bar
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvRating.text = String.format("%.1f", rating)
        }
        tvRating.text = "0.0"
        
        // Setup send button
        fabSendComment.setOnClickListener {
            addComment()
        }
        
        // Load comments
        loadComments()
    }
    
    private fun loadComments() {
        lifecycleScope.launch {
            communityRepository.getComments(postId)
                .onSuccess { comments ->
                    if (comments.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerViewComments.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recyclerViewComments.visibility = View.VISIBLE
                        adapter.submitList(comments)
                    }
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@PostCommentsActivity,
                        "Failed to load comments: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    
    private fun addComment() {
        val commentText = etComment.text?.toString()?.trim()
        val rating = ratingBar.rating
        
        if (commentText.isNullOrEmpty()) {
            etComment.error = "Please write a comment"
            return
        }
        
        // Disable send button
        fabSendComment.isEnabled = false
        
        lifecycleScope.launch {
            communityRepository.addComment(postId, commentText, rating)
                .onSuccess {
                    // Clear input
                    etComment.text?.clear()
                    ratingBar.rating = 0f
                    tvRating.text = "0.0"
                    
                    // Reload comments
                    loadComments()
                    
                    Toast.makeText(
                        this@PostCommentsActivity,
                        "Comment added!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    fabSendComment.isEnabled = true
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@PostCommentsActivity,
                        "Failed to add comment: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    fabSendComment.isEnabled = true
                }
        }
    }
}
