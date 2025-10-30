class History {
  final String id;
  final String entryId;
  final int lastReadPosition;
  final DateTime readAt;
  final DateTime createdAt;

  History({
    required this.id,
    required this.entryId,
    this.lastReadPosition = 0,
    required this.readAt,
    required this.createdAt,
  });

  factory History.fromJson(Map<String, dynamic> json) {
    return History(
      id: json['id'] as String,
      entryId: json['entry_id'] as String,
      lastReadPosition: json['last_read_position'] as int? ?? 0,
      readAt: DateTime.parse(json['read_at'] as String),
      createdAt: DateTime.parse(json['created_at'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'entry_id': entryId,
      'last_read_position': lastReadPosition,
      'read_at': readAt.toIso8601String(),
      'created_at': createdAt.toIso8601String(),
    };
  }
}
