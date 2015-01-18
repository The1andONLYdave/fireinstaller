package com.dlka.fireinstaller;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity implements
        OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

    public static final String PREFSFILE = "settings";
    public static final String SELECTED = "selected";
    private static final String TEMPLATEID = "templateid";
    private static final String PROPERTY_ID = "App";
    private static final String mailtag = "0.8_fixed";
    public String fireip = "";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    private TemplateSource templateSource = new TemplateSource(this);
    private TemplateData template;
    private AdView adView;
    int completed = 0; // this is the value for the notification percentage
    NotificationHelper notificationHelper= new NotificationHelper(this);
    int counter = 0;
    String dirs="";


    public static String noNull(String input) {
        if (input == null) {
            return "";
        }
        return input;
    }

    @Override
    protected void onCreate(Bundle b) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        ListView listView = getListView();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        Tracker t = (getTracker(
                TrackerName.APP_TRACKER));

        t.setScreenName("MainView");

        t.send(new HitBuilders.AppViewBuilder().build());


        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker t1 = analytics.newTracker(R.xml.global_tracker);
        t1.send(new HitBuilders.AppViewBuilder().build());

        // Create the adView.
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-8761501900041217/6245885681");
        adView.setAdSize(AdSize.BANNER);

        LinearLayout layout = (LinearLayout) findViewById(R.id.bannerLayout);

        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("89CADD0B4B609A30ABDCB7ED4E90A8DE")
                .addTestDevice("CCCBB7E354C2E6E64DB5A399A77298ED")  //current Nexus 4
                .addTestDevice("4DA61F48D168C897127AACD506BF35DF")  //current Note
                .build();

        adView.loadAd(adRequest);

        final Dialog dialog;
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Hello");

        Button button = (Button) dialog.findViewById(R.id.Button01);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        templateSource.open();
        List<TemplateData> formats = templateSource.list();
        SharedPreferences prefs = getSharedPreferences(PREFSFILE, 0);
        Iterator<TemplateData> it = formats.iterator();
        while (it.hasNext()) {
            template = it.next();
            if (template.id == prefs.getLong(TEMPLATEID, 0)) {
                break;
            }
            template = null;
        }
        setListAdapter(new AppAdapter(this, R.layout.app_item,
                new ArrayList<SortablePackageInfo>(), R.layout.app_item));
        new ListTask(this, R.layout.app_item).execute("");

        final Button bs = (Button) findViewById(R.id.button2);
        bs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyMenuSelect();
            }
        });

        final Button bs2 = (Button) findViewById(R.id.button1);
        bs2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPreferences();
            }
        });


    }


    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
        if (template != null) {
            editor.putLong(TEMPLATEID, template.id);
        }
        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            editor.putBoolean(SELECTED + "." + spi.packageName, spi.selected);
        }
        editor.commit();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.copy: {
                copyMenuSelect();
                break;
            }
            case (R.id.deselect_all): {
                ListAdapter adapter = getListAdapter();
                int count = adapter.getCount();
                for (int i = 0; i < count; i++) {
                    SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
                    spi.selected = false;
                }
                ((AppAdapter) adapter).notifyDataSetChanged();
                break;
            }
            case (R.id.item_help): {
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog);
                dialog.setTitle("Help Dialog 1");
                Button button = (Button) dialog.findViewById(R.id.Button01);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            }
            case (R.id.item_mail): {
                StringBuilder buffer;
                buffer = new StringBuilder();
                buffer.append("mailto:");
                buffer.append("feedback@kulsch-it.de");
                buffer.append("?subject=");
                buffer.append("Fireinstaller" + mailtag);
                buffer.append("&body=Please provide Androidversion and Device Model for Bugreport if you know it.");
                String uriString = buffer.toString().replace(" ", "%20");

                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
                break;
            }
            case (R.id.item_donate): {
                //TODO open donate-link or even in-app purchase
                Toast.makeText(this, "Isn't implemented yet. But you can buy donate-version @ google play. Or wait for in-app option with chooseable amount.", Toast.LENGTH_LONG).show();
                break;
            }
            case (R.id.item_settings): {
                showPreferences();
                break;
            }
           // case (R.id.item_debug_items): {
           //     showDebugDialog();
           //     break;
          //   }
        }
        return true;
    }

    private void showDebugDialog() {
        Intent myIntent = new Intent(MainActivity.this, DebugActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    private void showPreferences() {
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    @SuppressWarnings("unchecked")
    private void copyMenuSelect() {
        if (!isNothingSelected()) {
            Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
            fireip =(String)preferences.get("example_text");

            Toast.makeText(this, "Installing at IP" + fireip, Toast.LENGTH_LONG).show();
            Log.d("Fireinstaller", "IP ausgelesen:" + fireip);

            pushFireTv();

        } else {
            Toast.makeText(this, "no app selected", Toast.LENGTH_LONG).show();
        }
    }


    private void pushFireTv() {

        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();

        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            if (spi.selected) {
                Log.d("fireconnector", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);
                dirs=dirs+":::"+spi.sourceDir;
                counter++;//how much packages to push
            }
        }
        Log.d("fireconnector", " counter package: " + counter + ", dirs package: " + dirs);


        // this is usually performed from within an Activity
  //          Groundy.create(FireConnector.class)
    //                .callback(this)        // required if you want to get notified of your task lifecycle
      //              .arg("fireip", fireip)       // optional
        //            .arg("counter", counter)
          //          .arg("dirs", dirs)
            //        .queueUsing(MainActivity.this);

        //TODO Backgroundtask without groundy
        //TODO give fireip, counter and dirs as string, int, string
//lets start our long running process Asyncronous Task
        new LongRunningTask().execute(); //fireip,counter,dirs

            //return ret;
    }

    public boolean isNothingSelected() {
        ListAdapter adapter = getListAdapter();
        if (adapter != null) {
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
                if (spi.selected) {
                    return false;
                }
            }
        }
        Toast.makeText(this, R.string.msg_warn_nothing_selected, Toast.LENGTH_LONG)
                .show();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
        template = (TemplateData) parent.getAdapter().getItem(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        AppAdapter aa = (AppAdapter) getListAdapter();
        SortablePackageInfo spi = aa.getItem(position);
        spi.selected = !spi.selected;
        aa.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        AppAdapter aa = (AppAdapter) getListAdapter();
        SortablePackageInfo spi = aa.getItem(position);
        spi.selected = !spi.selected;
        aa.notifyDataSetChanged();
        return true;
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }


    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }


    private class LongRunningTask extends AsyncTask <String, Integer, Void>{

        private ProgressDialog dialog = null;

        //TODO: disallow orientation change while running
            @Override
            protected Void doInBackground(String... params) {

                Log.d("fireconnector","doInBackground starting");
                //String fireip=params[0];
                //int counter = 1;//TODO cast params[1];
                //String dirs = params[2];

                //working with big strings



                // lots of code
                publishProgress(0);
                Log.e("fireconnector", "0");
                //CONNECTING //should work if we call it only once (singleton making)
                Log.d("fireconnector", "connecting adb to " + fireip);
                Process adb = null;
                try {
                    adb = Runtime.getRuntime().exec("sh");

                } catch (IOException e1) {
                    Log.e("fireconnector", "IOException error e "+e1);
                }

                DataOutputStream outputStream = null;
                if (adb != null) {
                    outputStream = new DataOutputStream(adb.getOutputStream());
                }
                else{
                    Log.e("fireconnector", "abd == null");
                }
                try {
                    if (outputStream != null) {
                        outputStream.writeBytes("/system/bin/adb" + " connect " + fireip + "\n ");
                        outputStream.flush();
                        Log.d("fireconnector", "/system/bin/adb" + " connect " + fireip + "\n ");
                    }
                    else{
                        Log.e("fireconnector", "outputStream == null");
                    }


                } catch (IOException e1) {
                    Log.e("fireconnector", "IOException error 1"+e1);
                }


                //INSTALLING //maybe work
                String dir = dirs.substring(3);
                completed=1;
                publishProgress(1);//connection established
                Log.e("fireconnector", "1");

                //if(!dir.contains(":::")){return succeeded().add("the_result","cant split string");}
                String[] sourceDir = dir.split(":::");
                completed=2;
                publishProgress();//get packages ready
                Log.e("fireconnector", "2");

                for (int i = 0; i < counter; i++) {
                    //first dirs -- first 3 :
                    completed = i+3;
                    publishProgress();//do package nr i
                    Log.e("fireconnector", "3");
                    //then split as often as ValueOf counter by stripping first chars till :::


                    try {
                        //move apk to fire tv here
                        //Foreach Entry do and show progress thing:

                        if (outputStream != null) {
                            Log.d("fireconnector", "/system/bin/adb install " + sourceDir[i] + "\n");
                            outputStream.writeBytes("/system/bin/adb install " + sourceDir[i] + "\n");
                            outputStream.flush();
                        }
                        else{
                            Log.e("fireconnector", "outputStream == null (occurence 2)");
                        }

                    } catch (IOException e) {
                        Log.e("fireconnector", " IOException error "+e);
                    }


                } //end for loop






                //CLOSINGCONNECTION //should work

                //TODO better logging with errorcontent too

                //After pushing:
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    else{
                        Log.e("fireconnector", "outputStream closed already ");
                    }
                    if (adb != null) {
                        adb.waitFor();
                    }
                    else{
                        Log.e("fireconnector", "adb closed already ");
                    }
                } catch (IOException e) {
                    Log.e("fireconnector", "IOException error 2 "+e);
                } catch (InterruptedException e) {
                    Log.e("fireconnector", "InterruptedException error 5 "+e);
                }
                if (adb != null) {
                    adb.destroy();
                }
                else{
                    Log.e("fireconnector", "adb already destroyed ");
                }


                completed = 100;

                //lets call our onProgressUpdate() method which runs on the UI thread
                publishProgress(100);
                Log.e("fireconnector", "100");
                return null;
            }




        @Override
        protected void onPreExecute() {
            lockScreenOrientation();

            completed = 0;
            notificationHelper.createNotification();

            dialog = ProgressDialog.show(MainActivity.this, "Loading", "Please Wait, \nProgress in Notification Bar. \n", true);
            dialog.setCancelable(false); //no cancel dialog while installing
        }

        protected void onProgressUpdate(Integer... v) {
//lets format a string from the the 'completed' variable

            Log.d("fireinstaller","completed "+completed+" v "+v);
            //TODO Switch-case(0 start, 1 connected, 2 prepared packages, 3 installing first app, 4 installing next app, 100 finished
                notificationHelper.progressUpdate(completed);

            String dialogMessage="Please Wait, \nProgress in Notification Bar. \n";
            String contentText;

            if(completed==0){
                contentText = "Connecting to Fire TV...";
            }
            else if(completed==1){
                contentText = "Fire TV connected... Preparing apps to push.";
            }
            else if(completed==2){
                contentText = "Begin installing";
            }
            else if((completed>2)&(completed<100)){
                contentText = "Installing App Number "+completed + ". May take long time.";
            }
            else if(completed==100){
                contentText = "Installing complete. Thank you!";
            }
            else{
                contentText = "Unallowed percentageComplete. Please report error via email";
            }

                dialog.setMessage(dialogMessage+contentText);
            }

        protected void onPostExecute(final Void result) {
        //this should be self explanatory
                notificationHelper.completed();
            dialog.setCancelable(true);
            dialog.setProgress(100);
            dialog.dismiss();
            unlockScreenOrientation();

        }

        private void lockScreenOrientation() {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        private void unlockScreenOrientation() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        public void onCancel(DialogInterface theDialog) {
            cancel(true);
            //TODO stop installertask
        }

        }
}


