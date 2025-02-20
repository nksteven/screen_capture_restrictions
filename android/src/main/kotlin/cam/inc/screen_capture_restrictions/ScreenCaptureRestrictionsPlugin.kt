package cam.inc.screen_capture_restrictions

import android.app.Activity
import android.app.Activity.ScreenCaptureCallback
import android.view.WindowManager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.Settings

import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.os.Looper

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.annotation.NonNull

import java.io.File

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** ScreenCaptureRestrictionsPlugin */
class ScreenCaptureRestrictionsPlugin private constructor(
        private var activity: Activity?,
        private var context: Context?,
    ) : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    
    private var generatedFileObserverList: MutableList<FileObserver> = ArrayList()
    private var screenRecordingObserver: HashMap<String, FileObserver> = HashMap()
    private var handler: Handler? = null
    private var isScreenRecording = false
    private var screenCaptureCallback: ScreenCaptureCallback? = null

    constructor() : this(null, null)

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "screen_capture_restrictions")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "requestReadStoragePermission" -> {
                if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
                }
            }

            "requestManageStoragePermission" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    context!!.startActivity(intent)
                }
            }

            "observe" -> {
                handler = Handler(Looper.getMainLooper())

                updateScreenRecordingObserver()

                val dirFileList = DirectoryPath.getFileList()
                val dirPathList = DirectoryPath.getPathList()

                // NOTE: Android15以降の場合、ScreensCaptureCallbackで取得する
                // in the case of Android15 or later, it is obtained with ScreensCaptureCallback

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    screenCaptureCallback = ScreenCaptureCallback {
                        onImageCreated("Android14 and later does not support file path.")
                    }
                    activity?.registerScreenCaptureCallback(
                        context!!.mainExecutor,
                        screenCaptureCallback!!,
                    )

                    // screenCaptureのためにFileObserverを使う
                    // Use FileObserver for screenCapture
                    val screenCaptureObserver = FileGenObserverForSdk29Later(
                        dirFileList,
                        dirPathList,
                        {},
                        ::onVideoCreated,
                    )
                    screenCaptureObserver.startWatching()
                    generatedFileObserverList.add(screenCaptureObserver)
                    return
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val screenCaptureObserver = FileGenObserverForSdk29Later(
                        dirFileList,
                        dirPathList,
                        ::onImageCreated,
                        ::onVideoCreated,
                    )
                    screenCaptureObserver.startWatching()
                    generatedFileObserverList.add(screenCaptureObserver)
                    return
                } else {
                    for (dirPath in dirPathList) {
                        val screenCaptureObserver = FileGenObserver(
                            dirPath,
                            ::onImageCreated,
                            ::onVideoCreated,
                        )
                        screenCaptureObserver.startWatching()
                        generatedFileObserverList.add(screenCaptureObserver)
                    }
                    return
                }
            }

            "dispose" -> {
                if (generatedFileObserverList.isNotEmpty()) {
                    generatedFileObserverList.forEach { it.stopWatching() }
                    generatedFileObserverList.clear()
                }
                if (screenRecordingObserver.isNotEmpty()) {
                    screenRecordingObserver.entries.forEach { it.value.stopWatching() }
                    screenRecordingObserver.clear()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && screenCaptureCallback != null) {
                    activity?.unregisterScreenCaptureCallback(screenCaptureCallback!!)
                }
            }

            "isRecording" -> {
                result.success(isScreenRecording)
            }

            "enableSecure" -> {
                activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                result.success(true)
            }

            "disableSecure" -> {
                activity?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                result.success(true)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    private fun onImageCreated(path: String) {
        handler!!.post { 
            channel.invokeMethod("didScreenShot", path)
        }
    }

    private fun onVideoCreated(path: String) {
        stopScreenRecordingObserver()
        updateIsScreenRecording(true)
        updateScreenRecordingObserver()
    }

    private fun stopScreenRecordingObserver() {
        screenRecordingObserver.entries.forEach { it.value.stopWatching() }
        screenRecordingObserver.clear()
        updateIsScreenRecording(false)
    }

    private fun updateIsScreenRecording(isRecording: Boolean) {
        if (handler == null) throw IllegalStateException("observe method must be called first.")
        if (isScreenRecording != isRecording) {
            handler!!.post {
                isScreenRecording = isRecording
                channel.invokeMethod("didScreenRecord", isRecording)
            }
        }
    }

    private fun updateScreenRecordingObserver() {
        val dirPathList = DirectoryPath.getPathList()
        for (dirPath in dirPathList) {
            val latestFile = File(dirPath).getLatestModified(dirPath) ?: continue
            val mime = latestFile.getMimeType() ?: continue
            !mime.contains("video") ?: continue

            if (!screenRecordingObserver.containsKey(latestFile.getPath())) {
                var recordingObserver = RecordingObserver(latestFile) {
                    updateIsScreenRecording(it)
                }
                recordingObserver.startWatching()
                screenRecordingObserver.put(latestFile.getPath(), recordingObserver)
                return
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(@NonNull activityPluginBinding: ActivityPluginBinding) {
        activity = activityPluginBinding.getActivity()
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(@NonNull activityPluginBinding: ActivityPluginBinding) {
        onAttachedToActivity(activityPluginBinding)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}