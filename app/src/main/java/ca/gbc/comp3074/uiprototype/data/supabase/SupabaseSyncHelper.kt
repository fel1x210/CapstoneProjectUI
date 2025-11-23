package ca.gbc.comp3074.uiprototype.data.supabase

import ca.gbc.comp3074.uiprototype.data.PlaceRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Helper object to bridge Java PlaceRepository and Kotlin Coroutines
 */
object SupabaseSyncHelper {
    
    private const val TAG = "SupabaseSyncHelper"

    @JvmStatic
    fun syncFavorites(repository: PlaceRepository, supabaseRepository: SupabaseFavoritesRepository) {
        android.util.Log.d(TAG, "syncFavorites called - fetching from Supabase")
        CoroutineScopeHelper.ioScope.launch {
            supabaseRepository.getFavorites()
                .onSuccess { favorites ->
                    android.util.Log.d(TAG, "Successfully fetched ${favorites.size} favorites from Supabase")
                    repository.updateLocalFavorites(favorites)
                }
                .onFailure { e ->
                    android.util.Log.e(TAG, "Failed to sync favorites from Supabase", e)
                }
        }
    }

    @JvmStatic
    fun addFavorite(supabaseRepository: SupabaseFavoritesRepository, place: ca.gbc.comp3074.uiprototype.data.PlaceEntity) {
        android.util.Log.d(TAG, "addFavorite called for: ${place.name}")
        CoroutineScopeHelper.ioScope.launch {
            supabaseRepository.addFavorite(place)
                .onSuccess {
                    android.util.Log.d(TAG, "Successfully added favorite to Supabase: ${place.name}")
                }
                .onFailure { e ->
                    android.util.Log.e(TAG, "Failed to add favorite to Supabase: ${place.name}", e)
                }
        }
    }

    @JvmStatic
    fun removeFavorite(supabaseRepository: SupabaseFavoritesRepository, googlePlaceId: String) {
        CoroutineScopeHelper.ioScope.launch {
            supabaseRepository.removeFavorite(googlePlaceId)
                .onFailure { e ->
                    android.util.Log.e(TAG, "Failed to remove favorite: $googlePlaceId", e)
                }
        }
    }
}
