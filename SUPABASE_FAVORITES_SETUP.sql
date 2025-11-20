-- Create a table for user favorites
create table public.user_favorites (
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

-- Set up Row Level Security (RLS)
alter table public.user_favorites enable row level security;

-- Policy: Users can view their own favorites
create policy "Users can view their own favorites" on public.user_favorites
  for select using (auth.uid() = user_id);

-- Policy: Users can insert their own favorites
create policy "Users can insert their own favorites" on public.user_favorites
  for insert with check (auth.uid() = user_id);

-- Policy: Users can delete their own favorites
create policy "Users can delete their own favorites" on public.user_favorites
  for delete using (auth.uid() = user_id);
