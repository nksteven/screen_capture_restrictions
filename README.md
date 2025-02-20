# screen_capture_restrictions

A flutter plugins to restrictions to screen capture and recording on mobile platforms.

## Installation

Add `screen_capture_restrictions` as a [dependency in your pubspec.yaml file](https://flutter.dev/using-packages/).

```yaml
dependencies:
  screen_capture_restrictions: ^1.1.0
```

## Usage
### restrict screen capture and recording

If you want to restrict screen capture and recording, run enableSecure.
To remove the restriction, execute disableSecure.

While screen capture and recording limits are in effect, the generated images and videos will be blacked out.

```dart
// enable screen capture and recording restrictions
await ScreenCaptureRestrictions.enableSecure();

// disable restrictions
await ScreenCaptureRestrictions.disableSecure();
```

### callback when screen capture and recording

Receive callbacks during screen capture and recording.

```dart
// create instance
final screenCaptureRestriction = ScreenCaptureRestrictions()

// callback for screen capture detection.
screenCaptureRestriction.addScreenShotListener((imgPath) {
    print('screenShot: $imgPath');
});

// callback for screen recording detection.
screenCaptureRestriction.addScreenRecordListener((isRecording) {
    print('screenRecord: $isRecording');
});

// start monitoring
screenCaptureRestriction.observe();
```

To remove monitoring, execute dispose.

```dart
screenCaptureRestriction.dispose();
```

If you want to get the current screen recording status, execute isRecording.

```dart
final isRecording = await screenCaptureRestriction.isRecording();
print('isRecording: $isRecording');
```

If you want it to work on android, please request permissions before starting observe.
Depending on the android SDK version, request the appropriate permissions in AndroidManifest.xml.

```dart
// for sdk 30 or later
manageExternalStoragePermission();

// for sdk 29 or earlier
readExternalStoragePermission();
```

**⚠️caution⚠️** <br>
The `android.permission.MANAGE_EXTERNAL_STORAGE` permission is required to receive screen capture and recording callbacks on Android 11 and later devices.
In order to release apps using this permission to the store, they must meet the policies set by Google.
https://developer.android.com/training/data-storage/manage-all-files
Also, under the current specifications, there is a delay of several seconds until the screen capture and recording for Android 11~13 and the screen recording callback for Android 14 are processed when they are received.

## issues
- [ ] Eliminate callback delay for screen capture and recording on Android 11-13 and screen recording on Android 14.
- [ ] Support for Android 15 screen recording detection.