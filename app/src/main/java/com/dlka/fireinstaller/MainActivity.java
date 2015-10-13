package com.dlka.fireinstaller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.frederico.showtipsview.ShowTipsBuilder;
import net.frederico.showtipsview.ShowTipsView;
import net.frederico.showtipsview.ShowTipsViewInterface;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.nicolaspomepuy.discreetapprate.AppRate;


public class MainActivity extends ListActivity implements
        OnItemSelectedListener, OnItemClickListener {

    public static final String PREFSFILE = "settings2";
    public static final String SELECTED = "selected";
    private static final String PROPERTY_ID = "App";
    private static final String mailtag = BuildConfig.VERSION_NAME;
    public String fireip = "";
    public boolean notificationDisplay = false;
    public boolean debugDisplay = false;
    private AdView adView;
    public boolean installAPKdirectly = false;
    public String encPath = null;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    TextView textAbove;
    EditText debugView;
    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);


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
        new DrawerBuilder()
                .withActionBarDrawerToggle(false)
                .withTranslucentStatusBar(false)
                .withSystemUIHidden(false)
                .withActivity(this).build();

        //floating action menu
        final View actionB = findViewById(R.id.action_b);
        actionB.setVisibility(View.GONE);
        FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
        actionC.setTitle("Expert Option?");
        actionC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        menuMultipleActions.addButton(actionC);

        final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionA.setTitle("Action A clicked");
            }
        });


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
        } else {
            layout.setPadding(0, 0, 0, 0); //free ad-space for donate version
        }


        //while showing helpdialog we build list in background for ready when user read.


        setListAdapter(new AppAdapter(this, R.layout.app_item,
                new ArrayList<SortablePackageInfo>(), R.layout.app_item));
        new ListTask(this, R.layout.app_item).execute("");

        final View bs = (View) findViewById(R.id.firetv);
        bs.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialogBeforeInstall();
            }
        });

        final View bs2 = (View) findViewById(R.id.action_a);
        bs2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showPreferences();
            }
        });

        final View bs3 = (View) findViewById(R.id.action_b);
        bs3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showFilePicker();
            }
        });

        t1.send(new HitBuilders.AppViewBuilder().build());


        findViewById(R.id.floatinghelp).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogHelp();
            }
        });


        SharedPreferences settings = getSharedPreferences(PREFSFILE, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");
        if (!skipMessage.equals("checked"))
            showIntroHelp();


    }

    private void showIntroHelp() {
        final ListView listView = getListView();
        final View firetv = findViewById(R.id.firetv);
        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        final View action_a = findViewById(R.id.action_a);

        menuMultipleActions.expand();

        ShowTipsView showtips = new ShowTipsBuilder(this)
                .setTarget(action_a)
                .setTitle("Please open Settings")
                .setDescription("and enter your Fire's IP-Address (Network Address)")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips2 = new ShowTipsBuilder(this)
                .setTarget(listView, 100, 150, 200)
                .setTitle("select some apps")
                .setDescription("from all installed apps (filepicker in development for non-installed apps)")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips3 = new ShowTipsBuilder(this)
                .setTarget(firetv)
                .setTitle("and finally")
                .setDescription("install them on your Fire-Device with this Button")
                .setDelay(1000)
                .build();

        final View bhelp = findViewById(R.id.floatinghelp);

        final ShowTipsView showtips4 = new ShowTipsBuilder(this)
                .setTarget(bhelp)
                .setTitle("Need help?")
                .setDescription("Press this Button for more information, like where to find installed apps on Fire-Device, how to know IP-Adress and more.\n" +
                        "Hint of the day: There's a checkbox for hiding this interactive-Guide on start ;)\n\nHope you enjoy my app. Have Fun!\n\nFeeling adventurous? Try Menu -> Sideloading, push any apk on your phones sd-card.\n\n")
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
                menuMultipleActions.collapse();
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
                deselectAllList();
                break;
            }
            case (R.id.item_help): {
                showDialogHelp();
                break;
            }
            case (R.id.item_mail): {
                sendDeveloperMail();
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
            case (R.id.item_externalAPK): {
                showFilePicker();
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

    private void showDialogHelp() {
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
    }

    private void showFilePicker() {
        // This always works
        Intent i = new Intent(MainActivity.this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, 0);
    }

    private void sendDeveloperMail() {
        StringBuilder buffer;
        buffer = new StringBuilder();
        buffer.append("mailto:");
        buffer.append("feedback@kulsch-it.de");
        buffer.append("?subject=");
        buffer.append("Fireinstaller" + mailtag);
        buffer.append("&body=Please provide Androidversion and Device Model for Bugreport if you know it.");
        String uriString = buffer.toString().replace(" ", "%20");

        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
    }

    public void LogToView(String title, String message) {
        Log.d(title, message);
        EditText debugView = (EditText) findViewById(R.id.debugText);
        debugView.setText(debugView.getText() + title + " : " + message + "\n");
    }

    private void dialogBeforeInstall() {

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        fireip = (String) preferences.get("example_text");
        if (fireip.equals("192.0.0.0")) {
            Toast.makeText(this, "Target? Please enter Fire's IP-Address before.", Toast.LENGTH_LONG).show();
            showPreferences();
            return;
        }
        if (isValidIPV4(fireip) == false) {
            Toast.makeText(this, "Wrong IP Syntax. Please enter Fire's IP-Address before.", Toast.LENGTH_LONG).show();
            showPreferences();
            return;
        }

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

    private void deselectAllList() {
        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            spi.selected = false;
        }
        ((AppAdapter) adapter).notifyDataSetChanged();
    }

    private void copyMenuSelect() {

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        fireip = (String) preferences.get("example_text");

        if ((!isNothingSelected()) || (installAPKdirectly == true)) {

            //notificationDisplay = (Boolean) preferences.get("notifications_new_message");

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

    public void pushFireTv() {

        //lets start our long running process Asyncronous Task
        new LongRunningTask().execute();

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


    private class LongRunningTask extends AsyncTask<String, String, Void> {

        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        @Override
        protected Void doInBackground(String... params) {

            Log.d("fireconnector", "doInBackground starting");
            // lots of code
            publishProgress("Connecting to Fire TV at " + fireip);
            Log.e("fireconnector", "0");
            //CONNECTING //should work if we call it only once (singleton making)
            Log.d("fireconnector", "connecting adb to " + fireip);
            Process adb = null;
            try {
                adb = Runtime.getRuntime().exec("sh");

            } catch (IOException e1) {
                publishProgress("IOException error e" + e1.toString());
                Log.e("fireconnector", "IOException error e " + e1);
            }

            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;
            String output = "";

            if (adb != null) {
                outputStream = new DataOutputStream(adb.getOutputStream());
                inputStream = new DataInputStream(adb.getInputStream());
            } else {
                publishProgress("asb == null");
                Log.e("fireconnector", "adb == null");
            }
            try {
                if (outputStream != null) {
                    outputStream.writeBytes("/system/bin/adb" + " connect " + fireip + "\n ");
                    outputStream.flush();
                    Log.d("fireconnector", "/system/bin/adb" + " connect " + fireip + "\n ");

                    int readed = 0;
                    byte[] buff = new byte[4096];
                    while (inputStream.available() <= 0) {
                        try {
                            Thread.sleep(100); //TODO: does this check every 100ms for output, then continues?
                        } catch (Exception ex) {
                        }
                    }

                    while (inputStream.available() > 0) {
                        readed = inputStream.read(buff);
                        if (readed <= 0) break;
                        String seg = new String(buff, 0, readed);
                        output = seg; //result is a string to show in textview
                    }
                    publishProgress("adb_output_connect" + output);

                } else {
                    publishProgress("outputStream == null");
                    Log.e("fireconnector", "outputStream == null");
                }


            } catch (IOException e1) {
                publishProgress("IOException error 1" + e1.toString());
                Log.e("fireconnector", "IOException error 1" + e1);
            }


            //TODO: why we parse everything to string and separate it here, can't we access getListAdapter in onBackground?
            ListAdapter adapter = getListAdapter();
            int count = adapter.getCount();


            publishProgress("connection established at " + fireip + ". Preparing apps to push.");
            Log.e("fireconnector", "1");

            publishProgress("get packages ready");
            Log.e("fireconnector", "2");

            if (installAPKdirectly == true) {
                if (!(encPath.isEmpty())) {
                    Log.d("fireconnector", " external install called " + encPath);

                    publishProgress("Install external APK. " + encPath + " . \n\n" +
                            " May take long time.\n\n\n" +
                            "If no progress after some Minutes: Check if IP " + fireip + " is correct on your Fire TV. " +
                            "If it's wrong, just (force close and) restart this app. " +
                            "Then open Settings and enter correct IP (see menu: help for more).\n" +
                            "When this window disappears everything is installed. Thank you for using my app!");

                    Log.e("fireconnector", "3");

                    try {
                        //move apk to fire tv here
                        //Foreach Entry do and show progress thing:

                        if (outputStream != null) {
                            Log.d("fireconnector", "/system/bin/adb install " + encPath + "\n");
                            outputStream.writeBytes("/system/bin/adb install " + encPath + "\n");
                            outputStream.flush();

                            int readed = 0;
                            byte[] buff = new byte[4096];

                            while (inputStream.available() > 0) {
                                readed = inputStream.read(buff);
                                if (readed <= 0) break;
                                String seg = new String(buff, 0, readed);
                                output = seg; //result is a string to show in textview
                            }
                            publishProgress("adb_output" + output);
                            Log.d("adb output directly", output);
                        } else {
                            publishProgress("outputStream == null (occurence 2)");
                            Log.e("fireconnector", "outputStream == null (occurence 2)");
                        }
                    } catch (IOException e) {
                        publishProgress(" IOException error " + e.toString());
                        Log.e("fireconnector", " IOException error " + e);
                    }
                }
            } else {
                for (int i = 0; i < count; i++) {
                    SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
                    if (spi.selected) {
                        Log.d("fireconnector", " ret.append package: " + spi.displayName + ", sourceDir: " + spi.sourceDir);

                        publishProgress("Install:\"" + spi.displayName + "\"  (app number " + (i + 1) + ") from " + spi.sourceDir + " . \n\n" +
                                " May take long time.\n\n\n" +
                                "If no progress after some Minutes: Check if IP " + fireip + " is correct on your Fire TV. " +
                                "If it's wrong, just (force close and) restart this app. " +
                                "Then open Settings and enter correct IP (see menu: help for more).\n" +
                                "When this window disappears everything is installed. Thank you for using my app!");

                        Log.e("fireconnector", "3");

                        try {
                            //move apk to fire tv here
                            //Foreach Entry do and show progress thing:

                            if (outputStream != null) {
                                Log.d("fireconnector", "/system/bin/adb install -r " + spi.sourceDir + "\n");//add -g, ‚grant permissions on non-interactive install
                                outputStream.writeBytes("/system/bin/adb install -r " + spi.sourceDir + "\n");
                                outputStream.flush();

                                int readed = 0;
                                byte[] buff = new byte[4096];


                                while (inputStream.available() > 0) {
                                    readed = inputStream.read(buff);
                                    if (readed <= 0) break;
                                    String seg = new String(buff, 0, readed);
                                    output = seg; //result is a string to show in textview
                                }
                                publishProgress("adb_output" + output);
                                Log.d("adb output list", output);
                            } else {
                                publishProgress("outputStream == null (occurence 2)");
                                Log.e("fireconnector", "outputStream == null (occurence 2)");
                            }
                        } catch (IOException e) {
                            publishProgress(" IOException error " + e.toString());
                            Log.e("fireconnector", " IOException error " + e);
                        }
                    }
                }
                try {
                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                } catch (IOException e) {
                    publishProgress(" IOException error " + e.toString());
                    Log.e("fireconnector", " IOException error " + e);
                }
            }//end for loop


            //CLOSINGCONNECTION //should work

            //TODO better logging with errorcontent too
            publishProgress("Closing Connections. Installation complete. Thank you!");
            Log.e("fireconnector", "closing connections");

            //After pushing:
            try {
                if (outputStream != null) {
                    Log.d("fireconnector", "closing os");
                    outputStream.close();
                } else {
                    publishProgress("outputStream closed already ");
                    Log.e("fireconnector", "outputStream closed already ");
                }
                if (inputStream != null) {
                    Log.d("fireconnector", "closing is");
                    inputStream.close();
                } else {
                    Log.e("fireconnector", "inputStream closed already ");
                }
                if (adb != null) {
                    Log.d("fireconnector", "adb waitfor");
                    adb.waitFor();
                    Log.d("fireconnector", "adb waitfor finished");
                } else {
                    publishProgress("adb closed already ");
                    Log.e("fireconnector", "adb closed already ");
                }
            } catch (IOException e) {
                publishProgress("IOException error 2 " + e.toString());
                Log.e("fireconnector", "IOException error 2 " + e);
            } catch (InterruptedException e) {
                publishProgress("InterruptedException error 5 " + e.toString());
                Log.e("fireconnector", "InterruptedException error 5 " + e);
            }
            if (adb != null) {
                adb.destroy();
            } else {
                publishProgress("adb already destroyed ");
                Log.e("fireconnector", "adb already destroyed ");
            }

            //lets call our onProgressUpdate() method which runs on the UI thread
            publishProgress("Installing complete. Thank you!");
            Log.e("fireconnector", "100");
            return null;
        }


        @Override
        protected void onPreExecute() {
            lockScreenOrientation();

            if (notificationDisplay == true) {
                //notificationHelper.createNotification();
            }
            if (!debugDisplay) {
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#ff9900"));
                pDialog.setTitleText("installing on Fire...");
                pDialog.setCancelable(false);
                pDialog.showCancelButton(true);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setCancelText("Destroy Install-Process!");
                pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        cancel(true);
                        sDialog.cancel();
                    }
                });
                pDialog.show();
            }
        }

        protected void onProgressUpdate(String... v) {
            String passedValues = v[0];// + "," + v[1];

            LogToView("fireinstaller", "onProgressUpdate" + " passedValues " + passedValues); //TODO why we used to log v here?
            if (notificationDisplay == true) {
                //notificationHelper.progressUpdate(completed);
            }

            if (!debugDisplay) {
                pDialog.setContentText(passedValues);
            }

        }

        protected void onPostExecute(final Void result) {
            installAPKdirectly = false;
            //this should be self explanatory
            if (notificationDisplay == true) {
                //notificationHelper.completed();
            }
            if (!debugDisplay) {
                pDialog.setCancelable(true);
                pDialog.dismissWithAnimation();
            }
            unlockScreenOrientation();
            if (!debugDisplay) {
                TextView textAbove = (TextView) findViewById(R.id.format_as); //make sure we don't call an empty reference at textAbove
                textAbove.setVisibility(View.VISIBLE);
            }
            LogToView("fireinstaller", "complete. READY?\n");
            AppRate.with(MainActivity.this).checkAndShow();
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

        public void onCancelled() {
            Log.d("onCancelled", "Killing adb server. Installation canceled :(");
            installAPKdirectly = false;
            Log.d("onCancelled", "this should not happen...\n");


            Process adb2 = null;
            try {
                adb2 = Runtime.getRuntime().exec("sh");

            } catch (IOException e1) {
                Log.e("fireconnector", "IOException error e " + e1);
            }

            DataOutputStream outputStream2 = null;

            if (adb2 != null) {
                outputStream2 = new DataOutputStream(adb2.getOutputStream());
            } else {
                Log.e("fireconnector", "adb == null");
            }
            try {
                if (outputStream2 != null) {
                    outputStream2.writeBytes("/system/bin/adb kill-server" + "\n ");
                    outputStream2.flush();
                    Log.d("fireconnector", "/system/bin/adb kill-server" + "\n ");
                }
            } catch (IOException e) {
                Log.e("fireconnector", " IOException error " + e);
            }
            Log.d("onCancelled", "Closing Connections. Installation canceled :(");
            try {
                if (outputStream2 != null) {
                    outputStream2.close();
                } else {
                    Log.e("fireconnector", "outputStream closed already ");
                }
                if (adb2 != null) {
                    adb2.waitFor();
                } else {
                    Log.e("fireconnector", "adb closed already ");
                }
            } catch (IOException e) {
                Log.e("fireconnector", "IOException error 2 " + e);
            } catch (InterruptedException e) {
                Log.e("fireconnector", "InterruptedException error 5 " + e);
            }
            if (adb2 != null) {
                adb2.destroy();
            } else {
                Log.e("fireconnector", "adb already destroyed ");
            }

            cancel(true);
            //TODO stop installertask
            if (!debugDisplay) {
                pDialog.setCancelable(true);
                pDialog.dismissWithAnimation();
            }
            unlockScreenOrientation();
            if (!debugDisplay) {
                TextView textAbove = (TextView) findViewById(R.id.format_as); //make sure we don't call an empty reference at textAbove
                textAbove.setVisibility(View.VISIBLE);
            }
            Log.d("onCancel", "finished");
        }

    }

    public void installAPK(String sourceFile) {
        if (sourceFile.endsWith(".apk")) {
            encPath = sourceFile;
            installAPKdirectly = true;
            dialogBeforeInstall();
        } else {
            Toast.makeText(this, "Please select only files with .apk ending", Toast.LENGTH_LONG).show();
        }

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
                            Log.d("filepickerdebug1", uri.getEncodedPath());
                            installAPK(uri.getEncodedPath());
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            Log.d("filepickerdebug2", uri.getEncodedPath());
                            installAPK(uri.getEncodedPath());
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                Log.d("filepickerdebug3", uri.getEncodedPath());
                installAPK(uri.getEncodedPath());
            }
        }
    }

    public static boolean isValidIPV4(final String s) {
        return IPV4_PATTERN.matcher(s).matches();
    }
}


