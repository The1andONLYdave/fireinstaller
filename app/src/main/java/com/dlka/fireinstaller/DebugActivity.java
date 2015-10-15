package com.dlka.fireinstaller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import fr.nicolaspomepuy.discreetapprate.AppRate;


public class DebugActivity extends Activity {
    public String fireip = "";
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        final Button bv = (Button) findViewById(R.id.button4);
        final Button bfifth = (Button) findViewById(R.id.button5);

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

        Log.d("DebugActivity", "ready");

        LinearLayout layout = (LinearLayout) findViewById(R.id.bannerLayout);
        layout.setVisibility(View.INVISIBLE);

        if (!BuildConfig.IS_PRO_VERSION) {

            // Create the adView.
            adView = new AdView(this);
            adView.setAdUnitId("ca-app-pub-8761501900041217/6245885681");
            adView.setAdSize(AdSize.BANNER);

            layout.setVisibility(View.VISIBLE);
            layout.addView(adView);

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("89CADD0B4B609A30ABDCB7ED4E90A8DE")
                    .addTestDevice("CCCBB7E354C2E6E64DB5A399A77298ED")  //current Nexus 4
                    .addTestDevice("4DA61F48D168C897127AACD506BF35DF")  //current Note
                    .addTestDevice("9190B60D7EC5559B167C1AF6D89D714A")  // Nexus 4
                            //TODO current tablet
                    .build();

            adView.loadAd(adRequest);
        } else {
            layout.setPadding(0, 0, 0, 0); //free ad-space for donate version
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

    @Override
    public void onDestroy() {
        if (!BuildConfig.IS_PRO_VERSION) {
            adView.destroy();
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.IS_PRO_VERSION) {
            adView.resume();
        }
        AppRate.with(DebugActivity.this).checkAndShow();
    }


    @Override
    public void onPause() {
        if (!BuildConfig.IS_PRO_VERSION) {
            adView.pause();
        }
    }
}
