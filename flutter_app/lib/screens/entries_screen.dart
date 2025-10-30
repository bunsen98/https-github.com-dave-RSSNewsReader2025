import 'package:flutter/material.dart';
import 'package:rss_reader_flutter/models/entry.dart';
import 'package:rss_reader_flutter/repositories/entry_repository.dart';
import 'package:rss_reader_flutter/screens/entry_detail_screen.dart';

class EntriesScreen extends StatefulWidget {
  const EntriesScreen({super.key});

  @override
  State<EntriesScreen> createState() => _EntriesScreenState();
}

class _EntriesScreenState extends State<EntriesScreen> {
  final EntryRepository _entryRepository = EntryRepository();
  List<Entry> _entries = [];
  bool _isLoading = true;
  bool _showUnreadOnly = false;

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
      final entries = _showUnreadOnly
          ? await _entryRepository.getUnreadEntries()
          : await _entryRepository.getAllEntries();
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

  void _toggleFilter() {
    setState(() {
      _showUnreadOnly = !_showUnreadOnly;
    });
    _loadEntries();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('All Entries'),
        actions: [
          IconButton(
            icon: Icon(_showUnreadOnly ? Icons.filter_alt : Icons.filter_alt_outlined),
            onPressed: _toggleFilter,
            tooltip: _showUnreadOnly ? 'Show all' : 'Show unread only',
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadEntries,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _entries.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.article, size: 64, color: Colors.grey),
                      const SizedBox(height: 16),
                      Text(
                        _showUnreadOnly ? 'No unread entries' : 'No entries yet',
                        style: const TextStyle(fontSize: 18, color: Colors.grey),
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
