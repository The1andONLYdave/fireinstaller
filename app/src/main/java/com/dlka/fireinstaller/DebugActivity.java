package com.dlka.fireinstaller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import fr.nicolaspomepuy.discreetapprate.AppRate;


public class DebugActivity extends Activity {
    public String fireip = "";
    public String deviceId = "";
    public String android_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        final Button bt = findViewById(R.id.button3);
        final Button bv = findViewById(R.id.button4);
        final Button bfifth = findViewById(R.id.button5);

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newLog(detectArch());
            }
        });
        bv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendDeveloperMail();
            }
        });
        bfifth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Map<String, ?> preferences =
                        PreferenceManager.getDefaultSharedPreferences(DebugActivity.this).getAll();
                fireip = (String) preferences.get("example_text");

                newLog("\nping running: ip " + fireip + " please wait 5 seconds for output\n");
                newLog(pingTV(fireip));
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.bannerLayout);
        layout.setVisibility(View.INVISIBLE);
        layout.setPadding(0, 0, 0, 0); //free ad-space for donate version
    }

    public void newLog(String message) {
        EditText log = findViewById(R.id.editTextLog);
        log.setText(log.getText() + message);
    }

    public String detectArch() {
        Process detect = null;
        try {
            detect = Runtime.getRuntime().exec("sh");

        } catch (IOException e1) {
            newLog(e1.getMessage());
        }

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String output = "";
        if (detect != null) {
            outputStream = new DataOutputStream(detect.getOutputStream());
            inputStream = new DataInputStream(detect.getInputStream());
        }
        try {
            if (outputStream != null) {
                outputStream.writeBytes("uname -m \n ");
                outputStream.flush();

                int readed;
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
                    output = new String(buff, 0, readed);
                    //result is a string to show in textview
                }

            }
        } catch (IOException e1) {
            newLog(e1.getMessage());
        }

        //CLOSINGCONNECTION //should work


        //After pushing:
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (detect != null) {
                detect.waitFor();
            }
        } catch (IOException e) {
            newLog(e.getMessage());
        } catch (InterruptedException e) {
            newLog(e.getMessage());
        }
        if (detect != null) {
            detect.destroy();
        }

        return output;
    }

    public String pingTV(String fireip) {
        //TODO ping logic:  3 pings, no background, return error if unsuccesfull instead of UI freeze

        Process ping = null;
        try {
            ping = Runtime.getRuntime().exec("sh");

        } catch (IOException e1) {
            newLog(e1.getMessage());
        }

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String output = "";
        if (ping != null) {
            outputStream = new DataOutputStream(ping.getOutputStream());
            inputStream = new DataInputStream(ping.getInputStream());
        }
        try {
            if (outputStream != null) { //TODO path to ping? or builtin sh?
                outputStream.writeBytes("/system/bin/ping -c 3 " + fireip + " \n "); //todo -c3 correct?
                outputStream.flush();

                //http://stackoverflow.com/a/16563729/2359197
                int readed;
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

            }
        } catch (IOException e1) {
            newLog(e1.getMessage());
        }
        //CLOSINGCONNECTION //should work

        //After pushing:
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (ping != null) {
                ping.waitFor();
            }
        } catch (IOException e) {
            newLog(e.getMessage());
        } catch (InterruptedException e) {
            newLog(e.getMessage());
        }
        if (ping != null) {
            ping.destroy();
        }

        return output;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppRate.with(DebugActivity.this).checkAndShow();
    }



    private void sendDeveloperMail() {
        String ARCH = detectArch();
        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(DebugActivity.this).getAll();
        fireip = (String) preferences.get("example_text");
        String GPS = (String.valueOf(getResources().getInteger(com.google.android.gms.R.integer.google_play_services_version)));
        StringBuilder buffer;
        buffer = new StringBuilder();
        buffer.append("mailto:");
        buffer.append("feedback@kulsch-it.de");
        buffer.append("?subject=");
        buffer.append("Fireinstaller DEBUG");
        buffer.append("&body=" + deviceId + "\n" + android_id + "\n" + ARCH + "\n" + fireip + "\n" + GPS + "\n Please provide Androidversion and Device Model for Bugreport if you know it.");
        String uriString = buffer.toString().replace(" ", "%20");

        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
    }
}
