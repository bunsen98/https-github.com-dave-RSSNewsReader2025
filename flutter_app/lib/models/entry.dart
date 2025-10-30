class Entry {
  final String id;
  final String feedId;
  final String title;
  final String url;
  final String content;
  final DateTime publishedAt;
  final bool isRead;
  final bool isFavorite;
  final String point;
  final DateTime createdAt;
  final DateTime updatedAt;

  Entry({
    required this.id,
    required this.feedId,
    required this.title,
    required this.url,
    this.content = '',
    required this.publishedAt,
    this.isRead = false,
    this.isFavorite = false,
    this.point = '',
    required this.createdAt,
    required this.updatedAt,
  });

  factory Entry.fromJson(Map<String, dynamic> json) {
    return Entry(
      id: json['id'] as String,
      feedId: json['feed_id'] as String,
      title: json['title'] as String? ?? '',
      url: json['url'] as String,
      content: json['content'] as String? ?? '',
      publishedAt: DateTime.parse(json['published_at'] as String),
      isRead: json['is_read'] as bool? ?? false,
      isFavorite: json['is_favorite'] as bool? ?? false,
      point: json['point'] as String? ?? '',
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'feed_id': feedId,
      'title': title,
      'url': url,
      'content': content,
      'published_at': publishedAt.toIso8601String(),
      'is_read': isRead,
      'is_favorite': isFavorite,
      'point': point,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
    };
  }

  Entry copyWith({
    String? id,
    String? feedId,
    String? title,
    String? url,
    String? content,
    DateTime? publishedAt,
    bool? isRead,
    bool? isFavorite,
    String? point,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Entry(
      id: id ?? this.id,
      feedId: feedId ?? this.feedId,
      title: title ?? this.title,
      url: url ?? this.url,
      content: content ?? this.content,
      publishedAt: publishedAt ?? this.publishedAt,
      isRead: isRead ?? this.isRead,
      isFavorite: isFavorite ?? this.isFavorite,
      point: point ?? this.point,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
