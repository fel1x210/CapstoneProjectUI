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
                Arrays.asList("Quiet corners", "WiFi", "Specialty brews"),
                43.6532, -79.3832,
                "123 Queen St W, Toronto, ON",
                "A cozy café perfect for reading and studying with excellent coffee and quiet atmosphere.",
                "(416) 555-0123",
                "www.urbanreader.com",
                "Mon-Fri: 7AM-9PM, Sat-Sun: 8AM-10PM"));
        
        places.add(new PlaceEntity(
                "Toronto Reference Library",
                "Library",
                "0.5 miles",
                4.8f,
                128,
                true,
                32,
                "Last week",
                "📚",
                Arrays.asList("Study rooms", "Very quiet", "Research help"),
                43.6702, -79.3866,
                "789 Yonge St, Toronto, ON",
                "The largest public reference library in Canada with extensive study spaces and resources.",
                "(416) 393-7131",
                "www.torontopubliclibrary.ca",
                "Mon-Thu: 9AM-8:30PM, Fri-Sat: 9AM-5PM, Sun: 1:30PM-5PM"));
        
        places.add(new PlaceEntity(
                "WeWork Coworking Space",
                "Coworking",
                "0.8 miles",
                4.8f,
                89,
                false,
                14,
                "3 days ago",
                "💼",
                Arrays.asList("Professional", "Outlets", "Meeting rooms"),
                43.6426, -79.3871,
                "240 Richmond St W, Toronto, ON",
                "Modern coworking space with private offices and shared workspaces.",
                "(416) 555-0456",
                "www.wework.com",
                "Mon-Fri: 8AM-8PM, Sat: 9AM-5PM"));
        
        places.add(new PlaceEntity(
                "Tim Hortons - Quiet Study",
                "Café",
                "1.2 miles",
                4.6f,
                54,
                true,
                9,
                "Yesterday evening",
                "🌇",
                Arrays.asList("Free WiFi", "Outlets", "24/7"),
                43.6519, -79.3817,
                "456 King St W, Toronto, ON",
                "24/7 Tim Hortons with quiet study area and reliable WiFi.",
                "(416) 555-0789",
                "www.timhortons.com",
                "24/7"));
        
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
                Arrays.asList("Natural light", "Quiet zones", "Coffee bar"),
                43.6651, -79.3950,
                "321 Bloor St E, Toronto, ON",
                "Beautiful reading space with natural light and quiet study areas.",
                "(416) 555-0234",
                "www.aurorareading.com",
                "Mon-Fri: 8AM-10PM, Sat-Sun: 9AM-9PM"));
        
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
                Arrays.asList("24/7 access", "Phone booths", "Events"),
                43.7001, -79.4163,
                "567 Eglinton Ave W, Toronto, ON",
                "Modern coworking space with 24/7 access and professional amenities.",
                "(416) 555-0567",
                "www.focushub.com",
                "24/7"));
        
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
                Arrays.asList("Fresh air", "Shade", "Birdsong"),
                43.6789, -79.4012,
                "890 College St, Toronto, ON",
                "Peaceful outdoor space with natural surroundings and fresh air.",
                "(416) 555-0890",
                "www.greenhousecourtyard.com",
                "Daily: 6AM-8PM"));
        
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
                Arrays.asList("Late hours", "Cozy", "Music"),
                43.6482, -79.3742,
                "234 Spadina Ave, Toronto, ON",
                "Cozy late-night café perfect for night owls and students.",
                "(416) 555-0123",
                "www.midnightstudy.com",
                "Daily: 6PM-3AM"));
        
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
                Arrays.asList("River breeze", "Shade", "Picnic tables"),
                43.6319, -79.3716,
                "123 Queens Quay W, Toronto, ON",
                "Scenic outdoor writing space with river views and fresh air.",
                "(416) 555-0456",
                "www.riversidewriting.com",
                "Daily: 7AM-7PM"));
        
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
                Arrays.asList("Workshops", "Fast WiFi", "Community"),
                43.6551, -79.3626,
                "456 King St E, Toronto, ON",
                "Innovative coworking space with workshops and community events.",
                "(416) 555-0789",
                "www.innovationloft.com",
                "Mon-Fri: 7AM-9PM, Sat: 9AM-6PM"));
        return places;
    }
}
