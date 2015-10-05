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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.frederico.showtipsview.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import cn.pedant.SweetAlert.SweetAlertDialog;



public class MainActivity extends ListActivity implements
        OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

    public static final String PREFSFILE = "settings2";
    public static final String SELECTED = "selected";
    private static final String PROPERTY_ID = "App";
    private static final String mailtag = BuildConfig.VERSION_NAME;
    public String fireip = "";
    public boolean notificationDisplay = false;
    public boolean debugDisplay = false;
    private AdView adView;
    int completed = 0; // this is the value for the notification percentage
    int counter = 0;
    String dirs = "";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    TextView textAbove;
    EditText debugView;


    public static String noNull(String input) {
        if (input == null) {
            return "";
        }
        return input;
    }

    @Override
    protected void onCreate(Bundle b) {
        if (BuildConfig.DEBUG) {
       //     Log.initialize(this);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(b);
        // requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);


        ListView listView = getListView();
        listView.setOnItemClickListener(this);

        Tracker t = (getTracker(
                TrackerName.APP_TRACKER));

        t.setScreenName("MainView");
        t.send(new HitBuilders.AppViewBuilder().build());

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker t1 = analytics.newTracker(R.xml.global_tracker);

        //fix for white or black empty screen on app startup, see https://code.google.com/p/android/issues/detail?id=82157
        t.enableExceptionReporting(false);
        //fix for white or black empty screen on app startup, see https://code.google.com/p/android/issues/detail?id=82157
        t1.enableExceptionReporting(false);

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
                            //TODO current tablet
                    .build();

            adView.loadAd(adRequest);
        }
        else{
            layout.setPadding(0,0,0,0); //free ad-space for donate version
        }


        //while showing helpdialog we build list in background for ready when user read.


        setListAdapter(new AppAdapter(this, R.layout.app_item,
                new ArrayList<SortablePackageInfo>(), R.layout.app_item));
        new ListTask(this, R.layout.app_item).execute("");

        final Button bs = (Button) findViewById(R.id.button2);
        bs.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialogBeforeInstall();
            }
        });

        final Button bs2 = (Button) findViewById(R.id.button1);
        bs2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showPreferences();
            }
        });

        t1.send(new HitBuilders.AppViewBuilder().build());

        final Dialog dialog;
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Hello");

        Button button = (Button) dialog.findViewById(R.id.Button01);
        final CheckBox dontShowAgain = (CheckBox) dialog.findViewById(R.id.checkBox);
        button.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View view) {


                String checkBoxResult = "NOT checked";
                if (dontShowAgain.isChecked())
                    checkBoxResult = "checked";
                SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
                editor.putString("skipMessage", checkBoxResult);
                // Commit the edits!
                editor.commit();
                dialog.dismiss();

            }
        });


        findViewById(R.id.floatinghelp).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               /* SharedPreferences settings = getSharedPreferences(PREFSFILE, 0);
                String skipMessage = settings.getString("skipMessage", "NOT checked");
                if (!skipMessage.equals("checked"))
                */
                dialog.show();
            }
        });


                SharedPreferences settings = getSharedPreferences(PREFSFILE, 0);
                String skipMessage = settings.getString("skipMessage", "NOT checked");
                if (!skipMessage.equals("checked"))
                showIntroHelp();


    }

    private void showIntroHelp() {
        final Button bs2 = (Button) findViewById(R.id.button1);
        final Button bs = (Button) findViewById(R.id.button2);
        final ListView listView = getListView();



        ShowTipsView showtips = new ShowTipsBuilder(this)
                .setTarget(bs2)
                .setTitle("Please open Settings")
                .setDescription("And enter your Fire's IP-Adress")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips2 = new ShowTipsBuilder(this)
                //.setTarget(listView)
                .setTarget(listView, 100, 150, 200)
                .setTitle("select some apps")
                .setDescription("from all installed apps")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips3 = new ShowTipsBuilder(this)
                .setTarget(bs)
                .setTitle("and finally")
                .setDescription("install them on your Fire-Device with this Button")
                .setDelay(1000)
                .build();

        final View bhelp = findViewById(R.id.floatinghelp);

        final ShowTipsView showtips4 = new ShowTipsBuilder(this)
                .setTarget(bhelp)
                .setTitle("Need help?")
                .setDescription("Press this Button for more information, like where to find installed apps on Fire-Device, how to know IP-Adress and more.")
                .setDelay(1000)
                .build();


        showtips.setCallback(new ShowTipsViewInterface() {
            @Override
            public void gotItClicked() {
                showtips2.show(MainActivity.this);
            }
        });
        showtips2.setCallback(new ShowTipsViewInterface() {
            @Override
            public void gotItClicked() {
                showtips3.show(MainActivity.this);
            }
        });
        showtips3.setCallback(new ShowTipsViewInterface() {
            @Override
            public void gotItClicked() {
                showtips4.show(MainActivity.this);
            }
        });
        showtips4.setCallback(new ShowTipsViewInterface() {
            @Override
            public void gotItClicked() {

            }
        });

        showtips.show(this);
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
        AppRate.with(MainActivity.this).checkAndShow();
    }


    @Override
    public void onPause() {
        if (!BuildConfig.IS_PRO_VERSION) {
            adView.pause();
        }
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();

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
                dialogBeforeInstall();
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
                dialog.setTitle("Help Dialog");
                Button button = (Button) dialog.findViewById(R.id.Button01);
                final CheckBox dontShowAgain = (CheckBox) dialog.findViewById(R.id.checkBox);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String checkBoxResult = "NOT checked";
                        if (dontShowAgain.isChecked())
                            checkBoxResult = "checked";
                        SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
                        editor.putString("skipMessage", checkBoxResult);
                        // Commit the edits!
                        editor.commit();
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
                showDonationActivity();
                break;
            }
            case (R.id.item_settings): {
                showPreferences();
                break;
            }
        }
        return true;
    }

    private void showPreferences() {
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    private void showDonationActivity() {
        Intent myIntent = new Intent(MainActivity.this, DonationsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }


    public void LogToView(String title, String message) {
        Log.d(title, message);
        EditText debugView = (EditText) findViewById(R.id.debugText);
        debugView.setText(debugView.getText() + title + " : " + message + "\n");
    }

    private void dialogBeforeInstall(){

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        fireip = (String) preferences.get("example_text");

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Do you want to install at ip " + fireip)
                .setCancelText("No,cancel plx!")
                .setConfirmText("Yes,do it!")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        copyMenuSelect();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .show();
    }
    private void copyMenuSelect() {

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        fireip = (String) preferences.get("example_text");

        if (!isNothingSelected()) {

            notificationDisplay = (Boolean) preferences.get("notifications_new_message");

            debugDisplay = (Boolean) preferences.get("debug_view_enabled");

            TextView textAbove = (TextView) findViewById(R.id.format_as);
            EditText debugView = (EditText) findViewById(R.id.debugText);

            textAbove.setVisibility(View.GONE);
            if (debugDisplay) {
                debugView.setVisibility(View.VISIBLE);
                Log.d("Fireinstaller", "debug message test");
                Log.e("Fireinstaller", "failure message test");
            }

            Toast.makeText(this, "Installing at IP" + fireip, Toast.LENGTH_LONG).show();
            LogToView("Fireinstaller", "IP ausgelesen:" + fireip);

            pushFireTv();

        } else {
            Toast.makeText(this, "no app selected", Toast.LENGTH_LONG).show();
        }
    }


    private void pushFireTv() {

        //v0.9.2


        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();

        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            if (spi.selected) {
                LogToView("fireconnector", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);
                dirs = dirs + ":::" + spi.sourceDir;
                counter++;//how much packages to push
            }
        }
        LogToView("fireconnector", " counter package: " + counter + ", dirs package: " + dirs);


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


    private class LongRunningTask extends AsyncTask<String, Integer, Void> {

        private ProgressDialog dialog = null;

        //TODO: disallow orientation change while running
        @Override
        protected Void doInBackground(String... params) {

            Log.d("fireconnector", "doInBackground starting");
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
                publishProgress(501);
                Log.e("fireconnector", "IOException error e " + e1);
            }

            DataOutputStream outputStream = null;
            if (adb != null) {
                outputStream = new DataOutputStream(adb.getOutputStream());
            } else {
                publishProgress(502);
                Log.e("fireconnector", "adb == null");
            }
            try {
                if (outputStream != null) {
                    outputStream.writeBytes("/system/bin/adb" + " connect " + fireip + "\n ");
                    outputStream.flush();
                    Log.d("fireconnector", "/system/bin/adb" + " connect " + fireip + "\n ");
                } else {
                    publishProgress(503);
                    Log.e("fireconnector", "outputStream == null");
                }


            } catch (IOException e1) {
                publishProgress(504);
                Log.e("fireconnector", "IOException error 1" + e1);
            }


            //INSTALLING //maybe work
            String dir = dirs.substring(3);
            completed = 1;
            publishProgress(1);//connection established
            Log.e("fireconnector", "1");

            //if(!dir.contains(":::")){return succeeded().add("the_result","cant split string");}
            String[] sourceDir = dir.split(":::");
            completed = 2;
            publishProgress();//get packages ready
            Log.e("fireconnector", "2");

            for (int i = 0; i < counter; i++) {
                //first dirs -- first 3 :
                completed = i + 3;
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
                    } else {
                        publishProgress(505);
                        Log.e("fireconnector", "outputStream == null (occurence 2)");
                    }

                } catch (IOException e) {
                    publishProgress(506);
                    Log.e("fireconnector", " IOException error " + e);
                }


            } //end for loop


            //CLOSINGCONNECTION //should work

            //TODO better logging with errorcontent too

            //After pushing:
            try {
                if (outputStream != null) {
                    outputStream.close();
                } else {
                    publishProgress(507);
                    Log.e("fireconnector", "outputStream closed already ");
                }
                if (adb != null) {
                    adb.waitFor();
                } else {
                    publishProgress(508);
                    Log.e("fireconnector", "adb closed already ");
                }
            } catch (IOException e) {
                publishProgress(509);
                Log.e("fireconnector", "IOException error 2 " + e);
            } catch (InterruptedException e) {
                publishProgress(510);
                Log.e("fireconnector", "InterruptedException error 5 " + e);
            }
            if (adb != null) {
                adb.destroy();
            } else {
                publishProgress(511);
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
            if (notificationDisplay == true) {
                //notificationHelper.createNotification();
            }
            if (!debugDisplay) {
                dialog = ProgressDialog.show(MainActivity.this, "doing my work...", "Please Wait, \nProgress in Notification Bar. \n", true);
                dialog.setCancelable(false); //no cancel dialog while installing
            }
        }

        protected void onProgressUpdate(Integer... v) {
//lets format a string from the the 'completed' variable

            LogToView("fireinstaller", "completed " + completed + " v " + v); //TODO why we used to log v here?
            //TODO Switch-case(0 start, 1 connected, 2 prepared packages, 3 installing first app, 4 installing next app, 100 finished
            if (notificationDisplay == true) {
                //notificationHelper.progressUpdate(completed);
            }
            String dialogMessage = "Please Wait, \nProgress in Notification Bar. \n";
            String contentText = "";

            if (completed == 0) {
                contentText = "Connecting to Fire TV...";
                LogToView("fireinstaller", "Connecting to Fire TV...\n");
            } else if (completed == 1) {
                contentText = "Fire TV connected... Preparing apps to push.";
                LogToView("fireinstaller", "Fire TV connected... Preparing apps to push.\n");
            } else if (completed == 2) {
                contentText = "Begin installing";
                LogToView("fireinstaller", "Begin installing\n");
            } else if ((completed > 2) & (completed < 100)) {
                contentText = "Installing App Number " + (completed - 2) + " of " + counter + ".\n May take long time.\nIf no progress after some Minutes: Check if IP " + fireip + " is correct on your Fire TV. If it's wrong, just (force close and) restart this app. Then open Settings and enter correct IP (see menu: help for more).\nWhen this window disappears everything is installed. You can also enable (sometimes bugged) notifications for progress in settings of the app. Thank you for using my app!";
                LogToView("fireinstaller", "Installing App Number " + (completed - 2) + " of " + counter + ".\n");
            } else if (completed == 100) {
                contentText = "Installing complete. Thank you!";
                LogToView("fireinstaller", "Installing complete. Thank you!\n");
            } else if (completed == 501) {
                LogToView("fireconnector", "IOException error e, cant get sh to exec ");
            } else if (completed == 502) {
                LogToView("fireconnector", "adb == null");
            } else if (completed == 503) {
                LogToView("fireconnector", "outputStream == null");
            } else if (completed == 504) {
                LogToView("fireconnector", "IOException error 1, outputstream error?");
            } else if (completed == 505) {
                LogToView("fireconnector", "outputStream == null (occurence 2)");
            } else if (completed == 506) {
                LogToView("fireconnector", " IOException error, outputstream error2 ? ");
            } else if (completed == 507) {
                LogToView("fireconnector", "outputStream closed already ");
            } else if (completed == 508) {
                LogToView("fireconnector", "adb closed already ");
            } else if (completed == 509) {
                LogToView("fireconnector", "IOException error 2 ");
            } else if (completed == 510) {
                LogToView("fireconnector", "InterruptedException error 5 ");
            } else if (completed == 511) {
                LogToView("fireconnector", "adb already destroyed ");
            } else {
                contentText = "Unallowed percentageComplete. Please report error via email";
                LogToView("fireinstaller", "Unallowed percentageComplete. Please report error via email");
            }

            if (!debugDisplay) {
                dialog.setMessage(dialogMessage + contentText);
            }

        }

        protected void onPostExecute(final Void result) {
            //this should be self explanatory
            if (notificationDisplay == true) {
                //notificationHelper.completed();
            }
            if (!debugDisplay) {
                dialog.setCancelable(true);
                dialog.setProgress(100);
                dialog.dismiss();
            }
            unlockScreenOrientation();
            //fix for increasing number when more installations without app closing in between.
            completed = 0;
            counter = 0;
            if (!debugDisplay) {
                TextView textAbove = (TextView) findViewById(R.id.format_as); //make sure we don't call an empty reference at textAbove
                textAbove.setVisibility(View.VISIBLE);
            }
            LogToView("fireinstaller", "complete. READY?\n");
        }

        private void lockScreenOrientation() {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                LogToView("SCREEN_ORIENTATION_PORTRAIT", " calling locking.\n");
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                LogToView("SCREEN_ORIENTATION_LANDSCAPE", " calling locking.\n");
            }
        }

        private void unlockScreenOrientation() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            LogToView("SCREEN_ORIENTATION_SENSOR", " calling unlock.\n");
        }

        public void onCancel(DialogInterface theDialog) {
            LogToView("onCancel called", " this should not happen...\n");
            cancel(true);
            //TODO stop installertask
        }

    }
}


