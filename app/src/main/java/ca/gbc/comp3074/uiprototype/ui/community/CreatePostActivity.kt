package ca.gbc.comp3074.uiprototype.ui.community

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseCommunityRepository
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Activity for creating a new community post
 */
class CreatePostActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgPreview: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var etPlaceName: TextInputEditText
    private lateinit var etCaption: TextInputEditText
    private lateinit var btnPost: MaterialButton
    
    private lateinit var communityRepository: SupabaseCommunityRepository
    private var selectedImageUri: Uri? = null
    
    // Modern photo picker for Android 13+ (no permissions needed)
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable URI permission for the selected image
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission not needed or already granted
            }
            selectedImageUri = it
            displaySelectedImage(it)
        }
    }
    
    // Legacy image picker for older Android versions
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission not needed or already granted
            }
            selectedImageUri = it
            displaySelectedImage(it)
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            displaySelectedImage(selectedImageUri!!)
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        
        // Initialize Supabase
        SupabaseClientManager.initialize()
        communityRepository = SupabaseCommunityRepository(this)
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        imgPreview = findViewById(R.id.imgPreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        etPlaceName = findViewById(R.id.etPlaceName)
        etCaption = findViewById(R.id.etCaption)
        btnPost = findViewById(R.id.btnPost)
        
        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Setup select image button
        btnSelectImage.setOnClickListener {
            checkPermissionsAndSelectImage()
        }
        
        // Setup post button
        btnPost.setOnClickListener {
            validateAndCreatePost()
        }
    }
    
    private fun checkPermissionsAndSelectImage() {
        // Android 13+ (API 33+) uses READ_MEDIA_IMAGES and doesn't need permission for photo picker
        // Android 10-12 (API 29-32) uses scoped storage, no permission needed
        // Android 9 and below need READ_EXTERNAL_STORAGE
        
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Manifest.permission.READ_MEDIA_IMAGES
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+, scoped storage, no permission needed for gallery
                null
            }
            else -> {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }
        
        when {
            permission == null -> {
                // No permission needed, proceed directly
                showImageSourceDialog()
            }
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun showImageSourceDialog() {
        val options = arrayOf("Gallery", "Camera")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Use modern photo picker on Android 13+, legacy picker on older versions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                    1 -> launchCamera()
                }
            }
            .show()
    }
    
    private fun launchCamera() {
        val photoUri = createImageUri()
        photoUri?.let {
            selectedImageUri = it
            cameraLauncher.launch(it)
        }
    }
    
    private fun createImageUri(): Uri? {
        val contentResolver = contentResolver
        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.content.ContentValues()
        )
    }
    
    private fun displaySelectedImage(uri: Uri) {
        btnSelectImage.visibility = MaterialButton.GONE
        Glide.with(this)
            .load(uri)
            .override(800, 800) // Limit image size for preview
            .centerCrop()
            .into(imgPreview)
    }
    
    private fun validateAndCreatePost() {
        val placeName = etPlaceName.text?.toString()?.trim()
        val caption = etCaption.text?.toString()?.trim()
        val category = getSelectedCategory()
        
        when {
            selectedImageUri == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
            placeName.isNullOrEmpty() -> {
                etPlaceName.error = "Place name is required"
            }
            caption.isNullOrEmpty() -> {
                etCaption.error = "Caption is required"
            }
            else -> {
                createPost(placeName, caption, category, selectedImageUri!!)
            }
        }
    }
    
    private fun getSelectedCategory(): String {
        return when (chipGroupCategory.checkedChipId) {
            R.id.chipFood -> "food"
            R.id.chipDrink -> "drink"
            R.id.chipAtmosphere -> "atmosphere"
            R.id.chipEnvironment -> "environment"
            else -> "food"
        }
    }
    
    private fun createPost(placeName: String, caption: String, category: String, imageUri: Uri) {
        // Disable button to prevent double submission
        btnPost.isEnabled = false
        btnPost.text = "Posting..."
        
        lifecycleScope.launch {
            communityRepository.createPost(placeName, caption, category, imageUri)
                .onSuccess {
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Post created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .onFailure { error ->
                    btnPost.isEnabled = true
                    btnPost.text = "Post"
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Failed to create post: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
    
    override fun finish() {
        super.finish()
        // Add smooth exit transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
