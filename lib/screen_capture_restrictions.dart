import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

/// A flutter plugins to restrictions to screen capture and recording on mobile platforms.
class ScreenCaptureRestrictions {
  final List<Function(String path)> _screenShotListener = [];
  final List<Function(bool isRecord)> _screenRecordListener = [];

  /// Initialize MethodChannel
  static const methodChannel = MethodChannel('screen_capture_restrictions');

  ScreenCaptureRestrictions() {
    methodChannel.setMethodCallHandler((call) async {
      switch (call.method) {
        case "didScreenShot":
          for (var callback in _screenShotListener) {
            callback.call(call.arguments);
          }
          break;

        case "didScreenRecord":
          for (var callback in _screenRecordListener) {
            callback.call(call.arguments);
          }
          break;

        default:
          break;
      }
    });
  }

  /// Request permission for　READ_EXTERNAL_STORAGE
  /// Required for Android 10 (SDK29) and below
  Future<void> readStoragePermission() {
    if (Platform.isIOS) return Future.value();
    return methodChannel.invokeMethod('requestReadStoragePermission');
  }

  /// Request permission for　MANAGE_EXTERNAL_STORAGE
  /// Required for Android 11 (SDK30) or higher and 13（SDK33） or lower
  /// Also required if you want to receive screen recording callbacks on Android 14（SDK34）
  /// Please read the note in the readme to use this permission!!
  Future<void> manageStoragePermission() {
    if (Platform.isIOS) return Future.value();
    return methodChannel.invokeMethod('requestManageStoragePermission');
  }

  /// Start observing screen capture and recording
  void observe() {
    methodChannel.invokeMethod("observe");
  }

  /// Finish observing screen capture and recording
  void dispose() {
    methodChannel.invokeMethod("dispose");
    _screenShotListener.clear();
    _screenRecordListener.clear();
  }

  /// Returns whether screen recording is in progress
  Future<bool> isRecording() {
    return methodChannel.invokeMethod("isRecording").then((value) {
      return value ?? false;
    });
  }

  /// Enable screen capture and recording restrictions
  static Future enableSecure() async {
    await methodChannel.invokeMethod('enableSecure');
  }

  /// Disable restrictions
  static Future disableSecure() async {
    await methodChannel.invokeMethod('disableSecure');
  }

  /// Add optional processing to the callback for screen capture detection
  void addScreenShotListener(Function(String filePath) callback) {
    _screenShotListener.add(callback);
  }

  /// Add optional processing to the callback for screen recording detection
  void addScreenRecordListener(Function(bool recorded) callback) {
    _screenRecordListener.add(callback);
  }
}
