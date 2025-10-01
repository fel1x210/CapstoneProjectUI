package ca.gbc.comp3074.uiprototype.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PlaceRepository {

    private final PlaceDao placeDao;
    private final LiveData<List<PlaceEntity>> allPlaces;
    private final LiveData<List<PlaceEntity>> favoritePlaces;

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
}
