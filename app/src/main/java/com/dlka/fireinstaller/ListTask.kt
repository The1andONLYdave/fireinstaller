package com.dlka.fireinstaller

import android.app.ListActivity
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.AsyncTask
import android.util.Log

import java.util.ArrayList
import java.util.Arrays

/**
 * Query the packagemanager for a list of all installed apps that are not system
 * apps. Populate a listview with the result.
 *
 * @author patrick
 */
internal class ListTask
/**
 * New task
 *
 * @param listActivity the activity to report back to
 * @param layout       layout id to pass to the AppAdaptier
 */
(private val listActivity: ListActivity, private val layout: Int) : AsyncTask<Any, Any, ArrayList<SortablePackageInfo>?>() {

    override fun doInBackground(vararg params: Any): ArrayList<SortablePackageInfo>? {
        val ret = ArrayList<SortablePackageInfo>()
        val pm = listActivity.packageManager
        val list = pm.getInstalledPackages(0)
        val spitmp = arrayOfNulls<SortablePackageInfo>(list.size) //emptyArray<SortablePackageInfo>() //arrayOfNulls<SortablePackageInfo>(list.size)
        val it = list.iterator()
        //	AnnotationsSource aSource = new AnnotationsSource(listActivity);
        //	aSource.open();
        var idx = 0
        while (it.hasNext()) {
            val info = it.next()
            try {
                val ai = pm.getApplicationInfo(info.packageName, 0)
                spitmp[idx] = SortablePackageInfo()
                spitmp[idx]?.packageName = info.packageName
                spitmp[idx]?.sourceDir = ai.sourceDir
                spitmp[idx]?.displayName = pm
                        .getApplicationLabel(info.applicationInfo).toString()
                spitmp[idx]?.appInfo = ai
                //TODO needing this? //spitmp[idx].uid = info.applicationInfo.uid;
                idx++

            } catch (exp: NameNotFoundException) {
            }

        }
        // Reminder: the copying is necessary because we are filtering away the
        // system apps.
        val spi = arrayOfNulls<SortablePackageInfo>(idx)// emptyArray<SortablePackageInfo>() //arrayOfNulls<SortablePackageInfo>(idx)
        System.arraycopy(spitmp, 0, spi, 0, idx)
        Arrays.sort(spi)
        for (i in spi.indices) {
            spi[i]?.selected = false//unselect all apps on createView onStart
            if(spi[i] != null ) {
                ret.add(spi[i]!!)
            }
        }
        return ret
    }

    override fun onPreExecute() {
        listActivity.setProgressBarIndeterminate(true)
        listActivity.setProgressBarVisibility(true)
    }

    override fun onPostExecute(result: ArrayList<SortablePackageInfo>?) {
        super.onPostExecute(result)
        listActivity.listAdapter = AppAdapter(listActivity, layout, result,
                layout)
        listActivity.setProgressBarIndeterminate(false)
        listActivity.setProgressBarVisibility(false)
    }

}
