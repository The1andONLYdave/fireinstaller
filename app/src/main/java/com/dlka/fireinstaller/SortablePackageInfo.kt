package com.dlka.fireinstaller

import android.content.pm.ApplicationInfo
import android.view.View
import android.widget.CheckBox

/**
 * Data container for holding all the relevant information on a package.
 *
 * @author patrick
 */
internal class SortablePackageInfo : Comparable<SortablePackageInfo>, View.OnClickListener {

    var packageName: String? = null
    var displayName: String? = null
    var selected: Boolean = false
    var version: String? = null
    var appInfo: ApplicationInfo? = null
    var sourceDir: String? = null

    override fun compareTo(another: SortablePackageInfo): Int {
        return displayName!!.toLowerCase().compareTo(another.displayName!!.toLowerCase())
    }

    override fun onClick(v: View) {
        selected = (v as CheckBox).isChecked
    }

}
