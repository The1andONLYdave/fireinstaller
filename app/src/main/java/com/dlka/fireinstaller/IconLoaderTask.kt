package com.dlka.fireinstaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.widget.ImageView

import java.lang.ref.WeakReference

/**
 * Helper for loading images without blocking the UI thread.
 *
 * @author patrick
 */
internal class IconLoaderTask(private val packageManager: PackageManager, imageView: ImageView) : AsyncTask<ApplicationInfo, Void, Drawable>() {
    private val imageViewReference: WeakReference<ImageView>?

    init {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = WeakReference(imageView)
    }

    // Decode image in background.
    override fun doInBackground(vararg params: ApplicationInfo): Drawable {
        return params[0].loadIcon(packageManager)
    }

    // Once complete, see if ImageView is still around and set bitmap.
    override fun onPostExecute(drawable: Drawable?) {
        if (imageViewReference != null) {
            val imageView = imageViewReference.get()
            if (imageView != null) {

                if (drawable != null) {
                    imageView.setImageDrawable(drawable)
                }
            }
        }
    }
}