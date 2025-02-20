package cam.inc.screen_capture_restrictions

import android.os.Build
import android.os.FileObserver
import java.io.File

// SDK 29 以上の場合
// If SDK is 29 or higher
class FileGenObserverForSdk29Later: FileObserver {
    constructor(
        dirFileList: List<File>,
        dirPathList: List<String>,
        onImageCreated: (String) -> Unit,
        onVideoCreated: (String) -> Unit,
    ) : super(dirFileList, if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) CREATE else OPEN) {
        this.dirPathList = dirPathList
        this.onImageCreated = onImageCreated
        this.onVideoCreated = onVideoCreated
    }

    private val dirPathList: List<String>
    private val onImageCreated: (String) -> Unit
    private val onVideoCreated: (String) -> Unit

    override fun onEvent(event: Int, path: String?) {
        for (dirPath in dirPathList) {
            val file = File(dirPath + path)
            if (!file.exists()) continue
            val mime = file.getMimeType() ?: return
            when {
                mime.contains("image") -> onImageCreated(file.path)
                mime.contains("video") -> onVideoCreated(file.path)
            }
        }
    }
}

// SDK 28 以下の場合
// If SDK is 28 or lower
class FileGenObserver: FileObserver {
    constructor(
        dirPath: String,
        onImageCreated: (String) -> Unit,
        onVideoCreated: (String) -> Unit,
    ) : super(dirPath, CREATE) {
        this.dirPath = dirPath
        this.onImageCreated = onImageCreated
        this.onVideoCreated = onVideoCreated
    }

    private val dirPath: String
    private val onImageCreated: (String) -> Unit
    private val onVideoCreated: (String) -> Unit

    override fun onEvent(event: Int, path: String?) {
        val file = File(dirPath + path)
        val mime = file.getMimeType() ?: return
        when {
            mime.contains("image") -> onImageCreated(file.path)
            mime.contains("video") -> onVideoCreated(file.path)
         }
    }
}