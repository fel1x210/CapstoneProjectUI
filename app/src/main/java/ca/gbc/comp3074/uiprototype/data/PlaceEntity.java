package ca.gbc.comp3074.uiprototype.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "places")
public class PlaceEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String type;

    @NonNull
    public String distance;

    public float rating;

    public int reviewCount;

    public boolean favorite;

    public int checkins;

    @NonNull
    public String lastVisited;

    @NonNull
    public String emoji;

    @NonNull
    public List<String> tags;

    public PlaceEntity(@NonNull String name,
            @NonNull String type,
            @NonNull String distance,
            float rating,
            int reviewCount,
            boolean favorite,
            int checkins,
            @NonNull String lastVisited,
            @NonNull String emoji,
            @NonNull List<String> tags) {
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.favorite = favorite;
        this.checkins = checkins;
        this.lastVisited = lastVisited;
        this.emoji = emoji;
        this.tags = tags;
    }
}
