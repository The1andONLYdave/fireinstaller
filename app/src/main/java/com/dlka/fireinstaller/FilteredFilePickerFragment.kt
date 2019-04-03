package com.dlka.fireinstaller

/**
 * Created by dkulsch on 05.10.15.
 */

import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerFragment
import java.io.File

class FilteredFilePickerFragment : FilePickerFragment() {

    /**
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private fun getExtension(file: File): String? {
        val path = file.path
        val i = path.lastIndexOf(".")
        return if (i < 0) {
            null
        } else {
            path.substring(i)
        }
    }

    override fun isItemVisible(file: File): Boolean {
        return if (!isDir(file) && (mode == AbstractFilePickerFragment.MODE_FILE || mode == AbstractFilePickerFragment.MODE_FILE_AND_DIR)) {
            EXTENSION.equals(getExtension(file)!!, ignoreCase = true)
        } else isDir(file)
    }

    companion object {

        // File extension to filter on
        private val EXTENSION = ".apk"
    }
}