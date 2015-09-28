package com.dlka.fireinstaller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class DebugActivity extends Activity {

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
        bf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationHelper.createNotification();
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
            }
        });
        bv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newLog(String.valueOf(getResources().getInteger(com.google.android.gms.R.integer.google_play_services_version)));
            }
        });

    }
    public void newLog(String message){
        EditText log = (EditText) findViewById(R.id.editTextLog);
        log.setText(log.getText()+message);
    }

}
