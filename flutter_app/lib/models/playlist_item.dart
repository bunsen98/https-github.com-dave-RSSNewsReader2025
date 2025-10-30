class PlaylistItem {
  final String id;
  final String feedId;
  final String entryId;
  final int position;
  final DateTime createdAt;

  PlaylistItem({
    required this.id,
    required this.feedId,
    required this.entryId,
    required this.position,
    required this.createdAt,
  });

  factory PlaylistItem.fromJson(Map<String, dynamic> json) {
    return PlaylistItem(
      id: json['id'] as String,
      feedId: json['feed_id'] as String,
      entryId: json['entry_id'] as String,
      position: json['position'] as int,
      createdAt: DateTime.parse(json['created_at'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'feed_id': feedId,
      'entry_id': entryId,
      'position': position,
      'created_at': createdAt.toIso8601String(),
    };
  }
}
