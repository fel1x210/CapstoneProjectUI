package ca.gbc.comp3074.uiprototype.data.supabase

import ca.gbc.comp3074.uiprototype.data.PlaceRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Helper object to bridge Java PlaceRepository and Kotlin Coroutines
 */
object SupabaseSyncHelper {
    
    @JvmStatic
    fun syncFavorites(repository: PlaceRepository, supabaseRepository: SupabaseFavoritesRepository) {
        GlobalScope.launch {
            supabaseRepository.getFavorites()
                .onSuccess { favorites ->
                    repository.updateLocalFavorites(favorites)
                }
                .onFailure {
                    // Log error
                }
        }
    }

    @JvmStatic
    fun addFavorite(supabaseRepository: SupabaseFavoritesRepository, place: ca.gbc.comp3074.uiprototype.data.PlaceEntity) {
        GlobalScope.launch {
            supabaseRepository.addFavorite(place)
        }
    }

    @JvmStatic
    fun removeFavorite(supabaseRepository: SupabaseFavoritesRepository, googlePlaceId: String) {
        GlobalScope.launch {
            supabaseRepository.removeFavorite(googlePlaceId)
        }
    }
}
