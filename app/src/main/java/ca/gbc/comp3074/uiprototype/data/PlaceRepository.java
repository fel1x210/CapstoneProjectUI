package ca.gbc.comp3074.uiprototype.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlaceRepository {

    private final PlaceDao placeDao;
    private final LiveData<List<PlaceEntity>> allPlaces;
    private final LiveData<List<PlaceEntity>> favoritePlaces;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PlaceRepository(Application application) {
        QuietSpaceDatabase database = QuietSpaceDatabase.getInstance(application);
        placeDao = database.placeDao();
        allPlaces = placeDao.getAllPlaces();
        favoritePlaces = placeDao.getFavoritePlaces();
    }

    public LiveData<List<PlaceEntity>> getAllPlaces() {
        return allPlaces;
    }

    public LiveData<List<PlaceEntity>> getFavoritePlaces() {
        return favoritePlaces;
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
}
