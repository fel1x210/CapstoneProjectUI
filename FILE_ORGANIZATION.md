# 📁 Project File Organization Guide

**Last Updated:** October 24, 2025  
**Total Files Organized:** 68 resource files (28 layouts + 40 drawables)  
**Unused Files Cleaned:** 5 duplicate/unused files deleted (2 drawables + 3 data models)

---

## 🚀 How to Use This Guide

**For Presentations:**
1. Open this file to quickly reference any file location
2. Show "Visual Organization Map" to explain structure
3. Use "Your Community Feature Files" section to highlight your work

**For Development:**
1. Use Quick Navigation tables to find files
2. Search by prefix (activity_, fragment_, ic_, bg_)
3. Check "Quick Reference" section for common questions

**For Navigation in VS Code:**
- Press `Ctrl + P` → Type filename → Enter
- Press `Ctrl + F` in this document → Search feature name

---

## 📋 Quick Stats

| Resource Type | Count | Organized By |
|--------------|-------|--------------|
| 🏠 Activities | 10 | `activity_*.xml` prefix |
| 📄 Fragments | 6 | `fragment_*.xml` prefix |
| 🎴 RecyclerView Items | 7 | `item_*.xml` prefix |
| 💬 Dialogs | 5 | `dialog_*.xml` prefix |
| **📱 Total Layouts** | **28** | **Prefix-based naming** |
| | | |
| 🎨 Background Shapes | 11 | `bg_*.xml` prefix |
| 🔷 Icons | 24 | `ic_*.xml` prefix |
| 🌈 Gradients | 2 | `gradient_*.xml` prefix |
| 🚀 Logos | 1 | `logo_*.xml` prefix |
| 🖼️ Placeholders | 1 | `placeholder_*.xml` prefix |
| **🎨 Total Drawables** | **40** | **Prefix-based naming** |

⚠️ **Android Constraint:** Resource folders (`layout/`, `drawable/`) cannot have subdirectories.  
✅ **Solution:** Files are organized using consistent naming prefixes for easy searching.

---

## 📊 Visual Organization Map

```
res/
├── layout/ (28 files - organized by prefix)
│   ├── activity_*.xml ──┐
│   │                     ├─→ 10 Activity Screens
│   │                     │   (login, register, main, create_post, etc.)
│   │                     │
│   ├── fragment_*.xml ──┐
│   │                     ├─→ 6 Fragment Tabs
│   │                     │   (home, search, community, favorites, profile, map)
│   │                     │
│   ├── item_*.xml ──────┐
│   │                     ├─→ 7 RecyclerView Items
│   │                     │   (community_post, comment, place_card, etc.)
│   │                     │
│   └── dialog_*.xml ────┐
│                         ├─→ 5 Dialog Popups
│                         │   (edit_profile, change_password, etc.)
│
└── drawable/ (40 files - organized by prefix)
    ├── bg_*.xml ────────┐
    │                     ├─→ 11 Background Shapes
    │                     │   (soft_card, place_card, cosmic_gradient, etc.)
    │                     │
    ├── ic_*.xml ────────┐
    │                     ├─→ 24 Vector Icons
    │                     │   Navigation: home, search, community, favorite, profile
    │                     │   Community: heart_filled, heart_outline, comment, send
    │                     │   Actions: add, arrow_back, directions, refresh, etc.
    │                     │   Forms: email, lock, person, phone, location
    │                     │   Branding: launcher icons, quietspace_logo
    │                     │
    ├── gradient_*.xml ──┐
    │                     ├─→ 2 Gradient Backgrounds
    │                     │
    ├── logo_*.xml ──────┐
    │                     ├─→ 1 Logo Asset
    │                     │
    └── placeholder_*.xml ┐
                          ├─→ 1 Placeholder Image
```

---

## 🗄️ Database Architecture (Dual System)

Your app uses **TWO different database systems** for different purposes:

### 📍 Room Database (Local Storage)
**Purpose:** Storing map places data locally for offline access

