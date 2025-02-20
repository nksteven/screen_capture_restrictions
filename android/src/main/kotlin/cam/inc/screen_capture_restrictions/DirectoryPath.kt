package cam.inc.screen_capture_restrictions

import android.os.Environment
import java.io.File

enum class DirectoryPath(val path: String) {
    dcim(Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Screenshots" + File.separator),
    pictures(Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator),
    movies(Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_MOVIES + File.separator),
    dcimSumsung(Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Screen recordings" + File.separator);

    companion object {
        fun getPathList(): List<String> = values().map { it.path }
        fun getFileList(): List<File> = values().map { File(it.path) }
    }
}