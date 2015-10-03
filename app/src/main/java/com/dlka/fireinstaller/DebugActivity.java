package com.dlka.fireinstaller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;


public class DebugActivity extends Activity {
    public String fireip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        final int completed = 10; // this is the value for the notification percentage
        final NotificationHelper notificationHelper = new NotificationHelper(this);

        final Button bf = (Button) findViewById(R.id.button);
        final Button bs = (Button) findViewById(R.id.button2);
        final Button bt = (Button) findViewById(R.id.button3);
        final Button bv = (Button) findViewById(R.id.button4);
        final Button bfifth = (Button) findViewById(R.id.button5);
        final Button bsixth = (Button) findViewById(R.id.button5);
        int i = 0;

        bf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationHelper.createNotification();
                bs.setEnabled(true);
                bt.setEnabled(true);
                bf.setEnabled(false);
                bsixth.setEnabled(false);
            }
        });
        bs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationHelper.progressUpdate(completed);
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationHelper.completed();
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
                Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(DebugActivity.this).getAll();
                fireip = (String) preferences.get("example_text");

                newLog("\nping running: ip " + fireip + " please wait 5 seconds for output\n");
                newLog(pingTV(fireip));
            }
        });
        bsixth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showNewDesign();
            }
        });

    }

    public void newLog(String message) {
        EditText log = (EditText) findViewById(R.id.editTextLog);
        log.setText(log.getText()+message);
    }
    public String pingTV(String fireip){
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
                    while( inputStream.available() <= 0) {
                        try { Thread.sleep(5000); } catch(Exception ex) {}
                    }

                    while( inputStream.available() > 0) {
                        readed = inputStream.read(buff);
                        if ( readed <= 0 ) break;
                        String seg = new String(buff,0,readed);
                        output=seg; //result is a string to show in textview
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
        Intent myIntent = new Intent(DebugActivity.this, MainActivity2.class);
        DebugActivity.this.startActivity(myIntent);
    }
}
