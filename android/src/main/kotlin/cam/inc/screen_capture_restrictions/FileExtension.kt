package cam.inc.screen_capture_restrictions

import android.webkit.MimeTypeMap
import java.io.File

// MimeTypeを取得する
// Get MimeType
fun File.getMimeType(): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(this.path)
    if (extension == null) return null
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

// 最終更新日時のファイルを取得する
// Get the file with the latest update date
fun File.getLatestModified(path: String): File? {
    File(path).let { file ->
        if (!file.isDirectory) return null
        val files = file.listFiles { file -> file.isFile } ?: return null
        return files.maxByOrNull { it.lastModified() }
    }
}