import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:rss_reader_flutter/models/entry.dart';
import 'package:rss_reader_flutter/repositories/entry_repository.dart';
import 'package:rss_reader_flutter/services/tts_service.dart';

class EntryDetailScreen extends StatefulWidget {
  final Entry entry;

  const EntryDetailScreen({super.key, required this.entry});

  @override
  State<EntryDetailScreen> createState() => _EntryDetailScreenState();
}

class _EntryDetailScreenState extends State<EntryDetailScreen> {
  final EntryRepository _entryRepository = EntryRepository();
  final TtsService _ttsService = TtsService();
  late Entry _entry;
  bool _isPlaying = false;

  @override
  void initState() {
    super.initState();
    _entry = widget.entry;
    _markAsRead();
    _ttsService.setCompletionHandler(_onTtsComplete);
  }

  @override
  void dispose() {
    _ttsService.stop();
    super.dispose();
  }

  Future<void> _markAsRead() async {
    if (!_entry.isRead) {
      await _entryRepository.markAsRead(_entry.id);
      setState(() {
        _entry = _entry.copyWith(isRead: true);
      });
    }
  }

  Future<void> _toggleFavorite() async {
    try {
      await _entryRepository.toggleFavorite(_entry.id, !_entry.isFavorite);
      setState(() {
        _entry = _entry.copyWith(isFavorite: !_entry.isFavorite);
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(_entry.isFavorite ? 'Added to favorites' : 'Removed from favorites'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    }
  }

  Future<void> _openInBrowser() async {
    final url = Uri.parse(_entry.url);
    if (await canLaunchUrl(url)) {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    }
  }

  Future<void> _toggleTts() async {
    if (_isPlaying) {
      await _ttsService.stop();
      setState(() {
        _isPlaying = false;
      });
    } else {
      await _ttsService.speak(_entry.content);
      setState(() {
        _isPlaying = true;
      });
    }
  }

  void _onTtsComplete() {
    if (mounted) {
      setState(() {
        _isPlaying = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Entry'),
        actions: [
          IconButton(
            icon: Icon(
              _entry.isFavorite ? Icons.favorite : Icons.favorite_border,
              color: _entry.isFavorite ? Colors.red : null,
            ),
            onPressed: _toggleFavorite,
          ),
          IconButton(
            icon: const Icon(Icons.open_in_browser),
            onPressed: _openInBrowser,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              _entry.title,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 8),
            Text(
              _formatDate(_entry.publishedAt),
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.grey,
                  ),
            ),
            const SizedBox(height: 24),
            Text(
              _entry.content,
              style: Theme.of(context).textTheme.bodyLarge,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _toggleTts,
        child: Icon(_isPlaying ? Icons.stop : Icons.play_arrow),
      ),
    );
  }

  String _formatDate(DateTime date) {
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')} '
        '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }
}
