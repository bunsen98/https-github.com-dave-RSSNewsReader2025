import 'package:rss_reader_flutter/models/feed.dart';
import 'package:rss_reader_flutter/models/entry.dart';
import 'package:rss_reader_flutter/repositories/feed_repository.dart';
import 'package:rss_reader_flutter/repositories/entry_repository.dart';
import 'package:rss_reader_flutter/services/rss_parser.dart';
import 'package:uuid/uuid.dart';

class FeedService {
  final FeedRepository _feedRepository = FeedRepository();
  final EntryRepository _entryRepository = EntryRepository();
  final RssParser _rssParser = RssParser();
  final _uuid = const Uuid();

  Future<Feed> addFeed(String url) async {
    final existing = await _feedRepository.getFeedByUrl(url);
    if (existing != null) {
      throw Exception('Feed already exists');
    }

    final parsedFeed = await _rssParser.fetchAndParse(url);

    final feed = Feed(
      id: _uuid.v4(),
      title: parsedFeed.title,
      url: parsedFeed.url,
      siteUrl: parsedFeed.siteUrl,
      format: parsedFeed.format,
      unreadCount: parsedFeed.entries.length,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );

    final insertedFeed = await _feedRepository.insertFeed(feed);

    for (final parsedEntry in parsedFeed.entries) {
      final entry = Entry(
        id: _uuid.v4(),
        feedId: insertedFeed.id,
        title: parsedEntry.title,
        url: parsedEntry.url,
        content: parsedEntry.content,
        publishedAt: parsedEntry.publishedAt,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );
      await _entryRepository.insertEntry(entry);
    }

    return insertedFeed;
  }

  Future<void> updateFeed(String feedId) async {
    final feed = await _feedRepository.getFeedById(feedId);
    if (feed == null) {
      throw Exception('Feed not found');
    }

    final parsedFeed = await _rssParser.fetchAndParse(feed.url);

    final existingEntries = await _entryRepository.getEntriesByFeedId(feedId);
    final existingUrls = existingEntries.map((e) => e.url).toSet();

    int newEntriesCount = 0;
    for (final parsedEntry in parsedFeed.entries) {
      if (!existingUrls.contains(parsedEntry.url)) {
        final entry = Entry(
          id: _uuid.v4(),
          feedId: feedId,
          title: parsedEntry.title,
          url: parsedEntry.url,
          content: parsedEntry.content,
          publishedAt: parsedEntry.publishedAt,
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );
        await _entryRepository.insertEntry(entry);
        newEntriesCount++;
      }
    }

    final updatedFeed = feed.copyWith(
      title: parsedFeed.title,
      siteUrl: parsedFeed.siteUrl,
      unreadCount: feed.unreadCount + newEntriesCount,
      updatedAt: DateTime.now(),
    );

    await _feedRepository.updateFeed(updatedFeed);
  }

  Future<void> updateAllFeeds() async {
    final feeds = await _feedRepository.getAllFeeds();
    for (final feed in feeds) {
      try {
        await updateFeed(feed.id);
      } catch (e) {
        print('Error updating feed ${feed.title}: $e');
      }
    }
  }

  Future<void> deleteFeed(String feedId) async {
    await _entryRepository.deleteEntriesByFeedId(feedId);
    await _feedRepository.deleteFeed(feedId);
  }
}
