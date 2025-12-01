# Quiet Space 🤫

**Quiet Space** is a modern Android application designed to help students, remote workers, and peace-seekers discover and share the best quiet locations in their city. Whether you need a silent library for studying, a cozy cafe for reading, or a serene park for meditation, Quiet Space connects you with the perfect spot.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin)
![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=flat&logo=supabase)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

---

## 📱 Key Features

-   **🔍 Discover Quiet Spots:** Find rated locations based on noise levels, atmosphere, and amenities.
-   **🗺️ Interactive Map:** Visualize quiet places around you using Google Maps integration.
-   **👥 Community Feed:** Share your discoveries with photos, reviews, and ratings.
-   **❤️ Favorites:** Save your go-to spots for quick access.
-   **⭐ Detailed Reviews:** Rate places on Food, Drink, Atmosphere, and Environment.
-   **🔐 Secure Auth:** User accounts managed securely via Supabase.
-   **⚡ High Performance:** Optimized for fast startup and smooth scrolling.

---

## 🛠️ Tech Stack

-   **Language:** Kotlin (Primary), Java
-   **Architecture:** MVVM (Model-View-ViewModel), Repository Pattern
-   **UI:** Material Design 3, XML Layouts, ViewBinding
-   **Backend:** [Supabase](https://supabase.com/) (PostgreSQL, Auth, Storage, Realtime)
-   **Networking:** Ktor Client, OkHttp
-   **Maps & Data:** Google Maps SDK, Google Places API
-   **Image Loading:** Glide (with custom caching configuration)
-   **Build System:** Gradle (Kotlin DSL)

---

## 🚀 Getting Started

### Prerequisites
-   Android Studio Iguana or later.
-   JDK 11 or higher.
-   A Supabase project (for backend).
-   Google Cloud Console project (for Maps & Places API).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/fel1x210/CapstoneProjectUI.git
    ```
2.  **Open in Android Studio:**
    Open the `CapstoneProjectUI` folder.
3.  **Sync Gradle:**
    Allow Android Studio to download dependencies.
4.  **Build & Run:**
    Select your emulator or device and click Run.

### Configuration

The project currently uses hardcoded keys for demonstration purposes. For a production environment, you should replace them:

-   **Google Places API Key:** Located in `app/src/main/java/ca/gbc/comp3074/uiprototype/utils/AppConfig.java`.
-   **Supabase URL & Key:** Located in `app/src/main/java/ca/gbc/comp3074/uiprototype/data/supabase/SupabaseClient.kt`.

---

## 🗄️ Database Setup (Supabase)

To set up the backend, run the following SQL scripts in your Supabase project's **SQL Editor**.

### 1. Core Tables (Profiles & Community)

```sql
-- Profiles Table
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    full_name TEXT,
    avatar_url TEXT,
    bio TEXT,
    places_visited INTEGER DEFAULT 0,
    reviews_count INTEGER DEFAULT 0,
    followers_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can view profiles" ON profiles FOR SELECT USING (true);
CREATE POLICY "Users can insert their own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Users can update their own profile" ON profiles FOR UPDATE USING (auth.uid() = id);

-- Community Posts
CREATE TABLE IF NOT EXISTS community_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name TEXT NOT NULL,
    user_avatar_url TEXT,
    place_name TEXT NOT NULL,
    image_url TEXT NOT NULL,
    caption TEXT DEFAULT '',
    category TEXT NOT NULL CHECK (category IN ('food', 'drink', 'atmosphere', 'environment')),
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);
ALTER TABLE community_posts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can view posts" ON community_posts FOR SELECT USING (true);
CREATE POLICY "Authenticated users can create posts" ON community_posts FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can update their own posts" ON community_posts FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own posts" ON community_posts FOR DELETE USING (auth.uid() = user_id);

-- Post Likes
CREATE TABLE IF NOT EXISTS post_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    UNIQUE(post_id, user_id)
);
ALTER TABLE post_likes ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can view likes" ON post_likes FOR SELECT USING (true);
CREATE POLICY "Authenticated users can like posts" ON post_likes FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Users can unlike their own likes" ON post_likes FOR DELETE USING (auth.uid() = user_id);

-- Post Comments
CREATE TABLE IF NOT EXISTS post_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name TEXT NOT NULL,
    user_avatar_url TEXT,
    comment TEXT NOT NULL,
    rating REAL DEFAULT 0 CHECK (rating >= 0 AND rating <= 5),
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);
ALTER TABLE post_comments ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can view comments" ON post_comments FOR SELECT USING (true);
CREATE POLICY "Authenticated users can comment" ON post_comments FOR INSERT WITH CHECK (auth.role() = 'authenticated');
```

### 2. Favorites Feature

```sql
CREATE TABLE public.user_favorites (
  id uuid not null default gen_random_uuid (),
  user_id uuid not null references auth.users (id) on delete cascade,
  google_place_id text not null,
  name text not null,
  address text,
  rating float,
  user_ratings_total int,
  latitude float,
  longitude float,
  place_type text,
  quiet_score float,
  created_at timestamptz not null default now(),
  constraint user_favorites_pkey primary key (id),
  constraint user_favorites_google_place_id_user_id_key unique (google_place_id, user_id)
);
ALTER TABLE public.user_favorites ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view their own favorites" ON public.user_favorites FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own favorites" ON public.user_favorites FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete their own favorites" ON public.user_favorites FOR DELETE USING (auth.uid() = user_id);
```

### 3. Storage Buckets
Create the following buckets in your Supabase Storage dashboard and set them to **Public**:
-   `avatars`
-   `community-posts`

---

## 📂 Project Structure

```
app/src/main/java/ca/gbc/comp3074/uiprototype/
├── data/           # Data layer (Repositories, Supabase, Room DB)
├── ui/             # UI layer (Activities, Fragments, ViewModels)
│   ├── auth/       # Login/Register screens
│   ├── community/  # Feed, Create Post, Comments
│   ├── details/    # Place details & reviews
│   ├── main/       # Main navigation & Map
│   └── profile/    # User profile & settings
└── utils/          # Helper classes & AppConfig
```

---

## 📅 Recent Updates (Nov 2025)

-   **Community Feed:** Full implementation of social features, including photo sharing, likes, and comments.
-   **Performance Overhaul:**
    -   App startup time reduced by 60-70%.
    -   Memory usage reduced by 50%.
    -   Image loading optimized with Glide caching.
-   **UI Improvements:** Enhanced Material Design 3 components and smoother animations.
-   **Bug Fixes:** Resolved JSON parsing errors and Android 13+ permission issues.

---

## 🤝 Contributing

1.  Fork the project.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

## 📞 Contact

For any questions or support, please contact the repository owner.

*Built with ❤️ for the Capstone Project.*
