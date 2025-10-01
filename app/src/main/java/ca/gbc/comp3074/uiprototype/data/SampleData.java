package ca.gbc.comp3074.uiprototype.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class SampleData {

    private SampleData() {
    }

    static List<PlaceEntity> getPlaces() {
        List<PlaceEntity> places = new ArrayList<>();
        places.add(new PlaceEntity(
                "The Urban Reader Café",
                "Café",
                "0.2 miles",
                4.7f,
                362,
                true,
                18,
                "2 days ago",
                "☕",
                Arrays.asList("Quiet corners", "WiFi", "Specialty brews")));
        places.add(new PlaceEntity(
                "Central Library",
                "Library",
                "0.5 miles",
                4.8f,
                128,
                true,
                32,
                "Last week",
                "📚",
                Arrays.asList("Study rooms", "Very quiet", "Research help")));
        places.add(new PlaceEntity(
                "Peaceful Corner Coworking",
                "Coworking",
                "0.8 miles",
                4.8f,
                89,
                false,
                14,
                "3 days ago",
                "💼",
                Arrays.asList("Professional", "Outlets", "Meeting rooms")));
        places.add(new PlaceEntity(
                "Sunset Study Lounge",
                "Lounge",
                "1.2 miles",
                4.6f,
                54,
                true,
                9,
                "Yesterday evening",
                "🌇",
                Arrays.asList("Sunset view", "Soft seating", "Snacks")));
        places.add(new PlaceEntity(
                "Aurora Reading Atrium",
                "Library",
                "1.5 miles",
                4.9f,
                204,
                false,
                21,
                "3 days ago",
                "🌌",
                Arrays.asList("Natural light", "Quiet zones", "Coffee bar")));
        places.add(new PlaceEntity(
                "Focus Hub Midtown",
                "Coworking",
                "2.0 miles",
                4.4f,
                178,
                false,
                12,
                "1 week ago",
                "🏙",
                Arrays.asList("24/7 access", "Phone booths", "Events")));
        places.add(new PlaceEntity(
                "Greenhouse Courtyard",
                "Garden",
                "2.3 miles",
                4.3f,
                96,
                false,
                6,
                "4 days ago",
                "🌿",
                Arrays.asList("Fresh air", "Shade", "Birdsong")));
        places.add(new PlaceEntity(
                "Midnight Study Café",
                "Café",
                "0.9 miles",
                4.5f,
                147,
                true,
                24,
                "Tonight",
                "🌙",
                Arrays.asList("Late hours", "Cozy", "Music")));
        places.add(new PlaceEntity(
                "Riverside Writing Deck",
                "Outdoor",
                "3.3 miles",
                4.2f,
                63,
                false,
                4,
                "Last weekend",
                "🌊",
                Arrays.asList("River breeze", "Shade", "Picnic tables")));
        places.add(new PlaceEntity(
                "Innovation Loft",
                "Coworking",
                "1.9 miles",
                4.6f,
                112,
                true,
                17,
                "This morning",
                "🚀",
                Arrays.asList("Workshops", "Fast WiFi", "Community")));
        return places;
    }
}
