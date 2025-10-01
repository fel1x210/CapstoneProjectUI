package ca.gbc.comp3074.uiprototype.ui.favorites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.data.PlaceRepository;

public class FavoritesViewModel extends AndroidViewModel {

    private final PlaceRepository repository;
    private final LiveData<List<PlaceEntity>> favorites;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
        favorites = repository.getFavoritePlaces();
    }

    public LiveData<List<PlaceEntity>> getFavorites() {
        return favorites;
    }
}
