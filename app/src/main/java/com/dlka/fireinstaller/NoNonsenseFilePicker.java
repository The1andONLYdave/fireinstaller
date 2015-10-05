package com.dlka.fireinstaller;

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


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;


public class NoNonsenseFilePicker extends Activity {

    private static final int CODE_SD = 0;
    private static final int CODE_DB = 1;
    private static final int CODE_FTP = 2;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_nonsense_file_picker);

        final CheckBox checkAllowCreateDir =
                (CheckBox) findViewById(R.id.checkAllowCreateDir);
        final CheckBox checkAllowMultiple =
                (CheckBox) findViewById(R.id.checkAllowMultiple);
        final CheckBox checkLightTheme =
                (CheckBox) findViewById(R.id.checkLightTheme);
        final RadioGroup radioGroup =
                (RadioGroup) findViewById(R.id.radioGroup);
        textView = (TextView) findViewById(R.id.text);

        findViewById(R.id.button_sd)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Intent i;


                        i = new Intent(NoNonsenseFilePicker.this,
                                FilePickerActivity.class);
                        i.setAction(Intent.ACTION_GET_CONTENT);

                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                                checkAllowMultiple.isChecked());
                        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR,
                                checkAllowCreateDir.isChecked());

                        // What mode is selected
                        final int mode;
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.radioDir:
                                mode = AbstractFilePickerFragment.MODE_DIR;
                                break;
                            case R.id.radioFilesAndDirs:
                                mode =
                                        AbstractFilePickerFragment.MODE_FILE_AND_DIR;
                                break;
                            case R.id.radioFile:
                            default:
                                mode = AbstractFilePickerFragment.MODE_FILE;
                                break;
                        }

                        i.putExtra(FilePickerActivity.EXTRA_MODE, mode);


                        startActivityForResult(i, CODE_SD);
                    }
                });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_nonsense_file_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if ((CODE_SD == requestCode || CODE_DB == requestCode || CODE_FTP == requestCode) &&
                resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                    false)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();
                    StringBuilder sb = new StringBuilder();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            sb.append(clip.getItemAt(i).getUri().toString());
                            sb.append("\n");
                        }
                    }

                    textView.setText(sb.toString());
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra(
                            FilePickerActivity.EXTRA_PATHS);
                    StringBuilder sb = new StringBuilder();

                    if (paths != null) {
                        for (String path : paths) {
                            sb.append(path);
                            sb.append("\n");
                        }
                    }
                    textView.setText(sb.toString());
                }
            } else {
                textView.setText(data.getData().toString());
            }
        }
    }

}