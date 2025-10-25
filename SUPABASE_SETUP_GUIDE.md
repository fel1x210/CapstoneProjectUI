# 🚀 QuietSpace - Supabase Setup Guide

## ⚠️ CRITICAL: Fix Email Confirmation Issue

The error you're seeing (`localhost:3000/#error=access_denied&error_code=otp_expired`) is because **email confirmation is enabled** in Supabase. This MUST be disabled for mobile apps.

### 🔧 STEP 1: Disable Email Confirmation (REQUIRED!)

1. **Go to your Supabase Dashboard**: https://itwqcyumcrqqqetoqgai.supabase.co
2. **Navigate to**: Authentication → Settings → Email Auth
3. **Find**: "Enable email confirmations"
4. **TOGGLE IT OFF** ❌ (Disable it)
5. **Save Changes**

**Why?** Mobile apps can't handle email confirmation links that redirect to `localhost:3000`. This causes the authentication to fail.

---

## 📊 STEP 2: Run the Database Schema

1. **Open**: Supabase Dashboard → SQL Editor
2. **Click**: "+ New Query"
3. **Copy**: The entire contents of `supabase_schema.sql`
4. **Paste**: Into the SQL editor
5. **Click**: "Run" or press `Ctrl+Enter`

This will create:

- ✅ `profiles` table
- ✅ `community_posts` table
- ✅ `post_likes` table
- ✅ `post_comments` table
- ✅ Storage buckets (`avatars`, `community-posts`)
- ✅ All RLS policies
- ✅ All indexes

---

## ✅ STEP 3: Verify Setup

### Check Tables

Run this in SQL Editor:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_name IN ('profiles', 'community_posts', 'post_likes', 'post_comments');
```

**Expected Result**: Should show all 4 tables

### Check RLS (Row Level Security)

```sql
SELECT tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public';
```

**Expected Result**: All tables should have `rowsecurity = true`

### Check Storage Buckets

Go to: **Storage** → Should see:

- ✅ `avatars` (public)
- ✅ `community-posts` (public)

---

## 🧪 STEP 4: Test Registration

### Delete Previous Failed Users (if any)

If you had failed registration attempts:

1. **Go to**: Authentication → Users
2. **Delete**: Any test users you created
3. **Go to**: Table Editor → `profiles`
4. **Delete**: Any orphaned profile records

### Try New Registration

1. **Open** your app
2. **Tap**: "Sign Up"
3. **Fill in**:
   - Full Name: `Test User`
   - Email: `test@quietspace.com`
   - Password: `Test123456`
   - Confirm Password: `Test123456`
4. **Check**: "I agree to Terms..."
5. **Tap**: "Create Account"

### Expected Behavior:

- ✅ **No email confirmation required**
- ✅ **Immediately logged in**
- ✅ **No database errors**
- ✅ **Profile created successfully**

---

## 🐛 Troubleshooting

### Error: "User ID not found after signup"

**Solution**:

1. Disable email confirmation (Step 1)
2. Rebuild app: `./gradlew clean build`
3. Try again

### Error: "Database error. Please ensure the 'profiles' table exists"

**Solution**:

1. Run the SQL schema (Step 2)
2. Verify table exists (Step 3)

### Error: "Account already exists"

**Solution**:

- Try logging in instead of registering
- OR delete the user from Supabase dashboard

### Error: "Permission denied"

**Solution**:

1. Check RLS policies are created
2. Verify you're logged in (check Authentication → Users)

### Email Still Being Sent?

**Solution**:

1. Go to: Authentication → Settings
2. Scroll to: **Email Templates**
3. Confirm: "Confirm signup" is disabled
4. Check: **Auth Providers** → Email → Confirm email should be OFF

---

## 📱 STEP 5: Test All Features

After successful registration, test:

### ✅ Profile

- View profile
- Edit profile
- Upload avatar
- Change settings

### ✅ Home/Map

- View map
- See location markers
- Click on places

### ✅ Search

- Search for places
- Try different categories
- View place details

### ✅ Community

- Create a post
- Upload image
- Like posts
- Add comments with ratings

### ✅ Favorites

- Add places to favorites
- Remove from favorites
- View favorites list

---

## 🔐 Security Checklist

✅ Email confirmation is **DISABLED** for mobile app
✅ All tables have Row Level Security (RLS) enabled
✅ Storage buckets are public (for image viewing)
✅ Upload/Delete policies restrict to authenticated users
✅ Users can only modify their own content

---

## 📝 Additional Configuration

### Auto-Refresh Tokens

Go to: Authentication → Settings → Token Settings

- **JWT Expiry**: Set to 3600 (1 hour) or higher
- **Refresh Token Rotation**: Enable

### Database Backups

Go to: Settings → Database

- **Enable automatic backups**: ON
- **Backup retention**: 7 days minimum

---

## 🆘 Still Having Issues?

1. **Check Logs**:

   - In Android Studio: Logcat → Filter by "SupabaseAuthRepository"
   - Look for detailed error messages

2. **Verify Supabase**:

   - Dashboard → Project Settings → API
   - Confirm URL matches: `https://itwqcyumcrqqqetoqgai.supabase.co`
   - Confirm anon key is correct

3. **Clean & Rebuild**:

   ```bash
   ./gradlew clean
   ./gradlew build
   ```

4. **Check Internet**:
   - Ensure device/emulator has internet
   - Supabase requires active connection

---

## ✨ Success Indicators

After setup, you should see:

1. ✅ **Registration works** without email confirmation
2. ✅ **User appears** in Authentication → Users
3. ✅ **Profile created** in Table Editor → profiles
4. ✅ **Can login** with registered credentials
5. ✅ **No database errors** in app
6. ✅ **All features accessible**

---

## 📊 Database Schema Overview

```
auth.users (managed by Supabase)
    ↓
profiles (your table)
    - id → references auth.users
    - email, full_name, avatar_url
    - places_visited, reviews_count

community_posts
    - id, user_id → references auth.users
    - place_name, image_url, caption
    - likes_count, comments_count

post_likes
    - post_id → references community_posts
    - user_id → references auth.users
    - UNIQUE(post_id, user_id)

post_comments
    - post_id → references community_posts
    - user_id → references auth.users
    - comment, rating (0-5 stars)

Storage:
    - avatars/ (user profile pictures)
    - community-posts/images/ (post images)
```

---

## 🎉 You're Done!

Your QuietSpace app should now work perfectly with Supabase! 🚀

**Test credentials suggestion:**

- Email: `test@quietspace.com`
- Password: `Test123456`
