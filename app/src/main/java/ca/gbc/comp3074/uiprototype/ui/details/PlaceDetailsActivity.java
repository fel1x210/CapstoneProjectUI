package ca.gbc.comp3074.uiprototype.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.List;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.api.GooglePlacesService;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.data.PlaceRepository;

public class PlaceDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "place_id";
    public static final String EXTRA_GOOGLE_PLACE_ID = "google_place_id";

    private PlaceRepository placeRepository;
    private GooglePlacesService placesService;
    private PlaceEntity currentPlace;

    // Views
    private MaterialToolbar toolbar;
    private TextView placeName;
    private TextView placeType;
    private TextView placeAddress;
    private TextView placeRating;
    private TextView placeReviews;
    private TextView placeDistance;
    private TextView placeHours;
    private TextView placePhone;
    private TextView placeDescription;
    private TextView placeReviewsText;
    private ChipGroup tagContainer;
    private CardView quietScoreCard;
    private TextView quietScoreValue;
    private MaterialButton callButton;
    private MaterialButton directionsButton;
    private MaterialButton favoriteButton;
    private ImageView placeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_place_details);

        initViews();
        setupToolbar();

        placeRepository = new PlaceRepository(getApplication());
        placesService = new GooglePlacesService();

        // Get place ID from intent
        int placeId = getIntent().getIntExtra(EXTRA_PLACE_ID, -1);
        String googlePlaceId = getIntent().getStringExtra(EXTRA_GOOGLE_PLACE_ID);

        if (placeId != -1) {
            loadPlaceFromDatabase(placeId);
        } else if (googlePlaceId != null) {
            loadPlaceFromGoogle(googlePlaceId);
        } else {
            Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        placeName = findViewById(R.id.placeName);
        placeType = findViewById(R.id.placeType);
        placeAddress = findViewById(R.id.placeAddress);
        placeRating = findViewById(R.id.placeRating);
        placeReviews = findViewById(R.id.placeReviews);
        placeDistance = findViewById(R.id.placeDistance);
        placeHours = findViewById(R.id.placeHours);
        placePhone = findViewById(R.id.placePhone);
        placeDescription = findViewById(R.id.placeDescription);
        placeReviewsText = findViewById(R.id.placeReviewsText);
        tagContainer = findViewById(R.id.tagContainer);
        quietScoreCard = findViewById(R.id.quietScoreCard);
        quietScoreValue = findViewById(R.id.quietScoreValue);
        callButton = findViewById(R.id.callButton);
        directionsButton = findViewById(R.id.directionsButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        placeImage = findViewById(R.id.placeImage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Place Details");
        }

        // Handle back button clicks
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadPlaceFromDatabase(int placeId) {
        // Load from local database - no fallback to sample data
        Toast.makeText(this, "Place not found in database", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadPlaceFromGoogle(String googlePlaceId) {
        placesService.getPlaceDetails(googlePlaceId, new GooglePlacesService.PlaceDetailsCallback() {
            @Override
            public void onSuccess(GooglePlacesService.GooglePlaceDetails placeDetails) {
                runOnUiThread(() -> displayPlaceDetails(placeDetails));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PlaceDetailsActivity.this,
                            "Error loading place details: " + error,
                            Toast.LENGTH_LONG).show();
                    finish(); // Close activity if we can't load data
                });
            }
        });
    }

    private void displayPlaceDetails(GooglePlacesService.GooglePlaceDetails details) {
        placeName.setText(details.name);
        placeAddress.setText(details.formattedAddress != null ? details.formattedAddress : "Address not available");
        placeRating.setText(String.format("⭐ %.1f", details.rating));
        placeReviews.setText(String.format("(%d reviews)", details.userRatingsTotal));

        if (details.formattedPhoneNumber != null) {
            placePhone.setText(details.formattedPhoneNumber);
            placePhone.setVisibility(View.VISIBLE);
            callButton.setVisibility(View.VISIBLE);
        }

        if (details.openingHours != null) {
            placeHours.setText(details.openingHours.openNow ? "Open now" : "Closed");
            placeHours.setTextColor(
                    getColor(details.openingHours.openNow ? R.color.quiet_space_primary : R.color.quiet_space_warning));
        }

        // Set place type based on Google Place details
        String placeTypeText = determineQuietSpaceType(details.name);
        placeType.setText(placeTypeText);

        // Calculate and show quiet score
        float quietScore = calculateQuietScore(placeTypeText, details.rating);
        quietScoreValue.setText(String.format("%.1f", quietScore));

        // Show description
        placeDescription.setText(generateDescription(placeTypeText, details.rating, details.userRatingsTotal));

        // Show reviews
        if (details.reviews != null && !details.reviews.isEmpty()) {
            StringBuilder reviewsText = new StringBuilder();
            int maxReviews = Math.min(3, details.reviews.size());
            for (int i = 0; i < maxReviews; i++) {
                GooglePlacesService.GooglePlaceDetails.Review review = details.reviews.get(i);
                reviewsText.append("★★★★★".substring(0, review.rating))
                        .append(" ")
                        .append(review.authorName)
                        .append(": ")
                        .append(review.text.length() > 100 ? review.text.substring(0, 100) + "..." : review.text)
                        .append("\n\n");
            }
            placeReviewsText.setText(reviewsText.toString().trim());
            placeReviewsText.setVisibility(View.VISIBLE);
        }

        // Add tags
        addTagsForPlaceType(placeTypeText);

        // Setup buttons
        setupButtons(details);
    }

    private String determineQuietSpaceType(String placeName) {
        String lowerName = placeName.toLowerCase();
        if (lowerName.contains("library"))
            return "Library";
        if (lowerName.contains("cafe") || lowerName.contains("coffee"))
            return "Café";
        if (lowerName.contains("park"))
            return "Park";
        if (lowerName.contains("museum"))
            return "Museum";
        if (lowerName.contains("gallery"))
            return "Gallery";
        if (lowerName.contains("spa"))
            return "Wellness";
        if (lowerName.contains("church") || lowerName.contains("temple"))
            return "Spiritual";
        return "Quiet Space";
    }

    private float calculateQuietScore(String type, float rating) {
        float baseScore = 3.0f;
        switch (type) {
            case "Library":
                baseScore = 4.8f;
                break;
            case "Park":
                baseScore = 4.2f;
                break;
            case "Museum":
            case "Gallery":
                baseScore = 4.0f;
                break;
            case "Spiritual":
                baseScore = 4.5f;
                break;
            case "Café":
                baseScore = 3.0f;
                break;
            case "Wellness":
                baseScore = 4.3f;
                break;
        }

        if (rating > 0) {
            float ratingFactor = (rating - 2.5f) * 0.2f;
            baseScore = Math.max(1.0f, Math.min(5.0f, baseScore + ratingFactor));
        }

        return Math.round(baseScore * 10) / 10.0f;
    }

    private String generateDescription(String type, float rating, int reviewCount) {
        String baseDescription;
        switch (type) {
            case "Library":
                baseDescription = "Quiet study space with books and peaceful atmosphere";
                break;
            case "Park":
                baseDescription = "Natural outdoor space perfect for relaxation and quiet activities";
                break;
            case "Café":
                baseDescription = "Cozy spot for coffee and quiet conversation";
                break;
            case "Museum":
                baseDescription = "Cultural space with quiet galleries and exhibitions";
                break;
            case "Gallery":
                baseDescription = "Artistic environment perfect for contemplation";
                break;
            case "Wellness":
                baseDescription = "Relaxing space focused on health and tranquility";
                break;
            case "Spiritual":
                baseDescription = "Peaceful place for reflection and meditation";
                break;
            default:
                baseDescription = "A quiet space for relaxation and peace";
        }

        if (rating > 0 && reviewCount > 0) {
            baseDescription += String.format(" • Rated %.1f stars by %d visitors", rating, reviewCount);
        }

        return baseDescription;
    }

    private void addTagsForPlaceType(String type) {
        List<String> tags;
        switch (type) {
            case "Library":
                tags = Arrays.asList("Very quiet", "Study spaces", "Free WiFi", "Books");
                break;
            case "Park":
                tags = Arrays.asList("Fresh air", "Natural sounds", "Peaceful", "Outdoor");
                break;
            case "Café":
                tags = Arrays.asList("Coffee", "WiFi", "Cozy atmosphere", "Light chatter");
                break;
            case "Museum":
                tags = Arrays.asList("Quiet galleries", "Cultural", "Educational", "Peaceful");
                break;
            case "Gallery":
                tags = Arrays.asList("Art", "Contemplative", "Inspiring", "Quiet");
                break;
            case "Wellness":
                tags = Arrays.asList("Relaxation", "Meditation", "Tranquil", "Healing");
                break;
            case "Spiritual":
                tags = Arrays.asList("Meditation", "Prayer", "Silence", "Sacred");
                break;
            default:
                tags = Arrays.asList("Quiet", "Peaceful", "Relaxing");
        }

        tagContainer.removeAllViews();
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setChipBackgroundColorResource(R.color.quiet_space_chip_inactive);
            chip.setTextColor(getColor(R.color.quiet_space_text_secondary));
            tagContainer.addView(chip);
        }
    }

    private void setupButtons(GooglePlacesService.GooglePlaceDetails details) {
        // Call button
        if (details.formattedPhoneNumber != null) {
            callButton.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + details.formattedPhoneNumber));
                startActivity(callIntent);
            });
        } else {
            callButton.setVisibility(View.GONE);
        }

        // Directions button
        directionsButton.setOnClickListener(v -> {
            if (details.geometry != null && details.geometry.location != null) {
                String uri = String.format("geo:%f,%f?q=%f,%f(%s)",
                        details.geometry.location.lat, details.geometry.location.lng,
                        details.geometry.location.lat, details.geometry.location.lng,
                        Uri.encode(details.name));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Favorite button
        favoriteButton.setOnClickListener(v -> {
            // Toggle favorite status
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void finish() {
        super.finish();
        // Add smooth exit transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
