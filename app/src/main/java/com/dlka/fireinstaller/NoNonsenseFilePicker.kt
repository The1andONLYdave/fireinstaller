package com.dlka.fireinstaller

/**
 * Created by dkulsch on 05.10.15.
 */
/*
 * Copyright (c) 2015 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import android.annotation.TargetApi
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView

import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerActivity

import java.util.ArrayList


class NoNonsenseFilePicker : Activity() {
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_nonsense_file_picker)

        val checkAllowCreateDir = findViewById(R.id.checkAllowCreateDir) as CheckBox
        val checkAllowMultiple = findViewById(R.id.checkAllowMultiple) as CheckBox
        val checkLightTheme = findViewById(R.id.checkLightTheme) as CheckBox
        val radioGroup = findViewById(R.id.radioGroup) as RadioGroup
        textView = findViewById(R.id.text) as TextView

        findViewById(R.id.button_sd)
                .setOnClickListener {
                    val i: Intent


                    i = Intent(this@NoNonsenseFilePicker,
                            FilePickerActivity::class.java)
                    i.action = Intent.ACTION_GET_CONTENT

                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                            checkAllowMultiple.isChecked)
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,
                            checkAllowCreateDir.isChecked)

                    // What mode is selected
                    val mode: Int
                    when (radioGroup.checkedRadioButtonId) {
                        R.id.radioDir -> mode = AbstractFilePickerFragment.MODE_DIR
                        R.id.radioFilesAndDirs -> mode = AbstractFilePickerFragment.MODE_FILE_AND_DIR
                        R.id.radioFile -> mode = AbstractFilePickerFragment.MODE_FILE
                        else -> mode = AbstractFilePickerFragment.MODE_FILE
                    }

                    i.putExtra(FilePickerActivity.EXTRA_MODE, mode)


                    startActivityForResult(i, CODE_SD)
                }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.no_nonsense_file_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return id == R.id.action_settings || super.onOptionsItemSelected(item)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent) {
        if ((CODE_SD == requestCode || CODE_DB == requestCode || CODE_FTP == requestCode) && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                            false)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    val clip = data.clipData
                    val sb = StringBuilder()

                    if (clip != null) {
                        for (i in 0 until clip.itemCount) {
                            sb.append(clip.getItemAt(i).uri.toString())
                            sb.append("\n")
                        }
                    }

                    textView!!.text = sb.toString()
                } else {
                    val paths = data.getStringArrayListExtra(
                            FilePickerActivity.EXTRA_PATHS)
                    val sb = StringBuilder()

                    if (paths != null) {
                        for (path in paths) {
                            sb.append(path)
                            sb.append("\n")
                        }
                    }
                    textView!!.text = sb.toString()
                }
            } else {
                textView!!.text = data.data!!.toString()
            }
        }
    }

    companion object {

        private val CODE_SD = 0
        private val CODE_DB = 1
        private val CODE_FTP = 2
    }

}