**Files Used:**
- `QuietSpaceDatabase.java` - Room database setup
- `PlaceEntity.java` - Place model with Room annotations
- `PlaceDao.java` - Data Access Object for places
- `PlaceRepository.java` - Repository pattern for place operations
- `MapDataManager.java` - Manages map places from Google Places API
- `Converters.java` - Type converters for Room

**Used By:**
- Home Fragment (map with places)
- Search Fragment (search places)
- Favorites Fragment (saved places)
- Map Fragment (full map view)
- Place Details Activity

**Why Room?**
- Caches Google Places API results locally
- Works offline
- Fast access for map markers
- No authentication needed

---

### ☁️ Supabase (Cloud Database)
**Purpose:** Community features with real-time sync and authentication

**Files Used:**
- `SupabaseClient.kt` - Supabase connection setup
- `SupabaseCommunityRepository.kt` - Community CRUD operations
- `SupabaseAuthRepository.kt` - User authentication
- `SupabaseStorageRepository.kt` - Image uploads
- `data/supabase/models/CommunityPost.kt` - Post model (Kotlin)
- `data/supabase/models/UserProfile.kt` - User model

**Used By:**
- Community Fragment (feed)
- Create Post Activity (new posts)
- Post Comments Activity (comments & ratings)
- Authentication (login/register)
- Profile management

**Why Supabase?**
- Real-time updates across users
- Cloud storage for images
- User authentication & authorization
- Social features (likes, comments)

---

### ✅ Why Both?

| Feature | Room (Local) | Supabase (Cloud) |
|---------|--------------|------------------|
| **Data Type** | Places/Locations | User-generated content |
| **Source** | Google Places API | Users |
| **Sync** | One-way (API → Local) | Two-way (Real-time) |
| **Authentication** | Not needed | Required |
| **Offline** | ✅ Yes | ❌ No |
| **Sharing** | ❌ Private per device | ✅ Public to all users |

**Example User Flow:**
1. User opens app → **Room** loads cached places on map
2. User searches "coffee" → **Room** queries local database + Google API
3. User creates post about a café → **Supabase** stores with real-time sync
4. Other users see post → **Supabase** fetches community feed
5. User saves café as favorite → **Room** stores locally

---

## 🎯 Quick Navigation Index

---

## 📱 Activities (Screens)

### Authentication Flow
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_welcome.xml` | First screen with logo | ✅ Main Launcher |
| `activity_login.xml` | Login form | From Welcome |
| `activity_register.xml` | Sign up form | From Welcome |

### Main App
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_main.xml` | Bottom navigation container | After Login |

### Profile & Settings
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_edit_profile.xml` | Edit user profile | Profile Fragment |
| `activity_privacy_settings.xml` | Privacy settings | Profile Fragment |

### Places
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_place_details.xml` | Place information | Tap on place card |

