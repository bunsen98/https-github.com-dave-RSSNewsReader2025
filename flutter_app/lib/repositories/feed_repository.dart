import 'package:rss_reader_flutter/config/supabase_config.dart';
import 'package:rss_reader_flutter/models/feed.dart';

class FeedRepository {
  final _client = SupabaseConfig.client;

  Future<List<Feed>> getAllFeeds() async {
    final response = await _client
        .from('feeds')
        .select()
        .order('created_at', ascending: true);

    return (response as List).map((json) => Feed.fromJson(json)).toList();
  }

  Future<Feed?> getFeedById(String id) async {
    final response = await _client
        .from('feeds')
        .select()
        .eq('id', id)
        .maybeSingle();

    if (response == null) return null;
    return Feed.fromJson(response);
  }

  Future<Feed?> getFeedByUrl(String url) async {
    final response = await _client
        .from('feeds')
        .select()
        .eq('url', url)
        .maybeSingle();

    if (response == null) return null;
    return Feed.fromJson(response);
  }

  Future<Feed> insertFeed(Feed feed) async {
    final response = await _client
        .from('feeds')
        .insert({
          'title': feed.title,
          'url': feed.url,
          'site_url': feed.siteUrl,
          'icon_url': feed.iconUrl,
          'format': feed.format,
          'unread_count': feed.unreadCount,
        })
        .select()
        .single();

    return Feed.fromJson(response);
  }

  Future<void> updateFeed(Feed feed) async {
    await _client
        .from('feeds')
        .update({
          'title': feed.title,
          'url': feed.url,
          'site_url': feed.siteUrl,
          'icon_url': feed.iconUrl,
          'format': feed.format,
          'unread_count': feed.unreadCount,
          'updated_at': DateTime.now().toIso8601String(),
        })
        .eq('id', feed.id);
  }

  Future<void> updateUnreadCount(String feedId, int count) async {
    await _client
        .from('feeds')
        .update({
          'unread_count': count,
          'updated_at': DateTime.now().toIso8601String(),
        })
        .eq('id', feedId);
  }

  Future<void> deleteFeed(String id) async {
    await _client.from('feeds').delete().eq('id', id);
  }
}
