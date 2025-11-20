package ca.gbc.comp3074.uiprototype.data.supabase

import android.util.Log
import ca.gbc.comp3074.uiprototype.data.PlaceEntity
import ca.gbc.comp3074.uiprototype.data.supabase.models.UserFavorite
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseFavoritesRepository {

    private val TAG = "SupabaseFavorites"
    private val client = SupabaseClientManager.client

    suspend fun addFavorite(place: PlaceEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            if (place.googlePlaceId == null) {
                return@withContext Result.failure(Exception("Place has no Google ID"))
            }

            val favorite = UserFavorite(
                userId = userId,
                googlePlaceId = place.googlePlaceId,
                name = place.name,
                address = place.address,
                rating = place.rating,
                userRatingsTotal = place.reviewCount,
                latitude = place.latitude,
                longitude = place.longitude,
                placeType = place.type,
                quietScore = place.quietScore
            )

            client.postgrest["user_favorites"].insert(favorite)
            Log.d(TAG, "Added favorite to Supabase: ${place.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add favorite", e)
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(googlePlaceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            client.postgrest["user_favorites"].delete {
                filter {
                    eq("user_id", userId)
                    eq("google_place_id", googlePlaceId)
                }
            }
            Log.d(TAG, "Removed favorite from Supabase: $googlePlaceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove favorite", e)
            Result.failure(e)
        }
    }

    suspend fun getFavorites(): Result<List<UserFavorite>> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClientManager.getCurrentUserId()
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val favorites = client.postgrest["user_favorites"].select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<UserFavorite>()
            
            Log.d(TAG, "Fetched ${favorites.size} favorites from Supabase")
            Result.success(favorites)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch favorites", e)
            Result.failure(e)
        }
    }
}
