import 'package:rss_reader_flutter/config/supabase_config.dart';
import 'package:rss_reader_flutter/models/entry.dart';

class EntryRepository {
  final _client = SupabaseConfig.client;

  Future<List<Entry>> getAllEntries() async {
    final response = await _client
        .from('entries')
        .select()
        .order('published_at', ascending: false);

    return (response as List).map((json) => Entry.fromJson(json)).toList();
  }

  Future<List<Entry>> getEntriesByFeedId(String feedId) async {
    final response = await _client
        .from('entries')
        .select()
        .eq('feed_id', feedId)
        .order('published_at', ascending: false);

    return (response as List).map((json) => Entry.fromJson(json)).toList();
  }

  Future<List<Entry>> getUnreadEntries() async {
    final response = await _client
        .from('entries')
        .select()
        .eq('is_read', false)
        .order('published_at', ascending: false);

    return (response as List).map((json) => Entry.fromJson(json)).toList();
  }

  Future<List<Entry>> getFavoriteEntries() async {
    final response = await _client
        .from('entries')
        .select()
        .eq('is_favorite', true)
        .order('published_at', ascending: false);

    return (response as List).map((json) => Entry.fromJson(json)).toList();
  }

  Future<Entry?> getEntryById(String id) async {
    final response = await _client
        .from('entries')
        .select()
        .eq('id', id)
        .maybeSingle();

    if (response == null) return null;
    return Entry.fromJson(response);
  }

  Future<Entry?> getEntryByUrl(String url) async {
    final response = await _client
        .from('entries')
        .select()
        .eq('url', url)
        .maybeSingle();

    if (response == null) return null;
    return Entry.fromJson(response);
  }

  Future<Entry> insertEntry(Entry entry) async {
    final response = await _client
        .from('entries')
        .insert({
          'feed_id': entry.feedId,
          'title': entry.title,
          'url': entry.url,
          'content': entry.content,
          'published_at': entry.publishedAt.toIso8601String(),
          'is_read': entry.isRead,
          'is_favorite': entry.isFavorite,
          'point': entry.point,
        })
        .select()
        .single();

    return Entry.fromJson(response);
  }

  Future<void> updateEntry(Entry entry) async {
    await _client
        .from('entries')
        .update({
          'title': entry.title,
          'url': entry.url,
          'content': entry.content,
          'published_at': entry.publishedAt.toIso8601String(),
          'is_read': entry.isRead,
          'is_favorite': entry.isFavorite,
          'point': entry.point,
          'updated_at': DateTime.now().toIso8601String(),
        })
        .eq('id', entry.id);
  }

  Future<void> markAsRead(String entryId) async {
    await _client
        .from('entries')
        .update({
          'is_read': true,
          'updated_at': DateTime.now().toIso8601String(),
        })
        .eq('id', entryId);
  }

  Future<void> toggleFavorite(String entryId, bool isFavorite) async {
    await _client
        .from('entries')
        .update({
          'is_favorite': isFavorite,
          'updated_at': DateTime.now().toIso8601String(),
        })
        .eq('id', entryId);
  }

  Future<void> deleteEntry(String id) async {
    await _client.from('entries').delete().eq('id', id);
  }

  Future<void> deleteEntriesByFeedId(String feedId) async {
    await _client.from('entries').delete().eq('feed_id', feedId);
  }
}
