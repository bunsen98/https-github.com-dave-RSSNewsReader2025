import 'package:flutter/material.dart';
import 'package:rss_reader_flutter/models/feed.dart';
import 'package:rss_reader_flutter/models/entry.dart';
import 'package:rss_reader_flutter/repositories/entry_repository.dart';
import 'package:rss_reader_flutter/services/feed_service.dart';
import 'package:rss_reader_flutter/screens/entry_detail_screen.dart';

class FeedEntriesScreen extends StatefulWidget {
  final Feed feed;

  const FeedEntriesScreen({super.key, required this.feed});

  @override
  State<FeedEntriesScreen> createState() => _FeedEntriesScreenState();
}

class _FeedEntriesScreenState extends State<FeedEntriesScreen> {
  final EntryRepository _entryRepository = EntryRepository();
  final FeedService _feedService = FeedService();
  List<Entry> _entries = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadEntries();
  }

  Future<void> _loadEntries() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final entries = await _entryRepository.getEntriesByFeedId(widget.feed.id);
      setState(() {
        _entries = entries;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error loading entries: $e')),
        );
      }
    }
  }

  Future<void> _updateFeed() async {
    try {
      await _feedService.updateFeed(widget.feed.id);
      await _loadEntries();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Feed updated successfully')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error updating feed: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.feed.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _updateFeed,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _entries.isEmpty
              ? const Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.article, size: 64, color: Colors.grey),
                      SizedBox(height: 16),
                      Text(
                        'No entries yet',
                        style: TextStyle(fontSize: 18, color: Colors.grey),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  itemCount: _entries.length,
                  itemBuilder: (context, index) {
                    final entry = _entries[index];
                    return ListTile(
                      title: Text(
                        entry.title,
                        style: TextStyle(
                          fontWeight: entry.isRead ? FontWeight.normal : FontWeight.bold,
                        ),
                      ),
                      subtitle: Text(
                        _formatDate(entry.publishedAt),
                        style: const TextStyle(fontSize: 12),
                      ),
                      trailing: entry.isFavorite
                          ? const Icon(Icons.favorite, color: Colors.red)
                          : null,
                      onTap: () async {
                        await Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => EntryDetailScreen(entry: entry),
                          ),
                        );
                        await _loadEntries();
                      },
                    );
                  },
                ),
    );
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inDays > 7) {
      return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
    } else if (difference.inDays > 0) {
      return '${difference.inDays} days ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours} hours ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes} minutes ago';
    } else {
      return 'Just now';
    }
  }
}
