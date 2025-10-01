package ca.gbc.comp3074.uiprototype.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY rating DESC")
    LiveData<List<PlaceEntity>> getAllPlaces();

    @Query("SELECT * FROM places WHERE favorite = 1 ORDER BY rating DESC")
    LiveData<List<PlaceEntity>> getFavoritePlaces();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlaceEntity> places);

    @Query("DELETE FROM places")
    void clear();
}
