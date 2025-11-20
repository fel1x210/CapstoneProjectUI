package ca.gbc.comp3074.uiprototype.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY rating DESC")
    LiveData<List<PlaceEntity>> getAllPlaces();

    @Query("SELECT * FROM places ORDER BY rating DESC")
    List<PlaceEntity> getAllPlacesSync();

    @Query("SELECT * FROM places WHERE favorite = 1 ORDER BY rating DESC")
    LiveData<List<PlaceEntity>> getFavoritePlaces();

    @Query("SELECT * FROM places WHERE googlePlaceId = :googlePlaceId LIMIT 1")
    PlaceEntity getPlaceByGoogleId(String googlePlaceId);

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    PlaceEntity getPlaceById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlaceEntity> places);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlaceEntity place);

    @Update
    void update(PlaceEntity place);

    @Delete
    void delete(PlaceEntity place);

    @Query("DELETE FROM places")
    void clear();

    @Query("DELETE FROM places")
    void deleteAll();
}