### Community (Your Feature)
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_create_post.xml` | Create new post | Community FAB button |
| `activity_post_comments.xml` | View/add comments | Tap comment button |

### Animation (Optional Feature)
| File | Purpose | Entry Point |
|------|---------|-------------|
| `activity_welcome_animation.xml` | Animated welcome | Optional launcher |

---

## 📄 Fragments (Tab Content)

| File | Tab | Purpose |
|------|-----|---------|
| `fragment_home.xml` | 🏠 Home | Map with places |
| `fragment_search.xml` | 🔍 Search | Search places |
| `fragment_community.xml` | 👥 Community | **Social feed (YOUR WORK)** |
| `fragment_favorites.xml` | ⭐ Favorites | Saved places |
| `fragment_profile.xml` | 👤 Profile | User profile |
| `fragment_map.xml` | 🗺️ Map | Full map view |

---

## 🎴 Item Layouts (RecyclerView Items)

### Community Feed
| File | Used In | Purpose |
|------|---------|---------|
| `item_community_post.xml` | CommunityFragment | **Post card in feed** |
| `item_comment.xml` | PostCommentsActivity | **Comment with rating** |

### Places
| File | Used In | Purpose |
|------|---------|---------|
| `item_place_card.xml` | HomeFragment, SearchFragment | Place card display |
| `item_favorite_card.xml` | FavoritesFragment | Saved place card |
| `item_map_preview.xml` | MapFragment | Map marker preview |

### Categories & Search
| File | Used In | Purpose |
|------|---------|---------|
| `item_category_card.xml` | HomeFragment | Category selection |
| `item_recent_search.xml` | SearchFragment | Recent search item |

---

## 💬 Dialogs (Popups)

### Profile Dialogs
| File | Purpose | Triggered By |
|------|---------|--------------|
| `dialog_edit_profile.xml` | Quick profile edit | Profile Fragment |
| `dialog_change_password.xml` | Change password | Privacy Settings |
| `dialog_update_email.xml` | Update email | Privacy Settings |
| `dialog_password_confirm.xml` | Confirm password | Before sensitive actions |

### Animation Dialog
| File | Purpose | Triggered By |
|------|---------|--------------|
| `dialog_welcome_animation.xml` | Show animation popup | Optional |

---

## 🎨 XML File Naming Convention

⚠️ **Important Android Constraint:**
- Android **does NOT allow subdirectories** in `res/layout/` or `res/drawable/`
- All resource files must be in the root of their respective folders
- We use **naming prefixes** instead for organization

### Layout Files
Our project follows standard Android naming:

### Activities
- **Pattern**: `activity_[name].xml`
- **Examples**: `activity_login.xml`, `activity_main.xml`

### Fragments
- **Pattern**: `fragment_[name].xml`
- **Examples**: `fragment_home.xml`, `fragment_community.xml`

### RecyclerView Items
- **Pattern**: `item_[name].xml`
- **Examples**: `item_community_post.xml`, `item_comment.xml`

### Dialogs
- **Pattern**: `dialog_[name].xml`
- **Examples**: `dialog_edit_profile.xml`

---

## 🗂️ Complete File Structure

```
app/src/main/
├── java/ca/gbc/comp3074/uiprototype/
│   ├── QuietSpaceApp.java                    ← Application class
│   │
│   ├── ui/
│   │   ├── auth/                             ← Authentication
│   │   │   ├── WelcomeActivity.java
│   │   │   ├── LoginActivity.java
│   │   │   └── RegisterActivity.java
│   │   │
│   │   ├── main/                             ← Main container
│   │   │   └── MainActivity.java
│   │   │
│   │   ├── home/                             ← Home tab
│   │   │   └── HomeFragment.java
│   │   │
│   │   ├── search/                           ← Search tab
│   │   │   └── SearchFragment.java
│   │   │
│   │   ├── community/                        ← Community tab (YOUR WORK)
│   │   │   ├── CommunityFragment.kt          ← Feed display
│   │   │   ├── CreatePostActivity.kt         ← Create post
│   │   │   ├── PostCommentsActivity.kt       ← Comments view
│   │   │   ├── CommunityPostAdapter.kt       ← Post adapter
│   │   │   └── CommentsAdapter.kt            ← Comment adapter
│   │   │
│   │   ├── favorites/                        ← Favorites tab
│   │   │   └── FavoritesFragment.java
│   │   │
│   │   ├── profile/                          ← Profile tab
│   │   │   ├── ProfileFragment.java
│   │   │   ├── EditProfileActivity.java
│   │   │   └── PrivacySettingsActivity.java
│   │   │
│   │   ├── places/
│   │   │   └── PlaceDetailsActivity.java
│   │   │
│   │   ├── map/
│   │   │   └── MapFragment.java
│   │   │
│   │   └── animation/
│   │       ├── WelcomeAnimationActivity.java
│   │       └── WelcomeAnimationDialog.java
│   │
│   ├── data/
│   │   ├── QuietSpaceDatabase.java           ← Room database
│   │   │
│   │   ├── supabase/                         ← Supabase integration
│   │   │   ├── SupabaseClient.kt             ← Client setup
│   │   │   ├── SupabaseAuthRepository.kt     ← Auth operations
│   │   │   ├── SupabaseStorageRepository.kt  ← File uploads
│   │   │   ├── SupabaseCommunityRepository.kt ← Community operations
│   │   │   │
│   │   │   └── models/
│   │   │       ├── UserProfile.kt
│   │   │       ├── CommunityPost.kt          ← Post model
│   │   │       ├── PostComment.kt            ← Comment model (embedded)
│   │   │       └── PostLike.kt               ← Like model (embedded)
│   │   │
│   │   └── models/                           ← Room entities
│   │       ├── Place.java
│   │       ├── Category.java
│   │       └── FavoritePlace.java
│   │
│   ├── utils/                                ← Utility classes
│   │   ├── AppConfig.java
│   │   ├── PreferencesManager.java
│   │   └── GlideConfiguration.kt             ← Image loading config
│   │
│   └── adapters/                             ← Non-community adapters
│       ├── PlaceAdapter.java
│       ├── CategoryAdapter.java
│       └── FavoriteAdapter.java
│
└── res/
    ├── layout/                               ← All XML layouts (28 files)
    │   ├── activity_*.xml                    ← 10 activities
    │   ├── fragment_*.xml                    ← 6 fragments
    │   ├── item_*.xml                        ← 7 item layouts
    │   └── dialog_*.xml                      ← 5 dialogs
    │   
    │   ⚠️ NOTE: Android doesn't support subdirectories in res/layout/
    │            Files are organized by naming prefix instead
    │
    ├── drawable/                             ← Icons & graphics (40 files)
    │   ├── bg_*.xml                          ← 11 Background shapes
    │   ├── gradient_*.xml                    ← 2 Gradient backgrounds
    │   ├── ic_*.xml                          ← 24 Vector icons
    │   ├── logo_*.xml                        ← 1 Logo
    │   └── placeholder_*.xml                 ← 1 Placeholder
    │   
    │   ⚠️ NOTE: Android doesn't support subdirectories in res/drawable/
    │            Files are organized by naming prefix instead
    │
    ├── values/
    │   ├── colors.xml                        ← App colors
    │   ├── strings.xml                       ← Text strings
    │   ├── themes.xml                        ← App theme
    │   └── styles.xml                        ← Reusable styles
    │
    └── menu/
        └── bottom_nav_menu.xml               ← Bottom navigation items
