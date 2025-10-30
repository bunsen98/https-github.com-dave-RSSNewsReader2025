import 'package:supabase_flutter/supabase_flutter.dart';

class SupabaseConfig {
  static const String supabaseUrl = String.fromEnvironment(
    'SUPABASE_URL',
    defaultValue: 'https://cbevewdtwggwmpgahrqm.supabase.co',
  );

  static const String supabaseAnonKey = String.fromEnvironment(
    'SUPABASE_ANON_KEY',
    defaultValue: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNiZXZld2R0d2dnd21wZ2FocnFtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4MDY4MzYsImV4cCI6MjA3NzM4MjgzNn0.iAoMe_gw0NTZBdmwhl3CwkbA4Xctnmfp94NC9qsmcG4',
  );

  static Future<void> initialize() async {
    await Supabase.initialize(
      url: supabaseUrl,
      anonKey: supabaseAnonKey,
    );
  }

  static SupabaseClient get client => Supabase.instance.client;
}
