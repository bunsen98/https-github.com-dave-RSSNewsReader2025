class Feed {
  final String id;
  final String title;
  final String url;
  final String siteUrl;
  final String iconUrl;
  final String format;
  final int unreadCount;
  final DateTime createdAt;
  final DateTime updatedAt;

  Feed({
    required this.id,
    required this.title,
    required this.url,
    this.siteUrl = '',
    this.iconUrl = '',
    this.format = 'RSS',
    this.unreadCount = 0,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Feed.fromJson(Map<String, dynamic> json) {
    return Feed(
      id: json['id'] as String,
      title: json['title'] as String? ?? '',
      url: json['url'] as String,
      siteUrl: json['site_url'] as String? ?? '',
      iconUrl: json['icon_url'] as String? ?? '',
      format: json['format'] as String? ?? 'RSS',
      unreadCount: json['unread_count'] as int? ?? 0,
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'url': url,
      'site_url': siteUrl,
      'icon_url': iconUrl,
      'format': format,
      'unread_count': unreadCount,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
    };
  }

  Feed copyWith({
    String? id,
    String? title,
    String? url,
    String? siteUrl,
    String? iconUrl,
    String? format,
    int? unreadCount,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Feed(
      id: id ?? this.id,
      title: title ?? this.title,
      url: url ?? this.url,
      siteUrl: siteUrl ?? this.siteUrl,
      iconUrl: iconUrl ?? this.iconUrl,
      format: format ?? this.format,
      unreadCount: unreadCount ?? this.unreadCount,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
