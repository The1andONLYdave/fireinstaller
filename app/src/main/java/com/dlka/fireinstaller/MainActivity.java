package com.dlka.fireinstaller;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.telly.groundy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends ListActivity implements
        OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

    public static final String PREFSFILE = "settings";
    public static final String SELECTED = "selected";
    private static final String ALWAYS_GOOGLE_PLAY = "always_link_to_google_play";
    private static final String TEMPLATEID = "templateid";
    private static final String PROPERTY_ID = "App";
    private final String mailtag = "0.8";
    public String fireip = "";
    public SharedPreferences prefs2;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    private TemplateSource templateSource;
    private TemplateData template;
    private AdView adView;

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

        final Dialog dialog = new Dialog(this);
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

        SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(this);

        // prefs2.registerOnSharedPreferenceChangeListener();

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
        templateSource = new TemplateSource(this);
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
        editor.putBoolean(ALWAYS_GOOGLE_PLAY, true);
        //TODO editor.sharedprefs for saving/reading ip, make default ip go there if empty first
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
            case (R.id.select_all): {
                ListAdapter adapter = getListAdapter();
                int count = adapter.getCount();
                for (int i = 0; i < count; i++) {
                    SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
                    spi.selected = true;
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
                StringBuffer buffer = new StringBuffer();
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

                Toast.makeText(this, "Isn't implemented yet. But i'd be happy if you just buy any of my apps @ google play", Toast.LENGTH_LONG).show();

                break;
            }
            case (R.id.item_settings): {
                //TODO implement settings screen for setting ip, checkbox for auto-connect adb on app-startup, checkbox disable ads..
                showPreferences();
                break;
            }
        }
        return true;
    }

    private void showPreferences() {
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    @SuppressWarnings("unchecked")
    private void copyMenuSelect() {

        if (!isNothingSelected()) {


            Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();

            fireip =(String)preferences.get("example_text");

          //  fireip =(String)preferences.get("example_text", "192.168.0.1");
            Toast.makeText(this, "Installing at IP" + fireip, Toast.LENGTH_LONG).show();
            Log.d("Fireinstaller", "IP ausgelesen:" + fireip);

            //before we call FireConnector we need ip, packages, and maybe even more
            pushFireTv();//TODO make background/async task


        } else {
            Toast.makeText(this, "no app selected", Toast.LENGTH_LONG).show();
        }
    }


    private void pushFireTv() {

//		StringBuilder ret = new StringBuilder();

        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();
        int counter = 0;
        //String[] packagenameVector=null;
        //String[] packageVectorDir=null;
        String dirs=null;

        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            if (spi.selected) {
                Log.d("fireconnector", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);
              //  packagenameVector[counter] = spi.packageName;
              //  packageVectorDir[counter] = spi.sourceDir;
                dirs=dirs+":::"+spi.sourceDir;
                counter++;//how much packages to push
            }
        }

            // this is usually performed from within an Activity
            Groundy.create(FireConnector.class)
                    //  .callback(this)        // required if you want to get notified of your task lifecycle
                    .arg("fireip", fireip)       // optional
                    .arg("counter", count)
        //            .arg("dirs", packageVectorDir)
          //          .arg("names", packagenameVector)
                    .arg("dirs", dirs)
                    .queueUsing(MainActivity.this);


            //return ret;
            return;

    }

   // @OnSuccess(FireConnector.class)
   // public void onSuccess(@Param("the_result") String result) {
   //     // do something with the result
   //     //TODO make notification and maybe some if you like please donate?
   // }

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


}

