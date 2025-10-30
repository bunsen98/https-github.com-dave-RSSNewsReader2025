import 'package:shared_preferences/shared_preferences.dart';

class PreferencesService {
  static const String _keyAutoUpdate = 'auto_update';
  static const String _keyUpdateInterval = 'update_interval_hours';
  static const String _keyAutoTranslate = 'auto_translate';
  static const String _keyTargetLanguage = 'target_language';
  static const String _keySpeechRate = 'speech_rate';
  static const String _keyAutoPlay = 'auto_play';

  Future<bool> getAutoUpdate() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyAutoUpdate) ?? false;
  }

  Future<void> setAutoUpdate(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAutoUpdate, value);
  }

  Future<int> getUpdateInterval() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt(_keyUpdateInterval) ?? 1;
  }

  Future<void> setUpdateInterval(int hours) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyUpdateInterval, hours);
  }

  Future<bool> getAutoTranslate() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyAutoTranslate) ?? false;
  }

  Future<void> setAutoTranslate(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAutoTranslate, value);
  }

  Future<String> getTargetLanguage() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyTargetLanguage) ?? 'en-US';
  }

  Future<void> setTargetLanguage(String language) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyTargetLanguage, language);
  }

  Future<double> getSpeechRate() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getDouble(_keySpeechRate) ?? 1.0;
  }

  Future<void> setSpeechRate(double rate) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble(_keySpeechRate, rate);
  }

  Future<bool> getAutoPlay() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyAutoPlay) ?? false;
  }

  Future<void> setAutoPlay(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAutoPlay, value);
  }
}
