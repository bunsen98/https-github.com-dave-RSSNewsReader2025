# RSS Reader Flutter

A Flutter RSS reader application with Text-to-Speech functionality, built with Supabase backend.

## Features

- **RSS Feed Management**: Add, update, and delete RSS feeds
- **Entry Reading**: View all entries, filter by unread, and mark favorites
- **Text-to-Speech**: Listen to articles with built-in TTS
- **Cloud Sync**: All data synced with Supabase
- **Customizable Settings**: Auto-update, speech rate, translation preferences

## Original Java Code

All original Android Java code has been preserved in the project root directory.

## Flutter Project Structure

The new Flutter application is located in the `flutter_app` directory:

```
flutter_app/
├── lib/
│   ├── config/          # Supabase configuration
│   ├── models/          # Data models (Feed, Entry, etc.)
│   ├── repositories/    # Database access layer
│   ├── services/        # Business logic (RSS parser, TTS, etc.)
│   ├── screens/         # UI screens
│   ├── widgets/         # Reusable widgets
│   └── main.dart        # App entry point
├── assets/              # Static assets
└── pubspec.yaml         # Dependencies
```

## Setup

1. Install Flutter: https://flutter.dev/docs/get-started/install
2. Install dependencies:
   ```bash
   cd flutter_app
   flutter pub get
   ```
3. Run the app:
   ```bash
   flutter run
   ```

## Database

The app uses Supabase for data persistence with the following tables:
- `feeds`: RSS feed information
- `entries`: Feed entries/articles
- `playlists`: TTS playlists
- `history`: Reading history

## Dependencies

- `supabase_flutter`: Cloud database and sync
- `flutter_tts`: Text-to-speech
- `xml`: RSS/Atom feed parsing
- `http`: Network requests
- `shared_preferences`: Local settings storage
- `url_launcher`: Open links in browser
