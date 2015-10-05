package com.dlka.fireinstaller;

/**
 * Created by dkulsch on 05.10.15.
 */

import android.support.annotation.NonNull;
import com.nononsenseapps.filepicker.FilePickerFragment;
import java.io.File;

public class FilteredFilePickerFragment extends FilePickerFragment {

    // File extension to filter on
    private static final String EXTENSION = ".apk";

    /**
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        if (!isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            return EXTENSION.equalsIgnoreCase(getExtension(file));
        }
        return isDir(file);
    }
}