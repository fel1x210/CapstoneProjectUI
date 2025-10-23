package ca.gbc.comp3074.uiprototype.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.gbc.comp3074.uiprototype.R
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthRepository
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseStorageRepository
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile Fragment with Supabase integration for avatar management
 */
class ProfileFragmentKt : Fragment(R.layout.fragment_profile) {
    
    private lateinit var profileAvatar: ImageView
    private lateinit var profileName: TextView
    private lateinit var placesVisitedCount: TextView
    private lateinit var reviewsCount: TextView
    private lateinit var followersCount: TextView
    
    private lateinit var authRepository: SupabaseAuthRepository
    private lateinit var storageRepository: SupabaseStorageRepository
    
    private var photoUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repositories
        authRepository = SupabaseAuthRepository()
        storageRepository = SupabaseStorageRepository(requireContext())
        
        // Initialize activity result launchers
        initializeActivityResultLaunchers()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Find views
        profileAvatar = view.findViewById(R.id.profileAvatar)
        profileName = view.findViewById(R.id.profileName)
        val profileAvatarCard = view.findViewById<View>(R.id.profileAvatarCard)
        val buttonLogout = view.findViewById<View>(R.id.buttonLogout)
        
        // Load user profile from Supabase
        loadUserProfile()
        
        // Set click listener on avatar
        profileAvatarCard.setOnClickListener {
            showAvatarOptions()
        }
        
        // Logout button
        buttonLogout.setOnClickListener {
            showLogoutDialog()
        }
        
        // Setup other UI elements (quick actions, settings, etc.)
        setupQuickActions(view)
        setupSettings(view)
    }
    
    private fun initializeActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImage = result.data?.data
                if (selectedImage != null) {
                    uploadAvatarToSupabase(selectedImage)
                }
            }
        }
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
                uploadAvatarToSupabase(photoUri!!)
            }
        }
        
        // Permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                
                result.onSuccess { profile ->
                    // Update UI with profile data
                    profileName.text = profile.fullName ?: profile.email
                    
                    // Load avatar if exists
                    if (!profile.avatarUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profile.avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(profileAvatar)
                    }
                    
                    // Update stats (if you have TextViews for these)
                    // placesVisitedCount.text = profile.placesVisited.toString()
                    // reviewsCount.text = profile.reviewsCount.toString()
                    // followersCount.text = profile.followersCount.toString()
                }
                
                result.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showAvatarOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
                null
            }
            
            photoFile?.let {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "ca.gbc.comp3074.uiprototype.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                cameraLauncher.launch(takePictureIntent)
            }
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "AVATAR_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    private fun uploadAvatarToSupabase(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                // Show loading
                Toast.makeText(requireContext(), "Uploading avatar...", Toast.LENGTH_SHORT).show()
                
                // Get current user ID
                val userId = authRepository.getCurrentUserProfile().getOrNull()?.id
                    ?: throw Exception("User not authenticated")
                
                // Upload to Supabase Storage
                val result = storageRepository.updateUserAvatar(userId, imageUri)
                
                result.onSuccess { avatarUrl ->
                    // Load new avatar into ImageView
                    Glide.with(requireContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(profileAvatar)
                    
                    Toast.makeText(
                        requireContext(),
                        "Profile picture updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                result.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload avatar: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error uploading avatar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setupQuickActions(view: View) {
        // Quick actions
        view.findViewById<View>(R.id.actionCheckin)?.setOnClickListener {
            Toast.makeText(requireContext(), "Check-in feature", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.actionReview)?.setOnClickListener {
            Toast.makeText(requireContext(), "Review feature", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.actionPhoto)?.setOnClickListener {
            Toast.makeText(requireContext(), "Photo feature", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.actionExplore)?.setOnClickListener {
            Toast.makeText(requireContext(), "Explore feature", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupSettings(view: View) {
        // Settings options
        view.findViewById<View>(R.id.settingAppearance)?.setOnClickListener {
            Toast.makeText(requireContext(), "Appearance settings", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingPrivacy)?.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy settings", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingHelp)?.setOnClickListener {
            Toast.makeText(requireContext(), "Help & Support", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingAbout)?.setOnClickListener {
            Toast.makeText(requireContext(), "About QuietSpace", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setPositiveButton(R.string.logout) { _, _ ->
                lifecycleScope.launch {
                    authRepository.signOut()
                    activity?.finish()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
