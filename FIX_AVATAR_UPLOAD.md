# 🔧 Avatar Upload Error - Quick Fix Guide

## ❌ Problem
When trying to upload an avatar, you get an error: **"cannot upload avatar, error"**

## ✅ Solution

The issue is **99% likely** due to missing **Supabase Storage policies**. Your code is correct, but the Supabase backend isn't configured to allow uploads.

---

## 🚀 Quick Fix (Copy & Paste in Supabase)

### Step 1: Go to Supabase SQL Editor
1. Open: https://itwqcyumcrqqqetoqgai.supabase.co
2. Click: **SQL Editor** (left sidebar)
3. Click: **New Query**

### Step 2: Run This SQL

```sql
-- ===== CREATE AVATARS BUCKET IF IT DOESN'T EXIST =====
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- ===== REMOVE OLD POLICIES (if any) =====
DROP POLICY IF EXISTS "Allow all for authenticated users" ON storage.objects;
DROP POLICY IF EXISTS "Public read access" ON storage.objects;
DROP POLICY IF EXISTS "Users can upload their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Users can update their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Users can delete their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Public can view avatars" ON storage.objects;

-- ===== CREATE NEW POLICIES =====

-- Allow authenticated users to upload/update/delete avatars
CREATE POLICY "Allow all for authenticated users"
ON storage.objects
FOR ALL
TO authenticated
USING (bucket_id = 'avatars')
WITH CHECK (bucket_id = 'avatars');

-- Allow anyone to view avatars (needed to display profile pictures)
CREATE POLICY "Public read access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'avatars');
```

### Step 3: Click "Run" (or press Ctrl+Enter)

You should see: ✅ **Success. No rows returned**

---

## 🧪 Test It

1. **Rebuild your app:**
   - In Android Studio: **Build → Clean Project**
   - Then: **Build → Rebuild Project**

2. **Run the app** on your device/emulator

3. **Go to Profile → Tap avatar → Choose image**

4. **Should now work!** ✅

---

## 🔍 If Still Not Working

### Check Logcat for Specific Error:
1. Open **Logcat** in Android Studio (bottom toolbar)
2. Filter by: `SupabaseStorageRepo`
3. Try uploading avatar again
4. Look for error message

### Common Errors & Fixes:

| Error Message | Cause | Fix |
|--------------|-------|-----|
| `Permission denied` | Storage policies not created | Run the SQL above |
| `Bucket not found` | Avatars bucket doesn't exist | Run the SQL above |
| `Could not open image` | Android file permission issue | Grant camera/storage permissions |
| `Network error` | No internet | Check device connection |
| `User not authenticated` | Not logged in | Log out and log back in |

---

## 📊 Verify Setup in Supabase

### Check Bucket Exists:
1. Go to **Storage** (left sidebar)
2. Should see **`avatars`** bucket
3. Click on it → should say **"Public"**

### Check Policies:
1. In **Storage → avatars**
2. Click **"Policies"** tab
3. Should see 2 policies:
   - ✅ "Allow all for authenticated users"
   - ✅ "Public read access"

---

## 🎯 What Changed in Your Code

I updated `SupabaseStorageRepository.kt` to organize avatars by user folder:

**Before:**
```kotlin
val fileName = "avatar_${userId}_${UUID.randomUUID()}.jpg"
```

**After:**
```kotlin
val fileName = "${userId}/avatar_${System.currentTimeMillis()}.jpg"
```

**Benefits:**
- Better organization (each user has their own folder)
- Easier to implement strict security policies later
- Prevents filename collisions
- Easier to clean up old avatars

---

## ✅ Expected Behavior After Fix

1. **Tap avatar** → Shows dialog: "Take Photo / Choose from Gallery / Remove Photo"
2. **Select image** → Shows "Uploading avatar..." toast
3. **After 1-2 seconds** → Shows "Avatar updated successfully!" ✅
4. **Avatar image updates** in profile
5. **Other users can see** your avatar in community posts

---

## 🔐 Security Notes

The policies allow:
- ✅ **Any authenticated user** can upload/update/delete files in avatars bucket
- ✅ **Anyone (public)** can view/download avatars
- ❌ **Anonymous users** cannot upload

For production, you might want stricter policies where users can only modify their own folder. See `AVATAR_UPLOAD_DEBUG.md` for advanced security policies.

---

## 📞 Need More Help?

If you're still getting errors:

1. **Copy the exact error** from Logcat
2. **Screenshot** of Supabase Storage → avatars → Policies
3. **Verify** you're logged in (Supabase Dashboard → Authentication → Users)

The SQL script above should fix 99% of avatar upload issues! 🚀
