# Avatar Upload Debugging Guide

## Check These in Order:

### 1. **Verify Storage Bucket Exists**
- Go to Supabase Dashboard → Storage
- Confirm `avatars` bucket exists
- Confirm it's marked as **Public**

### 2. **Check Storage Policies**
Run this in Supabase SQL Editor:

```sql
SELECT * FROM storage.policies 
WHERE bucket_id = 'avatars';
```

Should return at least 2-4 policies for INSERT, SELECT, UPDATE, DELETE.

### 3. **Test Direct Upload (Bypass App)**
In Supabase Dashboard:
- Go to Storage → avatars
- Try manually uploading a test image
- If this fails, policies are the issue

### 4. **Check User Authentication**
Run in SQL Editor:

```sql
SELECT auth.uid(); -- Should return your user ID
```

If returns `NULL`, user is not authenticated properly.

### 5. **View Logcat Error**
In Android Studio:
1. Open Logcat (bottom toolbar)
2. Filter by: `SupabaseStorageRepo`
3. Try uploading avatar
4. Copy the full error message

Common errors:
- `"Bucket not found"` → Bucket doesn't exist
- `"Permission denied"` → Missing storage policies
- `"Could not open image"` → File permission issue (Android)
- `"Network error"` → Check internet connection

### 6. **Test with Simplified Policy**
Temporarily use this permissive policy for testing:

```sql
-- Remove old policies first
DROP POLICY IF EXISTS "Users can upload their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Users can update their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Users can delete their own avatar" ON storage.objects;
DROP POLICY IF EXISTS "Public can view avatars" ON storage.objects;

-- Add simple test policy
CREATE POLICY "Allow all for authenticated users"
ON storage.objects
FOR ALL
TO authenticated
USING (bucket_id = 'avatars')
WITH CHECK (bucket_id = 'avatars');

-- Allow public viewing
CREATE POLICY "Public read access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'avatars');
```

### 7. **Verify File Provider Config**
Check that `file_paths.xml` exists with camera permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="pictures" path="Pictures/" />
    <cache-path name="cache" path="." />
</paths>
```

## Quick Fix SQL (Run in Supabase)

```sql
-- Create avatars bucket if doesn't exist
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- Drop existing policies
DROP POLICY IF EXISTS "Allow all for authenticated users" ON storage.objects;
DROP POLICY IF EXISTS "Public read access" ON storage.objects;

-- Create simple policies
CREATE POLICY "Allow all for authenticated users"
ON storage.objects
FOR ALL
TO authenticated
USING (bucket_id = 'avatars')
WITH CHECK (bucket_id = 'avatars');

CREATE POLICY "Public read access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'avatars');
```

## Still Not Working?

1. Share the **exact error message** from Logcat
2. Verify Supabase URL and anon key are correct in your app
3. Check internet connectivity
4. Try with a different image file
5. Verify you're logged in (check Authentication → Users in Supabase)
