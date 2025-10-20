package ca.gbc.comp3074.uiprototype.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
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

    public double latitude;
    public double longitude;
    public String address;
    public String description;
    public String phoneNumber;
    public String website;
    public String openingHours;
    
    // Additional fields for Google Places integration
    public String googlePlaceId;
    public String priceLevel;
    public boolean isOpen;
    public float quietScore;
    public String photoReference;
    public String reviews;

    // Default constructor for Room
    public PlaceEntity() {
        this.name = "";
        this.type = "";
        this.distance = "";
        this.lastVisited = "";
        this.emoji = "";
        this.tags = new ArrayList<>();
    }

    @Ignore
    public PlaceEntity(@NonNull String name,
            @NonNull String type,
            @NonNull String distance,
            float rating,
            int reviewCount,
            boolean favorite,
            int checkins,
            @NonNull String lastVisited,
            @NonNull String emoji,
            @NonNull List<String> tags,
            double latitude,
            double longitude,
            String address,
            String description,
            String phoneNumber,
            String website,
            String openingHours) {
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
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.openingHours = openingHours;
        
        // Initialize new fields with defaults
        this.googlePlaceId = "";
        this.priceLevel = "";
        this.isOpen = true;
        this.quietScore = 3.0f;
        this.photoReference = "";
        this.reviews = "";
    }
}
