package com.dlka.fireinstaller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.sufficientlysecure.donations.DonationsFragment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class DebugActivity extends Activity {
    public String fireip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        final Button bf = (Button) findViewById(R.id.button);
        final Button bs = (Button) findViewById(R.id.button2);
        final Button bt = (Button) findViewById(R.id.button3);
        final Button bv = (Button) findViewById(R.id.button4);
        final Button bfifth = (Button) findViewById(R.id.button5);
        final Button bsixth = (Button) findViewById(R.id.button5);

        bs.setEnabled(false);
        bt.setEnabled(false);
        bf.setEnabled(false);

        bf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bs.setEnabled(true);
                bt.setEnabled(true);
                bf.setEnabled(false);
                bsixth.setEnabled(false);
            }
        });
        bs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bs.setEnabled(false);
                bt.setEnabled(false);
                bf.setEnabled(true);
                bsixth.setEnabled(true);
            }
        });
        bv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newLog(String.valueOf(getResources().getInteger(com.google.android.gms.R.integer.google_play_services_version)));
                bv.setEnabled(false);
            }
        });
        bfifth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("debugActivity", "pingTV starting");
                Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(DebugActivity.this).getAll();
                fireip = (String) preferences.get("example_text");

                newLog("\nping running: ip " + fireip + " please wait 5 seconds for output\n");
                newLog(pingTV(fireip));
            }
        });
        bsixth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                showNewDesign();
            }
        });
        Log.d("DebugActivity", "ready");
        // This always works
        Intent i = new Intent(DebugActivity.this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, 0);

        Intent myIntent = new Intent(DebugActivity.this, NoNonsenseFilePicker.class);
        DebugActivity.this.startActivity(myIntent);


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            Log.d("filepickerdebug", uri.getEncodedPath());
                            Log.d("filepickerdebug", uri.getPath());// Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path: paths) {
                            Uri uri = Uri.parse(path);
                            Log.d("filepickerdebug", uri.getEncodedPath());
                            Log.d("filepickerdebug", uri.getPath());// Do something with the URI
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                Log.d("filepickerdebug", uri.getEncodedPath());
                Log.d("filepickerdebug", uri.getPath());// Do something with the URI
            }
        }
    }

    public void newLog(String message) {
        EditText log = (EditText) findViewById(R.id.editTextLog);
        log.setText(log.getText() + message);
    }

    public String pingTV(String fireip) {
        //TODO ping logic:  3 pings, no backgroung, return error if unseccesfull instead of UI freeze
        Log.d("debugActivity", "pingTV starting");

        Process ping = null;
        try {
            ping = Runtime.getRuntime().exec("sh");

        } catch (IOException e1) {
            Log.e("firedebug", "IOException error e " + e1);
        }

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String output = "";
        if (ping != null) {
            outputStream = new DataOutputStream(ping.getOutputStream());
            inputStream = new DataInputStream(ping.getInputStream());
        } else {
            Log.e("firedebug", "adb == null");
        }
        try {
            if (outputStream != null) { //TODO path to ping? or builtin sh?
                outputStream.writeBytes("/system/bin/ping -c 3 " + fireip + " \n "); //todo -c3 correct?
                outputStream.flush();
                Log.d("firedebug", "/system/bin/ping -c 3 " + fireip + " \n ");

//http://stackoverflow.com/a/16563729/2359197
                int readed = 0;
                byte[] buff = new byte[4096];
                while (inputStream.available() <= 0) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex) {
                    }
                }

                while (inputStream.available() > 0) {
                    readed = inputStream.read(buff);
                    if (readed <= 0) break;
                    String seg = new String(buff, 0, readed);
                    output = seg; //result is a string to show in textview
                }

            } else {
                Log.e("firedebug", "outputStream == null");
            }
        } catch (IOException e1) {
            Log.e("firedebug", "IOException error 1" + e1);
        }

        //CLOSINGCONNECTION //should work


        //After pushing:
        try {
            if (outputStream != null) {
                outputStream.close();
            } else {
                Log.e("firedebug", "outputStream closed already ");
            }
            if (inputStream != null) {
                inputStream.close();
            } else {
                Log.e("firedebug", "inputStream closed already ");
            }
            if (ping != null) {
                ping.waitFor();
            } else {
                Log.e("firedebug", "ping closed already ");
            }
        } catch (IOException e) {
            Log.e("firedebug", "IOException error 2 " + e);
        } catch (InterruptedException e) {
            Log.e("firedebug", "InterruptedException error 5 " + e);
        }
        if (ping != null) {
            ping.destroy();
        } else {
            Log.e("firedebug", "ping already destroyed ");
        }


        return output;
    }

    private void showNewDesign() {
        Intent myIntent = new Intent(DebugActivity.this, MainActivity.class);
        DebugActivity.this.startActivity(myIntent);
    }
}
