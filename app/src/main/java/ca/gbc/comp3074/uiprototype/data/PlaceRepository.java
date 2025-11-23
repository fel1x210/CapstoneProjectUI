package ca.gbc.comp3074.uiprototype.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseFavoritesRepository;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseSyncHelper;
import ca.gbc.comp3074.uiprototype.data.supabase.models.UserFavorite;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class PlaceRepository {

    private final PlaceDao placeDao;
    private final LiveData<List<PlaceEntity>> allPlaces;
    private final LiveData<List<PlaceEntity>> favoritePlaces;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SupabaseFavoritesRepository supabaseFavoritesRepository;

    public PlaceRepository(Application application) {
        QuietSpaceDatabase database = QuietSpaceDatabase.getInstance(application);
        placeDao = database.placeDao();
        allPlaces = placeDao.getAllPlaces();
        favoritePlaces = placeDao.getFavoritePlaces();
        supabaseFavoritesRepository = new SupabaseFavoritesRepository();
    }

    public LiveData<List<PlaceEntity>> getAllPlaces() {
        return allPlaces;
    }

    public LiveData<List<PlaceEntity>> getFavoritePlaces() {
        return favoritePlaces;
    }

    public PlaceEntity getPlaceByGoogleId(String googlePlaceId) {
        try {
            Future<PlaceEntity> future = executorService.submit(() -> placeDao.getPlaceByGoogleId(googlePlaceId));
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    public PlaceEntity getPlaceById(int id) {
        try {
            Future<PlaceEntity> future = executorService.submit(() -> placeDao.getPlaceById(id));
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get places synchronously (for background thread use only)
     * 
     * @return List of places or empty list if error
     */
    public List<PlaceEntity> getAllPlacesSync() {
        try {
            Future<List<PlaceEntity>> future = executorService.submit(() -> placeDao.getAllPlacesSync());
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Insert a place into the database
     */
    public void insertPlace(PlaceEntity place) {
        executorService.execute(() -> placeDao.insert(place));
    }

    /**
     * Delete all places from the database
     */
    public void deleteAllPlaces() {
        executorService.execute(() -> placeDao.deleteAll());
    }

    /**
     * Update a place in the database
     */
    public void updatePlace(PlaceEntity place) {
        executorService.execute(() -> placeDao.update(place));
    }

    /**
     * Insert or update a place based on Google Place ID
     * Also syncs with Supabase if it's a favorite
     */
    public void insertOrUpdate(PlaceEntity place) {
        executorService.execute(() -> {
            android.util.Log.d("PlaceRepository",
                    "insertOrUpdate called for: " + place.name + ", favorite=" + place.favorite);
            if (place.googlePlaceId != null) {
                PlaceEntity existing = placeDao.getPlaceByGoogleId(place.googlePlaceId);
                if (existing != null) {
                    place.id = existing.id;
                    placeDao.update(place);
                    android.util.Log.d("PlaceRepository", "Updated existing place in Room DB: " + place.name);
                } else {
                    placeDao.insert(place);
                    android.util.Log.d("PlaceRepository", "Inserted new place in Room DB: " + place.name);
                }

                // Sync with Supabase
                android.util.Log.d("PlaceRepository", "Triggering Supabase sync for: " + place.name);
                syncToSupabase(place);
            } else {
                placeDao.insert(place);
                android.util.Log.d("PlaceRepository", "Inserted place without Google ID");
            }
        });
    }

    private void syncToSupabase(PlaceEntity place) {
        if (place.favorite) {
            android.util.Log.d("PlaceRepository", "Calling SupabaseSyncHelper.addFavorite for: " + place.name);
            SupabaseSyncHelper.addFavorite(supabaseFavoritesRepository, place);
        } else {
            android.util.Log.d("PlaceRepository",
                    "Calling SupabaseSyncHelper.removeFavorite for: " + place.googlePlaceId);
            SupabaseSyncHelper.removeFavorite(supabaseFavoritesRepository, place.googlePlaceId);
        }
    }

    /**
     * Sync favorites from Supabase to local database
     * Should be called on app start or login
     */
    public void syncFavoritesFromCloud() {
        SupabaseSyncHelper.syncFavorites(this, supabaseFavoritesRepository);
    }

    // Helper method for the Kotlin sync helper to call back
    public void updateLocalFavorites(List<UserFavorite> cloudFavorites) {
        android.util.Log.d("PlaceRepository",
                "updateLocalFavorites called with " + cloudFavorites.size() + " favorites from cloud");
        executorService.execute(() -> {
            // Create a set of cloud Google Place IDs for quick lookup
            java.util.Set<String> cloudGoogleIds = new java.util.HashSet<>();
            for (UserFavorite fav : cloudFavorites) {
                cloudGoogleIds.add(fav.getGooglePlaceId());
            }

            // First, update/insert favorites from cloud
            for (UserFavorite cloudFav : cloudFavorites) {
                PlaceEntity existing = placeDao.getPlaceByGoogleId(cloudFav.getGooglePlaceId());
                if (existing == null) {
                    // Create new place from cloud favorite
                    PlaceEntity newPlace = new PlaceEntity();
                    newPlace.googlePlaceId = cloudFav.getGooglePlaceId();
                    newPlace.name = cloudFav.getName();
                    newPlace.address = cloudFav.getAddress();
                    newPlace.rating = cloudFav.getRating() != null ? cloudFav.getRating() : 0;
                    newPlace.reviewCount = cloudFav.getUserRatingsTotal() != null ? cloudFav.getUserRatingsTotal() : 0;
                    newPlace.latitude = cloudFav.getLatitude() != null ? cloudFav.getLatitude() : 0;
                    newPlace.longitude = cloudFav.getLongitude() != null ? cloudFav.getLongitude() : 0;
                    newPlace.type = cloudFav.getPlaceType() != null ? cloudFav.getPlaceType() : "Quiet Space";
                    newPlace.quietScore = cloudFav.getQuietScore() != null ? cloudFav.getQuietScore() : 3.0f;
                    newPlace.favorite = true;
                    newPlace.isOpen = true;
                    newPlace.tags = new java.util.ArrayList<>();

                    placeDao.insert(newPlace);
                    android.util.Log.d("PlaceRepository", "Inserted new favorite from cloud: " + newPlace.name);
                } else if (!existing.favorite) {
                    // Update existing place to be favorite
                    existing.favorite = true;
                    placeDao.update(existing);
                    android.util.Log.d("PlaceRepository", "Updated existing place to favorite: " + existing.name);
                } else {
                    android.util.Log.d("PlaceRepository", "Place already exists and is favorited: " + existing.name);
                }
            }

            android.util.Log.d("PlaceRepository", "Finished updating local favorites from cloud");
        });
    }
}