```

---

## 🎯 Your Community Feature Files

### Kotlin Files (Logic)
1. **CommunityFragment.kt** (226 lines)
   - Location: `ui/community/`
   - Layout: `fragment_community.xml`
   - Purpose: Main feed display with RecyclerView

2. **CreatePostActivity.kt** (269 lines)
   - Location: `ui/community/`
   - Layout: `activity_create_post.xml`
   - Purpose: Create new post with image

3. **PostCommentsActivity.kt** (180+ lines)
   - Location: `ui/community/`
   - Layout: `activity_post_comments.xml`
   - Purpose: View/add comments

4. **CommunityPostAdapter.kt** (154 lines)
   - Location: `ui/community/`
   - Item Layout: `item_community_post.xml`
   - Purpose: Display posts in RecyclerView

5. **CommentsAdapter.kt** (99 lines)
   - Location: `ui/community/`
   - Item Layout: `item_comment.xml`
   - Purpose: Display comments in RecyclerView

### Repository & Models
6. **SupabaseCommunityRepository.kt** (337 lines)
   - Location: `data/supabase/`
   - Purpose: All community database operations

7. **CommunityPost.kt**
   - Location: `data/supabase/models/`
   - Purpose: Post data model

8. **PostComment.kt** (embedded in CommunityPost.kt)
   - Purpose: Comment data model

9. **PostLike.kt** (embedded in CommunityPost.kt)
   - Purpose: Like data model

### XML Layouts
10. **fragment_community.xml**
    - Components: Toolbar, SwipeRefresh, RecyclerView, FAB, Empty State

11. **activity_create_post.xml**
    - Components: Image preview, ChipGroup (categories), Input fields

12. **activity_post_comments.xml**
    - Components: Comments RecyclerView, RatingBar, Input field

13. **item_community_post.xml**
    - Components: User header, Post image, Category chip, Like/Comment buttons

14. **item_comment.xml**
    - Components: User avatar, Name, RatingBar, Comment text

---

## 🎨 Drawable Resources (Icons & Backgrounds)

### 📐 Background Shapes (11 files)
| File | Purpose | Used In |
|------|---------|---------|
| `bg_bottom_sheet.xml` | Bottom sheet background | Dialogs |
| `bg_cosmic_gradient.xml` | Cosmic theme gradient | Welcome screens |
| `bg_cosmic_overlay.xml` | Cosmic overlay effect | Welcome screens |
| `bg_image_placeholder.xml` | Image loading placeholder | All image views |
| `bg_place_card.xml` | Place card background | Place items |
| `bg_social_button.xml` | Social media button style | Login/Register |
| `bg_soft_card.xml` | Soft card background | Cards |
| `bg_soft_chip.xml` | Chip background | Category chips, Post tags |
| `bg_surface_card.xml` | Surface card background | Main cards |
| `gradient_background_welcome.xml` | Welcome screen gradient | Welcome Activity |
| `gradient_overlay.xml` | General gradient overlay | Various screens |

### 🔷 Navigation Icons (5 files)
| File | Purpose | Used In |
|------|---------|---------|
| `ic_home.xml` | Home tab icon | Bottom Navigation |
| `ic_search_nav.xml` | Search tab icon | Bottom Navigation |
| `ic_community.xml` | **Community tab icon (YOUR WORK)** | Bottom Navigation |
| `ic_favorite.xml` | Favorites tab icon | Bottom Navigation |
| `ic_profile.xml` | Profile tab icon | Bottom Navigation |

### ❤️ Community Feature Icons (YOUR WORK)
| File | Purpose | Used In |
|------|---------|---------|
| `ic_heart_outline.xml` | **Unlike button** | Post card |
| `ic_heart_filled.xml` | **Liked button (red)** | Post card |
| `ic_comment.xml` | **Comment button** | Post card |
| `ic_send.xml` | **Send comment button** | Comments screen |
| `ic_image.xml` | **Add image button** | Create post |

### 🔧 Action Icons (8 files)
| File | Purpose | Used In |
|------|---------|---------|
| `ic_add.xml` | Add/Create action | FAB buttons |
| `ic_arrow_back.xml` | Back navigation | Toolbar |
| `ic_arrow_right.xml` | Forward navigation | Lists |
| `ic_directions.xml` | Get directions | Place details |
| `ic_favorite_border.xml` | Unfavorited state | Place cards |
| `ic_more_vert.xml` | More options menu | Post cards |
| `ic_refresh.xml` | Refresh content | Pull to refresh |
| `ic_search.xml` | Search action | Search bar |

### 👤 Form & Profile Icons (6 files)
| File | Purpose | Used In |
|------|---------|---------|
| `ic_email.xml` | Email input icon | Login/Register |
| `ic_lock.xml` | Password input icon | Login/Register |
| `ic_person.xml` | User icon | Profile |
| `ic_phone.xml` | Phone input icon | Profile edit |
| `ic_location.xml` | Location icon | Place cards |
| `ic_schedule.xml` | Time/Schedule icon | Place details |

### 🚀 App Branding (4 files)
| File | Purpose | Used In |
|------|---------|---------|
| `ic_launcher_foreground.xml` | App icon foreground | App icon |
| `ic_launcher_background.xml` | App icon background | App icon |
| `ic_quietspace_logo.xml` | App logo | Welcome screen |
| `logo_circle_bg.xml` | Logo circle background | Welcome screen |

### 🖼️ Placeholders (1 file)
| File | Purpose | Used In |
|------|---------|---------|
| `placeholder_place.xml` | Place image placeholder | Place details |

---

## 🎯 Drawable Naming Convention

Our drawable files follow these prefixes:

| Prefix | Purpose | Example |
|--------|---------|---------|
| `bg_` | Background shapes | `bg_soft_card.xml` |
| `gradient_` | Gradient backgrounds | `gradient_overlay.xml` |
| `ic_` | Icons | `ic_heart_filled.xml` |
| `logo_` | Logo assets | `logo_circle_bg.xml` |
| `placeholder_` | Placeholder images | `placeholder_place.xml` |

---

## 🗑️ Unused Files Status

### ✅ Cleaned Up:

**Drawable Duplicates:**
- ❌ Deleted: `ic_launcher_foreground_backup.xml` (unused duplicate)
- ❌ Deleted: `ic_launcher_foreground_new.xml` (unused duplicate)

**Data Model Duplicates:**
- ❌ Deleted: `data/model/CommunityPost.java` (replaced by Kotlin version in `supabase/models/`)
- ❌ Deleted: `data/model/PostComment.java` (embedded in CommunityPost.kt)
- ❌ Deleted: `data/model/` directory (empty after cleanup)

**Why Were These Duplicates?**
- Old Java models created before switching to Kotlin + Supabase
- Kotlin models in `supabase/models/` have `@Serializable` for JSON
- New models are actively used, old Java versions were abandoned

### Current Status:
- ✅ All 28 layout files are IN USE
- ✅ All 40 drawable files are IN USE (after cleanup)
- ✅ All data models verified - Room for places, Supabase for community
- ✅ No duplicate or unused files remaining
- ✅ Build successful after cleanup

---

## 🔍 How to Find Files Quickly

### In Android Studio:
1. **Ctrl + Shift + N** → Type filename → Enter
2. **Ctrl + N** → Type class name → Enter
3. **Project view** → Filter by file type

### File Naming Pattern Recognition:
- See `activity_` → It's a screen
- See `fragment_` → It's a tab content
- See `item_` → It's a RecyclerView item
- See `dialog_` → It's a popup

### Your Files (Community):
Search for: `community`, `post`, `comment`
- Will find all your related files instantly

---

## 💡 Quick Tips

### When Creating New Files:
1. **Activities**: `activity_[name].xml`
2. **Fragments**: `fragment_[name].xml`
3. **List Items**: `item_[name].xml`
4. **Dialogs**: `dialog_[name].xml`

### When Looking for a Feature:
1. Identify type: Activity? Fragment? Item?
2. Search by prefix: `activity_`, `fragment_`, `item_`
3. Or search by feature name: `community`, `profile`, `login`

### File Navigation Shortcuts:
- **Ctrl + Click** on R.layout.name → Opens XML
- **Alt + F7** → Find usages of layout
- **Ctrl + F12** → Show file structure

---

## 📊 File Count Summary

| Category | Count | Examples |
|----------|-------|----------|
| **Layouts** | | |
| Activities | 10 | activity_main, activity_create_post |
| Fragments | 6 | fragment_community, fragment_home |
| Items | 7 | item_community_post, item_comment |
| Dialogs | 5 | dialog_edit_profile |
| **Total Layouts** | **28** | All organized and in use |
| | | |
| **Drawables** | | |
| Backgrounds | 11 | bg_soft_card, gradient_overlay |
| Icons | 24 | ic_heart_filled, ic_comment |
| Logos | 1 | logo_circle_bg |
| Placeholders | 1 | placeholder_place |
| **Total Drawables** | **40** | ~~42~~ (2 unused deleted) |
| | | |
| **Total Resources** | **68** | All organized by prefix |

---

## ✅ Organization Checklist

- ✅ All files follow naming conventions
- ✅ No duplicate files (2 unused launcher icons deleted)
- ✅ No unused layouts (all 28 referenced in code)
- ✅ No unused drawables (all 40 referenced in code)
- ✅ Clear prefix-based organization (Android constraint)
- ✅ Your community files clearly identified
- ✅ Easy to navigate and find files
- ⚠️ Cannot use subdirectories (Android limitation)

---

## 🎯 Quick Reference: "Where is X?"

**Q: Where's the main screen?**
A: `activity_main.xml` + `MainActivity.java`

**Q: Where's the feed?**
A: `fragment_community.xml` + `CommunityFragment.kt`

**Q: Where's the post creation?**
A: `activity_create_post.xml` + `CreatePostActivity.kt`

**Q: Where's the post card design?**
A: `item_community_post.xml` + `CommunityPostAdapter.kt`

**Q: Where's the database code?**
A: `data/supabase/SupabaseCommunityRepository.kt`

**Q: Where are the data models?**
A: `data/supabase/models/CommunityPost.kt`

**Q: Where are the heart/like icons?**
A: `ic_heart_outline.xml` + `ic_heart_filled.xml` (drawable folder)

**Q: Where are the community tab icons?**
A: `ic_community.xml`, `ic_comment.xml`, `ic_send.xml` (drawable folder)

**Q: Where are the background styles?**
A: Search `bg_*.xml` in drawable folder (11 files)

**Q: Where's the app logo?**
A: `ic_quietspace_logo.xml` + `logo_circle_bg.xml` (drawable folder)

---

## 🔍 Quick Search Tips

### Finding Layout Files:
- **All activities**: Search `activity_` → 10 files
- **All fragments**: Search `fragment_` → 6 files
- **All list items**: Search `item_` → 7 files
- **All dialogs**: Search `dialog_` → 5 files
- **Your work**: Search `community`, `post`, `comment`

### Finding Drawable Files:
- **All backgrounds**: Search `bg_` → 11 files
- **All icons**: Search `ic_` → 24 files
- **Community icons**: Search `heart`, `comment`, `send`
- **Navigation icons**: Search `nav` → 5 bottom tab icons
- **Gradients**: Search `gradient_` → 2 files

---

**Everything is organized, documented, and ready for presentation!** 🚀

---

## ✅ What Was Organized

### 🗑️ Cleanup Completed:

#### Drawable Resources:
1. ❌ **Deleted:** `ic_launcher_foreground_backup.xml` (unused duplicate)
2. ❌ **Deleted:** `ic_launcher_foreground_new.xml` (unused duplicate)

#### Data Models:
3. ❌ **Deleted:** `data/model/CommunityPost.java` (replaced by Kotlin version)
4. ❌ **Deleted:** `data/model/PostComment.java` (replaced by Kotlin version)
5. ❌ **Deleted:** `data/model/` directory (now empty after cleanup)

#### Verification:
- ✅ **All 28 layouts** are actively used (no duplicates)
- ✅ **All 40 drawables** are actively used (after cleanup)
- ✅ **All data models** verified - Room DB for places, Supabase for community
- ✅ **Build successful** after cleanup (no broken references)
- ✅ **Total files removed:** 5 unused/duplicate files

### 📁 Organization Method:
Since Android doesn't support subdirectories in `res/layout/` and `res/drawable/`, files are organized using **consistent naming prefixes**:

**Layouts:**
- `activity_*.xml` → 10 activity screens
- `fragment_*.xml` → 6 fragment tabs
- `item_*.xml` → 7 RecyclerView items
- `dialog_*.xml` → 5 dialog popups

**Drawables:**
- `bg_*.xml` → 11 background shapes
- `ic_*.xml` → 24 icons
- `gradient_*.xml` → 2 gradients
- `logo_*.xml` → 1 logo
- `placeholder_*.xml` → 1 placeholder

### 📖 Documentation Created:
This comprehensive guide includes:
- ✅ File count summary (68 total resources)
- ✅ Complete file categorization
- ✅ Purpose and usage for each file
- ✅ Quick navigation tables
- ✅ Your community feature files highlighted
- ✅ Search tips for finding files fast
- ✅ Android constraint explanations

---

**Project is clean, organized, and ready for your professor presentation!** 🎓
