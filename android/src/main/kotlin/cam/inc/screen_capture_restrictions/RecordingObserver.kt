package cam.inc.screen_capture_restrictions

import android.os.FileObserver
import java.io.File
import java.util.Timer
import java.util.TimerTask

// ScreenRecordの監視
// Monitoring ScreenRecord
class RecordingObserver: FileObserver {
    constructor(
        latestFile: File,
        isRecordingCallBack: (Boolean) -> Unit,
    ) : super(latestFile.getPath()) {
        this.latestFile = latestFile
        this.isRecordingCallBack = isRecordingCallBack
    }

    private val latestFile: File
    private val isRecordingCallBack: (Boolean) -> Unit

    private var tmpFileLength: Long = 0
    private var captureTimer: Timer? = Timer()

    override fun onEvent(event: Int, path: String?) {
        val latestFileLength: Long = latestFile.length()
        if (latestFileLength > tmpFileLength) {
            captureTimer?.also {
                try {
                    it.cancel()
                    captureTimer = null
                } catch (ignored: Exception) {
                }
            }
            isRecordingCallBack(event == FileObserver.MODIFY)
            tmpFileLength = latestFile.length()
        }
        captureTimer?.also{} ?: run {
            captureTimer = Timer()
            captureTimer?.schedule(object : TimerTask() {
                override fun run() {
                    isRecordingCallBack(latestFileLength != tmpFileLength)
                }
            }, 1500)
        }
    }
}