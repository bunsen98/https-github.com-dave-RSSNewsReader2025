import 'package:http/http.dart' as http;
import 'package:xml/xml.dart';
import 'package:html/parser.dart' as html_parser;
import 'package:rss_reader_flutter/models/feed.dart';
import 'package:rss_reader_flutter/models/entry.dart';

class RssParser {
  Future<ParsedFeed> fetchAndParse(String url) async {
    try {
      final response = await http.get(Uri.parse(url));

      if (response.statusCode != 200) {
        throw Exception('Failed to fetch feed: ${response.statusCode}');
      }

      final document = XmlDocument.parse(response.body);

      final rssElement = document.findAllElements('rss').firstOrNull;
      if (rssElement != null) {
        return _parseRss(document, url);
      }

      final feedElement = document.findAllElements('feed').firstOrNull;
      if (feedElement != null) {
        return _parseAtom(document, url);
      }

      throw Exception('Invalid feed format');
    } catch (e) {
      throw Exception('Error parsing feed: $e');
    }
  }

  ParsedFeed _parseRss(XmlDocument document, String feedUrl) {
    final channel = document.findAllElements('channel').first;

    final title = channel.findElements('title').first.innerText;
    final link = channel.findElements('link').firstOrNull?.innerText ?? '';
    final description = channel.findElements('description').firstOrNull?.innerText ?? '';

    final items = document.findAllElements('item').map((item) {
      final itemTitle = item.findElements('title').firstOrNull?.innerText ?? 'Untitled';
      final itemLink = item.findElements('link').firstOrNull?.innerText ?? '';
      final itemDescription = item.findElements('description').firstOrNull?.innerText ?? '';
      final itemContent = item.findElements('content:encoded').firstOrNull?.innerText ?? itemDescription;

      final pubDateStr = item.findElements('pubDate').firstOrNull?.innerText;
      DateTime pubDate = DateTime.now();
      if (pubDateStr != null) {
        try {
          pubDate = DateTime.parse(pubDateStr);
        } catch (e) {
          pubDate = _parseRfc822Date(pubDateStr) ?? DateTime.now();
        }
      }

      return ParsedEntry(
        title: itemTitle,
        url: itemLink,
        content: _stripHtml(itemContent),
        publishedAt: pubDate,
      );
    }).toList();

    return ParsedFeed(
      title: title,
      url: feedUrl,
      siteUrl: link,
      format: 'RSS',
      entries: items,
    );
  }

  ParsedFeed _parseAtom(XmlDocument document, String feedUrl) {
    final feed = document.findAllElements('feed').first;

    final title = feed.findElements('title').first.innerText;
    final linkElement = feed.findAllElements('link').firstWhere(
      (element) => element.getAttribute('rel') == 'alternate' || element.getAttribute('rel') == null,
      orElse: () => feed.findElements('link').first,
    );
    final link = linkElement.getAttribute('href') ?? '';

    final entries = feed.findAllElements('entry').map((entry) {
      final entryTitle = entry.findElements('title').firstOrNull?.innerText ?? 'Untitled';

      final entryLinkElement = entry.findAllElements('link').firstWhere(
        (element) => element.getAttribute('rel') == 'alternate' || element.getAttribute('rel') == null,
        orElse: () => entry.findElements('link').first,
      );
      final entryLink = entryLinkElement.getAttribute('href') ?? '';

      final summary = entry.findElements('summary').firstOrNull?.innerText ?? '';
      final content = entry.findElements('content').firstOrNull?.innerText ?? summary;

      final publishedStr = entry.findElements('published').firstOrNull?.innerText ??
          entry.findElements('updated').firstOrNull?.innerText;
      DateTime published = DateTime.now();
      if (publishedStr != null) {
        try {
          published = DateTime.parse(publishedStr);
        } catch (e) {
          published = DateTime.now();
        }
      }

      return ParsedEntry(
        title: entryTitle,
        url: entryLink,
        content: _stripHtml(content),
        publishedAt: published,
      );
    }).toList();

    return ParsedFeed(
      title: title,
      url: feedUrl,
      siteUrl: link,
      format: 'ATOM',
      entries: entries,
    );
  }

  String _stripHtml(String htmlString) {
    final document = html_parser.parse(htmlString);
    return document.body?.text ?? htmlString;
  }

  DateTime? _parseRfc822Date(String dateString) {
    try {
      final months = {
        'Jan': 1, 'Feb': 2, 'Mar': 3, 'Apr': 4, 'May': 5, 'Jun': 6,
        'Jul': 7, 'Aug': 8, 'Sep': 9, 'Oct': 10, 'Nov': 11, 'Dec': 12,
      };

      final parts = dateString.trim().split(' ');
      if (parts.length < 4) return null;

      final day = int.parse(parts[1]);
      final month = months[parts[2]] ?? 1;
      final year = int.parse(parts[3]);

      final timeParts = parts[4].split(':');
      final hour = int.parse(timeParts[0]);
      final minute = int.parse(timeParts[1]);
      final second = int.parse(timeParts[2]);

      return DateTime(year, month, day, hour, minute, second);
    } catch (e) {
      return null;
    }
  }
}

class ParsedFeed {
  final String title;
  final String url;
  final String siteUrl;
  final String format;
  final List<ParsedEntry> entries;

  ParsedFeed({
    required this.title,
    required this.url,
    required this.siteUrl,
    required this.format,
    required this.entries,
  });
}

class ParsedEntry {
  final String title;
  final String url;
  final String content;
  final DateTime publishedAt;

  ParsedEntry({
    required this.title,
    required this.url,
    required this.content,
    required this.publishedAt,
  });
}
