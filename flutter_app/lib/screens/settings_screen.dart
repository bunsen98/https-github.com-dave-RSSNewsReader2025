import 'package:flutter/material.dart';
import 'package:rss_reader_flutter/services/preferences_service.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final PreferencesService _prefsService = PreferencesService();
  bool _autoUpdate = false;
  int _updateInterval = 1;
  bool _autoTranslate = false;
  String _targetLanguage = 'en-US';
  double _speechRate = 1.0;
  bool _autoPlay = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final autoUpdate = await _prefsService.getAutoUpdate();
      final updateInterval = await _prefsService.getUpdateInterval();
      final autoTranslate = await _prefsService.getAutoTranslate();
      final targetLanguage = await _prefsService.getTargetLanguage();
      final speechRate = await _prefsService.getSpeechRate();
      final autoPlay = await _prefsService.getAutoPlay();

      setState(() {
        _autoUpdate = autoUpdate;
        _updateInterval = updateInterval;
        _autoTranslate = autoTranslate;
        _targetLanguage = targetLanguage;
        _speechRate = speechRate;
        _autoPlay = autoPlay;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Feed Updates',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          SwitchListTile(
            title: const Text('Auto Update Feeds'),
            subtitle: const Text('Automatically update feeds in the background'),
            value: _autoUpdate,
            onChanged: (value) async {
              await _prefsService.setAutoUpdate(value);
              setState(() {
                _autoUpdate = value;
              });
            },
          ),
          ListTile(
            title: const Text('Update Interval'),
            subtitle: Text('$_updateInterval hour(s)'),
            trailing: DropdownButton<int>(
              value: _updateInterval,
              items: [1, 3, 6, 12, 24].map((int value) {
                return DropdownMenuItem<int>(
                  value: value,
                  child: Text('$value hour(s)'),
                );
              }).toList(),
              onChanged: (value) async {
                if (value != null) {
                  await _prefsService.setUpdateInterval(value);
                  setState(() {
                    _updateInterval = value;
                  });
                }
              },
            ),
          ),
          const Divider(),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Text-to-Speech',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          SwitchListTile(
            title: const Text('Auto Play'),
            subtitle: const Text('Automatically play TTS when opening an entry'),
            value: _autoPlay,
            onChanged: (value) async {
              await _prefsService.setAutoPlay(value);
              setState(() {
                _autoPlay = value;
              });
            },
          ),
          ListTile(
            title: const Text('Speech Rate'),
            subtitle: Slider(
              value: _speechRate,
              min: 0.5,
              max: 2.0,
              divisions: 15,
              label: _speechRate.toStringAsFixed(1),
              onChanged: (value) {
                setState(() {
                  _speechRate = value;
                });
              },
              onChangeEnd: (value) async {
                await _prefsService.setSpeechRate(value);
              },
            ),
          ),
          const Divider(),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Translation',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          SwitchListTile(
            title: const Text('Auto Translate'),
            subtitle: const Text('Automatically translate entries'),
            value: _autoTranslate,
            onChanged: (value) async {
              await _prefsService.setAutoTranslate(value);
              setState(() {
                _autoTranslate = value;
              });
            },
          ),
          ListTile(
            title: const Text('Target Language'),
            subtitle: Text(_targetLanguage),
            trailing: DropdownButton<String>(
              value: _targetLanguage,
              items: [
                'en-US',
                'es-ES',
                'fr-FR',
                'de-DE',
                'it-IT',
                'ja-JP',
                'ko-KR',
                'zh-CN',
              ].map((String value) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
              onChanged: (value) async {
                if (value != null) {
                  await _prefsService.setTargetLanguage(value);
                  setState(() {
                    _targetLanguage = value;
                  });
                }
              },
            ),
          ),
          const Divider(),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'About',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          const ListTile(
            title: Text('Version'),
            subtitle: Text('1.0.0'),
          ),
        ],
      ),
    );
  }
}
