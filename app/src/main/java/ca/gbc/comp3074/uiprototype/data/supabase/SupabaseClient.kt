package ca.gbc.comp3074.uiprototype.data.supabase

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Singleton object to manage Supabase client instance
 * Provides access to Authentication, Database (Postgrest), and Storage modules
 */
object SupabaseClientManager {
    
    private const val SUPABASE_URL = "https://itwqcyumcrqqqetoqgai.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0d3FjeXVtY3JxcXFldG9xZ2FpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEyMzk4MjIsImV4cCI6MjA3NjgxNTgyMn0.tVvcvrORayDB6i63-y56hg-VgTkz0kwJmaSCO0GyMo4"
    
    private var _client: SupabaseClient? = null
    
    /**
     * Get or create the Supabase client instance
     */
    val client: SupabaseClient
        get() {
            if (_client == null) {
                _client = createSupabaseClient(
                    supabaseUrl = SUPABASE_URL,
                    supabaseKey = SUPABASE_KEY
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Storage)
                }
            }
            return _client!!
        }
    
    /**
     * Access to Supabase Authentication module
     */
    val auth: Auth
        get() = client.auth
    
    /**
     * Access to Supabase Database (Postgrest) module
     */
    val database: Postgrest
        get() = client.postgrest
    
    /**
     * Access to Supabase Storage module
     */
    val storage: Storage
        get() = client.storage
    
    /**
     * Initialize the client (optional - can be called from Application class)
     */
    fun initialize() {
        // Initialize the client by accessing it
        // This ensures the client is created early
        client
        
        // You can add additional initialization logic here
        // For example, checking authentication state, etc.
    }
    
    /**
     * Check if user is currently authenticated
     */
    suspend fun isUserAuthenticated(): Boolean {
        return try {
            auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current user ID if authenticated
     */
    suspend fun getCurrentUserId(): String? {
        return try {
            auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }
}
