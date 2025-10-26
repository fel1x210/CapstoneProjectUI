package ca.gbc.comp3074.uiprototype.ui.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseCommunityRepository
import ca.gbc.comp3074.uiprototype.data.supabase.models.CommunityPost
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

/**
 * Fragment for displaying community posts feed
 */
class CommunityFragment : Fragment() {

    private val TAG = "CommunityFragment"
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var fabCreatePost: FloatingActionButton
    
    private lateinit var communityRepository: SupabaseCommunityRepository
    private lateinit var adapter: CommunityPostAdapter
    
    private var needsRefresh = false
    private var isFirstLoad = true
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)
        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyState = view.findViewById(R.id.emptyState)
        fabCreatePost = view.findViewById(R.id.fabCreatePost)
        
        // Initialize repository
        communityRepository = SupabaseCommunityRepository(requireContext())
        
        // Setup toolbar
        setupToolbar()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup SwipeRefresh
        swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
        
        // Setup FAB
        fabCreatePost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            // Mark that we need to refresh when returning (new post created)
            needsRefresh = true
            startActivity(intent)
        }
        
        // Load initial data with count sync on first load
        loadPosts(syncCounts = true)
    }
    
    override fun onResume() {
        super.onResume()
        // Reload if:
        // 1. First load (adapter not initialized or empty)
        // 2. Returning from activity that modified data (needsRefresh flag)
        if (isFirstLoad || !::adapter.isInitialized || adapter.itemCount == 0 || needsRefresh) {
            loadPosts()
            needsRefresh = false
            isFirstLoad = false
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Set flag to refresh when coming back (user might have interacted with posts)
        if (::adapter.isInitialized && adapter.itemCount > 0) {
            needsRefresh = true
        }
    }
    
    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_refresh -> {
                    // Force sync counts on manual refresh
                    loadPosts(syncCounts = true)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = CommunityPostAdapter(
            onLikeClick = { post -> toggleLike(post) },
            onCommentClick = { post -> showCommentsDialog(post) },
            onMoreClick = { post -> showPostOptions(post) },
            onUserClick = { post -> showUserProfile(post) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CommunityFragment.adapter
            setHasFixedSize(true)
            
            // Performance optimizations
            itemAnimator = null // Disable item animations for smoother scrolling
            setItemViewCacheSize(10) // Cache 10 off-screen items
            isNestedScrollingEnabled = true
        }
    }
    
    private fun loadPosts(syncCounts: Boolean = false) {
        swipeRefresh.isRefreshing = true
        
        lifecycleScope.launch {
            // Log authentication status for debugging
            val isAuthenticated = SupabaseClientManager.isUserAuthenticated()
            val userId = SupabaseClientManager.getCurrentUserId()
            Log.d(TAG, "Loading posts - Authenticated: $isAuthenticated, UserId: $userId")
            
            // Sync counts if requested (manual refresh or first load with mismatches)
            if (syncCounts) {
                Log.d(TAG, "Syncing post counts...")
                try {
                    communityRepository.syncPostCounts().getOrThrow()
                    Log.d(TAG, "Post counts synced successfully")
                } catch (error: Exception) {
                    Log.e(TAG, "Failed to sync counts: ${error.message}")
                }
            }
            
            // Load posts AFTER sync is complete
            communityRepository.getPosts()
                .onSuccess { posts ->
                    swipeRefresh.isRefreshing = false
                    
                    if (posts.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.submitList(posts)
                    }
                }
                .onFailure { error ->
                    swipeRefresh.isRefreshing = false
                    Log.e(TAG, "Failed to load posts", error)
                    Toast.makeText(
                        requireContext(),
                        "Failed to load posts: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
    
    private fun toggleLike(post: CommunityPost) {
        lifecycleScope.launch {
            communityRepository.toggleLike(post.id)
                .onSuccess { isLiked ->
                    // Update the post in the adapter immediately for instant feedback
                    val updatedPost = post.copy(
                        likesCount = if (isLiked) post.likesCount + 1 else post.likesCount - 1,
                        isLikedByCurrentUser = isLiked
                    )
                    updatePostInAdapter(updatedPost)
                    
                    // Also schedule a full refresh to ensure accuracy
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadPosts()
                    }, 500)
                }
                .onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to like post: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    
    private fun updatePostInAdapter(updatedPost: CommunityPost) {
        val currentList = adapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            currentList[index] = updatedPost
            adapter.submitList(currentList)
        }
    }
    
    private fun showCommentsDialog(post: CommunityPost) {
        val intent = Intent(requireContext(), PostCommentsActivity::class.java)
        intent.putExtra("POST_ID", post.id)
        intent.putExtra("POST_PLACE_NAME", post.placeName)
        // Mark that we need to refresh when returning
        needsRefresh = true
        startActivity(intent)
    }
    
    private fun showPostOptions(post: CommunityPost) {
        val options = arrayOf("Report", "Share")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Post Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> reportPost(post)
                    1 -> sharePost(post)
                }
            }
            .show()
    }
    
    private fun reportPost(post: CommunityPost) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Report Post")
            .setMessage("Are you sure you want to report this post?")
            .setPositiveButton("Report") { _, _ ->
                Toast.makeText(requireContext(), "Post reported", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun sharePost(post: CommunityPost) {
        val shareText = "${post.userName} shared a photo at ${post.placeName}\n\n${post.caption}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share post"))
    }
    
    private fun showUserProfile(post: CommunityPost) {
        Toast.makeText(
            requireContext(),
            "View ${post.userName}'s profile",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    companion object {
        fun newInstance() = CommunityFragment()
    }
}
