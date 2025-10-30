import 'package:rss_reader_flutter/config/supabase_config.dart';
import 'package:rss_reader_flutter/models/playlist_item.dart';

class PlaylistRepository {
  final _client = SupabaseConfig.client;

  Future<List<PlaylistItem>> getPlaylistByFeedId(String feedId) async {
    final response = await _client
        .from('playlists')
        .select()
        .eq('feed_id', feedId)
        .order('position', ascending: true);

    return (response as List).map((json) => PlaylistItem.fromJson(json)).toList();
  }

  Future<PlaylistItem> addToPlaylist(String feedId, String entryId) async {
    final maxPosition = await _getMaxPosition(feedId);

    final response = await _client
        .from('playlists')
        .insert({
          'feed_id': feedId,
          'entry_id': entryId,
          'position': maxPosition + 1,
        })
        .select()
        .single();

    return PlaylistItem.fromJson(response);
  }

  Future<void> removeFromPlaylist(String entryId) async {
    await _client.from('playlists').delete().eq('entry_id', entryId);
  }

  Future<void> clearPlaylist(String feedId) async {
    await _client.from('playlists').delete().eq('feed_id', feedId);
  }

  Future<void> updatePosition(String id, int position) async {
    await _client
        .from('playlists')
        .update({'position': position})
        .eq('id', id);
  }

  Future<int> _getMaxPosition(String feedId) async {
    final response = await _client
        .from('playlists')
        .select('position')
        .eq('feed_id', feedId)
        .order('position', ascending: false)
        .limit(1)
        .maybeSingle();

    if (response == null) return 0;
    return response['position'] as int;
  }
}
