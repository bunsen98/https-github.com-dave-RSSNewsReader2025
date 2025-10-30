import 'package:rss_reader_flutter/config/supabase_config.dart';
import 'package:rss_reader_flutter/models/history.dart';

class HistoryRepository {
  final _client = SupabaseConfig.client;

  Future<History?> getHistoryByEntryId(String entryId) async {
    final response = await _client
        .from('history')
        .select()
        .eq('entry_id', entryId)
        .maybeSingle();

    if (response == null) return null;
    return History.fromJson(response);
  }

  Future<List<History>> getAllHistory() async {
    final response = await _client
        .from('history')
        .select()
        .order('read_at', ascending: false);

    return (response as List).map((json) => History.fromJson(json)).toList();
  }

  Future<History> insertOrUpdateHistory(String entryId, int position) async {
    final existing = await getHistoryByEntryId(entryId);

    if (existing != null) {
      final response = await _client
          .from('history')
          .update({
            'last_read_position': position,
            'read_at': DateTime.now().toIso8601String(),
          })
          .eq('entry_id', entryId)
          .select()
          .single();

      return History.fromJson(response);
    } else {
      final response = await _client
          .from('history')
          .insert({
            'entry_id': entryId,
            'last_read_position': position,
            'read_at': DateTime.now().toIso8601String(),
          })
          .select()
          .single();

      return History.fromJson(response);
    }
  }

  Future<void> deleteHistory(String entryId) async {
    await _client.from('history').delete().eq('entry_id', entryId);
  }

  Future<void> clearAllHistory() async {
    await _client.from('history').delete().neq('id', '');
  }
}
