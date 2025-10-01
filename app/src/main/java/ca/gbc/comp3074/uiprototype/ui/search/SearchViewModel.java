package ca.gbc.comp3074.uiprototype.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.data.PlaceRepository;

public class SearchViewModel extends AndroidViewModel {

    private final PlaceRepository repository;
    private final LiveData<List<PlaceEntity>> allPlaces;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
        allPlaces = repository.getAllPlaces();
    }

    public LiveData<List<PlaceEntity>> getAllPlaces() {
        return allPlaces;
    }
}
