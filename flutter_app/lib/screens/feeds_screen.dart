import 'package:flutter/material.dart';
import 'package:rss_reader_flutter/models/feed.dart';
import 'package:rss_reader_flutter/repositories/feed_repository.dart';
import 'package:rss_reader_flutter/services/feed_service.dart';
import 'package:rss_reader_flutter/screens/feed_entries_screen.dart';
import 'package:rss_reader_flutter/widgets/add_feed_dialog.dart';

class FeedsScreen extends StatefulWidget {
  const FeedsScreen({super.key});

  @override
  State<FeedsScreen> createState() => _FeedsScreenState();
}

class _FeedsScreenState extends State<FeedsScreen> {
  final FeedRepository _feedRepository = FeedRepository();
  final FeedService _feedService = FeedService();
  List<Feed> _feeds = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadFeeds();
  }

  Future<void> _loadFeeds() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final feeds = await _feedRepository.getAllFeeds();
      setState(() {
        _feeds = feeds;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error loading feeds: $e')),
        );
      }
    }
  }

  Future<void> _updateAllFeeds() async {
    try {
      await _feedService.updateAllFeeds();
      await _loadFeeds();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Feeds updated successfully')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error updating feeds: $e')),
        );
      }
    }
  }

  Future<void> _deleteFeed(Feed feed) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Feed'),
        content: Text('Are you sure you want to delete "${feed.title}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        await _feedService.deleteFeed(feed.id);
        await _loadFeeds();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Feed deleted')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error deleting feed: $e')),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Feeds'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _updateAllFeeds,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _feeds.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.rss_feed, size: 64, color: Colors.grey),
                      const SizedBox(height: 16),
                      const Text(
                        'No feeds yet',
                        style: TextStyle(fontSize: 18, color: Colors.grey),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Add a feed to get started',
                        style: TextStyle(color: Colors.grey),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  itemCount: _feeds.length,
                  itemBuilder: (context, index) {
                    final feed = _feeds[index];
                    return ListTile(
                      leading: CircleAvatar(
                        backgroundColor: Theme.of(context).primaryColor,
                        child: Text(
                          feed.title.isNotEmpty ? feed.title[0].toUpperCase() : '?',
                          style: const TextStyle(color: Colors.white),
                        ),
                      ),
                      title: Text(feed.title),
                      subtitle: Text('${feed.unreadCount} unread'),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () => _deleteFeed(feed),
                      ),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => FeedEntriesScreen(feed: feed),
                          ),
                        );
                      },
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          final result = await showDialog<bool>(
            context: context,
            builder: (context) => const AddFeedDialog(),
          );
          if (result == true) {
            await _loadFeeds();
          }
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
