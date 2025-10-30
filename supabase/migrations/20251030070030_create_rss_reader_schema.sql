/*
  # RSS Reader Database Schema

  1. New Tables
    - `feeds`
      - `id` (uuid, primary key)
      - `title` (text)
      - `url` (text, unique)
      - `site_url` (text)
      - `icon_url` (text)
      - `format` (text) - RSS or ATOM
      - `unread_count` (integer, default 0)
      - `created_at` (timestamptz)
      - `updated_at` (timestamptz)
    
    - `entries`
      - `id` (uuid, primary key)
      - `feed_id` (uuid, foreign key to feeds)
      - `title` (text)
      - `url` (text)
      - `content` (text)
      - `published_at` (timestamptz)
      - `is_read` (boolean, default false)
      - `is_favorite` (boolean, default false)
      - `point` (text)
      - `created_at` (timestamptz)
      - `updated_at` (timestamptz)
    
    - `playlists`
      - `id` (uuid, primary key)
      - `feed_id` (uuid, foreign key to feeds)
      - `entry_id` (uuid, foreign key to entries)
      - `position` (integer)
      - `created_at` (timestamptz)
    
    - `history`
      - `id` (uuid, primary key)
      - `entry_id` (uuid, foreign key to entries)
      - `last_read_position` (integer, default 0)
      - `read_at` (timestamptz)
      - `created_at` (timestamptz)

  2. Security
    - Enable RLS on all tables
    - Add policies for public access (no authentication required for this RSS reader)

  3. Important Notes
    - All timestamps use `timestamptz` for timezone support
    - Foreign keys with cascade delete to maintain referential integrity
    - Indexes on frequently queried columns
*/

CREATE TABLE IF NOT EXISTS feeds (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title text NOT NULL DEFAULT '',
  url text UNIQUE NOT NULL,
  site_url text DEFAULT '',
  icon_url text DEFAULT '',
  format text DEFAULT 'RSS',
  unread_count integer DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS entries (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  feed_id uuid NOT NULL REFERENCES feeds(id) ON DELETE CASCADE,
  title text NOT NULL DEFAULT '',
  url text NOT NULL,
  content text DEFAULT '',
  published_at timestamptz DEFAULT now(),
  is_read boolean DEFAULT false,
  is_favorite boolean DEFAULT false,
  point text DEFAULT '',
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS playlists (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  feed_id uuid NOT NULL REFERENCES feeds(id) ON DELETE CASCADE,
  entry_id uuid NOT NULL REFERENCES entries(id) ON DELETE CASCADE,
  position integer NOT NULL DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  UNIQUE(feed_id, entry_id)
);

CREATE TABLE IF NOT EXISTS history (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  entry_id uuid NOT NULL REFERENCES entries(id) ON DELETE CASCADE,
  last_read_position integer DEFAULT 0,
  read_at timestamptz DEFAULT now(),
  created_at timestamptz DEFAULT now(),
  UNIQUE(entry_id)
);

CREATE INDEX IF NOT EXISTS idx_entries_feed_id ON entries(feed_id);
CREATE INDEX IF NOT EXISTS idx_entries_published_at ON entries(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_entries_is_read ON entries(is_read);
CREATE INDEX IF NOT EXISTS idx_entries_is_favorite ON entries(is_favorite);
CREATE INDEX IF NOT EXISTS idx_playlists_feed_id ON playlists(feed_id);
CREATE INDEX IF NOT EXISTS idx_playlists_position ON playlists(position);
CREATE INDEX IF NOT EXISTS idx_history_entry_id ON history(entry_id);

ALTER TABLE feeds ENABLE ROW LEVEL SECURITY;
ALTER TABLE entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE playlists ENABLE ROW LEVEL SECURITY;
ALTER TABLE history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Feeds are publicly readable"
  ON feeds FOR SELECT
  USING (true);

CREATE POLICY "Feeds are publicly insertable"
  ON feeds FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Feeds are publicly updatable"
  ON feeds FOR UPDATE
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Feeds are publicly deletable"
  ON feeds FOR DELETE
  USING (true);

CREATE POLICY "Entries are publicly readable"
  ON entries FOR SELECT
  USING (true);

CREATE POLICY "Entries are publicly insertable"
  ON entries FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Entries are publicly updatable"
  ON entries FOR UPDATE
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Entries are publicly deletable"
  ON entries FOR DELETE
  USING (true);

CREATE POLICY "Playlists are publicly readable"
  ON playlists FOR SELECT
  USING (true);

CREATE POLICY "Playlists are publicly insertable"
  ON playlists FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Playlists are publicly updatable"
  ON playlists FOR UPDATE
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Playlists are publicly deletable"
  ON playlists FOR DELETE
  USING (true);

CREATE POLICY "History is publicly readable"
  ON history FOR SELECT
  USING (true);

CREATE POLICY "History is publicly insertable"
  ON history FOR INSERT
  WITH CHECK (true);

CREATE POLICY "History is publicly updatable"
  ON history FOR UPDATE
  USING (true)
  WITH CHECK (true);

CREATE POLICY "History is publicly deletable"
  ON history FOR DELETE
  USING (true);
