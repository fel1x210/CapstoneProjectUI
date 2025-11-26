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
    private final SearchManager searchManager;

    private final androidx.lifecycle.MutableLiveData<String> searchQuery = new androidx.lifecycle.MutableLiveData<>();
    private final androidx.lifecycle.MutableLiveData<List<PlaceEntity>> searchResults = new androidx.lifecycle.MutableLiveData<>();
    private final androidx.lifecycle.MutableLiveData<Boolean> isLoading = new androidx.lifecycle.MutableLiveData<>(
            false);
    private final androidx.lifecycle.MutableLiveData<String> errorMessage = new androidx.lifecycle.MutableLiveData<>();

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
        allPlaces = repository.getAllPlaces();
        searchManager = new SearchManager(application);
    }

    public LiveData<List<PlaceEntity>> getAllPlaces() {
        return allPlaces;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<List<PlaceEntity>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void search(String query, double latitude, double longitude) {
        searchQuery.setValue(query);

        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        searchManager.searchByText(query, latitude, longitude, new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<PlaceEntity> places) {
                isLoading.postValue(false);
                searchResults.postValue(places);
            }

            @Override
            public void onSearchError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}
