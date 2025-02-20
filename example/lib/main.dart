import 'package:flutter/material.dart';
import 'package:screen_capture_restrictions/screen_capture_restrictions.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: MainScreen(),
    );
  }
}

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter Screen Capture Restrictions Sample.'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            InkWell(
              child: const Text('Secure Sample'),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const SecureScreen(),
                  ),
                );
              },
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Observe Sample'),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ObserveSample(),
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}

class SecureScreen extends StatelessWidget {
  const SecureScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Secure Sample'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            InkWell(
              child: const Text('Enable Secure'),
              onTap: () async {
                await ScreenCaptureRestrictions.enableSecure();
              },
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Disable Secure'),
              onTap: () async {
                await ScreenCaptureRestrictions.disableSecure();
              },
            ),
          ],
        ),
      ),
    );
  }
}

class ObserveSample extends StatelessWidget {
  ObserveSample({super.key});

  final screenCaptureEvent = ScreenCaptureRestrictions();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Observe Sample'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            InkWell(
              child: const Text('Read Storage Permission'),
              onTap: () async =>
                  await screenCaptureEvent.readStoragePermission(),
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Manage Storage Permission'),
              onTap: () async =>
                  await screenCaptureEvent.manageStoragePermission(),
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Enable Observe'),
              onTap: () {
                screenCaptureEvent.addScreenShotListener(
                  (path) => print('didScreenShot: $path'),
                );
                screenCaptureEvent.addScreenRecordListener(
                  (isRecord) => print('didScreenRecord: $isRecord'),
                );
                screenCaptureEvent.observe();
              },
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Dispose Observe'),
              onTap: () => screenCaptureEvent.dispose(),
            ),
            const SizedBox(height: 50),
            InkWell(
              child: const Text('Is Recording'),
              onTap: () async {
                await screenCaptureEvent.isRecording();
              },
            ),
          ],
        ),
      ),
    );
  }
}
