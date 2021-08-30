package com.dlka.fireinstaller;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.frederico.showtipsview.ShowTipsBuilder;
import net.frederico.showtipsview.ShowTipsView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.nicolaspomepuy.discreetapprate.AppRate;


public class MainActivity extends ListActivity implements
        OnItemSelectedListener, OnItemClickListener {

    public static final String PREFSFILE = "settings2";
    public static final String SELECTED = "selected";
    private static final String mailtag = BuildConfig.VERSION_NAME;
    public String fireip = "";
    public String deviceId = "";
    public String android_id = "";

    public boolean installAPKdirectly = false;
    public String encPath = null;
    static private final String IPV4_REGEX;

    static {
        IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    }

    static private final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    Drawer drawer = null;

    @Override
    protected void onCreate(Bundle b) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        //app drawer
        drawer = new DrawerBuilder()
                .withActionBarDrawerToggle(false)
                .withTranslucentStatusBar(false)
                .withSystemUIHidden(false)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.copy),//0
                        new SecondaryDrawerItem().withName(R.string.externalAPK),//1
                        new DividerDrawerItem(),//2
                        new SecondaryDrawerItem().withName(R.string.mail),//3
                        new SecondaryDrawerItem().withName(R.string.helppage).withDescription(R.string.viewbrowser),//4
                        new SecondaryDrawerItem().withName(R.string.help),//5
                        new SecondaryDrawerItem().withName(R.string.donate),//6
                        new DividerDrawerItem(),//7
                        new PrimaryDrawerItem().withName(R.string.settings).withDescription(R.string.enterIP)//8
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    switch (position) {
                        case 0: {
                            dialogBeforeInstall();
                            break;
                        }
                        case 1: {
                            showFilePicker();
                            break;
                        }
                        case 3: {
                            sendDeveloperMail();
                            break;
                        }
                        case 4: {
                            showWebpageHelp();
                            break;
                        }
                        case 5: {
                            showDialogHelp();
                            break;
                        }
                        case 6: {
                            break;
                        }
                        case 8: {
                            showPreferences();
                            break;
                        }
                    }
                    return true; //@TODO change function -> void
                })
                .withActivity(this).build();
        //floating action button
        findViewById(R.id.multiple_actions).setOnClickListener(v -> drawer.openDrawer());

        ListView listView = getListView();
        listView.setOnItemClickListener(this);

        LinearLayout layout = findViewById(R.id.bannerLayout);
        layout.setVisibility(View.INVISIBLE);


        layout.setPadding(0, 0, 0, 0); //free ad-space for donate version

        //while showing helpdialog we build list in background for ready when user read. //@TODO no helpdialog, so need for loading-dialog here
        setListAdapter(new AppAdapter(this, R.layout.app_item,
                new ArrayList<>(), R.layout.app_item));
        new ListTask(this, R.layout.app_item).execute("");

        SharedPreferences settings = getSharedPreferences(PREFSFILE, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");
        if (!skipMessage.equals("checked"))
            showIntroHelp();
    }

    private void showIntroHelp() {
        final ListView listView = getListView();

        ShowTipsView showtips = new ShowTipsBuilder(this)
                .setTarget(findViewById(R.id.multiple_actions))
                .setTitle("Start here")
                .setDescription("to open drawer. Then open Settings for entering IP-Address (Networkaddress) of FireTV/FireStick.")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips2 = new ShowTipsBuilder(this)
                .setTarget(listView, 100, 150, 200)
                .setTitle("select some apps")
                .setDescription("from all installed apps or use Sideload-Option in Menu.")
                .setDelay(1000)
                .build();

        final ShowTipsView showtips4 = new ShowTipsBuilder(this)
                .setTarget(findViewById(R.id.multiple_actions))
                .setTitle("Searching Help?")
                .setDescription("Select Help or Help-Webpage from Drawer for more information, like where to find installed apps on Fire-Device, how to know IP-Adress and more.\n" +
                        "Hope you enjoy my app. Have Fun!\n\nFeeling adventurous? Try Drawer -> Sideloading, push any apk from your phones sd-card.\n\n")
                .setDelay(1000)
                .build();


        showtips.setCallback(() -> showtips2.show(MainActivity.this));
        showtips2.setCallback(() -> showtips4.show(MainActivity.this));
        showtips4.setCallback(() -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
            editor.putString("skipMessage", "checked");
            // Commit the edits!
            editor.apply();
        });

        showtips.show(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppRate.with(MainActivity.this).checkAndShow();
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();

        ListAdapter adapter = getListAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
            editor.putBoolean(SELECTED + "." + spi.packageName, spi.selected);
        }
        editor.apply();
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
            case R.id.drawer: {
                drawer.openDrawer();
                break;
            }
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

    private void showDialogHelp() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Help Dialog");
        Button button = dialog.findViewById(R.id.Button01);
        final CheckBox dontShowAgain = dialog.findViewById(R.id.checkBox);
        button.setOnClickListener(view -> {
            String checkBoxResult = "NOT checked";
            if (dontShowAgain.isChecked())
                checkBoxResult = "checked";
            SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
            editor.putString("skipMessage", checkBoxResult);
            // Commit the edits!
            editor.apply();
            dialog.dismiss();
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
        String ARCH = detectArch();
        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getAll();
        fireip = (String) preferences.get("example_text");
        String GPS = (String.valueOf(getResources().getInteger(com.google.android.gms.R.integer.google_play_services_version)));

        StringBuilder buffer;
        buffer = new StringBuilder();
        buffer.append("mailto:");
        buffer.append("feedback@kulsch-it.de");
        buffer.append("?subject=");
        buffer.append("Fireinstaller" + mailtag);
        buffer.append("&body=" + deviceId + "\n" + android_id + "\n" + ARCH + "\n" + fireip + "\n" +
                GPS +
                "\n Please provide Androidversion and Device Model for Bugreport if you know it.");
        String uriString = buffer.toString().replace(" ", "%20");

        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
    }

    private void dialogBeforeInstall() {

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        fireip = (String) preferences.get("example_text");
        if (fireip != null && fireip.isEmpty()) {
            Toast.makeText(this, "Target? Fire's IP-Address is empty.", Toast.LENGTH_LONG).show();
            showPreferences();
            return;
        }

        if (fireip != null && fireip.equals("192.0.0.0")) {
            Toast.makeText(this, "Target? Please enter Fire's IP-Address before.", Toast.LENGTH_LONG).show();
            showPreferences();
            return;
        }
        if (!isValidIPV4(fireip)) {
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
                .setConfirmClickListener(sDialog -> {
                    sDialog.cancel();
                    copyMenuSelect();
                })
                .setCancelClickListener(SweetAlertDialog::cancel)
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

        if ((!isNothingSelected()) || (installAPKdirectly)) {
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

    private class LongRunningTask extends AsyncTask<String, String, Void> {
        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        @Override
        protected Void doInBackground(String... params) {

            // lots of code
            publishProgress("Connecting to Fire TV at " + fireip);
            //CONNECTING //should work if we call it only once (singleton making)
            Process adb = null;
            try {
                adb = Runtime.getRuntime().exec("sh");
            } catch (IOException e1) {
                publishProgress("IOException error e" + e1);
            }

            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;
            String output = "";

            if (adb != null) {
                outputStream = new DataOutputStream(adb.getOutputStream());
                inputStream = new DataInputStream(adb.getInputStream());
            } else {
                publishProgress("adb == null");
            }
            try {
                if (outputStream != null) {
                    outputStream.writeBytes("/system/bin/adb" + " connect " + fireip + "\n ");
                    outputStream.flush();

                    int readed;
                    byte[] buff = new byte[4096];
                    while (inputStream.available() <= 0) {
                        Thread.sleep(100); //TODO: does this check every 100ms for output, then continues?

                    }

                    while (inputStream.available() > 0) {
                        readed = inputStream.read(buff);
                        if (readed <= 0) break;
                        output = new String(buff, 0, readed);
                        //result is a string to show in textview
                    }
                    publishProgress("adb_output_connect" + output);

                } else {
                    publishProgress("outputStream == null");
                }

            } catch (IOException | InterruptedException e1) {
                publishProgress("IOException error 1" + e1);
                //connection not successful trying bundled adb
                //1.getting arch (see DebugActivity.java "uname -m")

                //2.using bundled adb
            }


            //TODO: why we parse everything to string and separate it here, can't we access getListAdapter in onBackground?
            ListAdapter adapter = getListAdapter();
            int count = adapter.getCount();


            publishProgress("connection established at " + fireip + ". Preparing apps to push.");
            publishProgress("get packages ready");

            if (installAPKdirectly) {
                if (!(encPath.isEmpty())) {
                    publishProgress("Install external APK. " + encPath + " . \n\n" +
                            " May take long time.\n\n\n" +
                            "If no progress after some Minutes: Check if IP " + fireip + " is correct on your Fire TV. " +
                            "If it's wrong, just (force close and) restart this app. " +
                            "Then open Settings and enter correct IP (see menu: help for more).\n" +
                            "When this window disappears everything is installed. Thank you for using my app!");
                    try {
                        //move apk to fire tv here
                        //Foreach Entry do and show progress thing:

                        if (outputStream != null) {
                            outputStream.writeBytes("/system/bin/adb install -g " + encPath + "\n");
                            outputStream.flush();

                            int readed;
                            byte[] buff = new byte[4096];

                            while (inputStream.available() > 0) {
                                readed = inputStream.read(buff);
                                if (readed <= 0) break;
                                output = new String(buff, 0, readed);
                                //result is a string to show in textview
                            }
                            publishProgress("adb_output" + output);
                        } else {
                            publishProgress("outputStream == null (occurence 2)");
                        }
                    } catch (IOException e) {
                        publishProgress(" IOException error " + e);
                    }
                }
            } else {
                for (int i = 0; i < count; i++) {
                    SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
                    if (spi.selected) {
                        publishProgress("Install:\"" +
                                spi.displayName + "\"  (app number " + (i + 1) + ") from " +
                                spi.sourceDir + " . \n\n" +
                                " May take long time.\n\n\n" +
                                "If no progress after some Minutes: Check if IP " + fireip +
                                " is correct on your Fire TV. " +
                                "If it's wrong, just (force close and) restart this app. " +
                                "Then open Settings and enter correct IP (see menu: help for more).\n" +
                                "When this window disappears everything is installed. Thank you for using my app!");
                        try {
                            //move apk to fire tv here
                            //Foreach Entry do and show progress thing:

                            if (outputStream != null) {
                                //add -g, ‚grant permissions on non-interactive install
                                outputStream.writeBytes("/system/bin/adb install -g -r " + spi.sourceDir + "\n");
                                outputStream.flush();

                                int readed;
                                byte[] buff = new byte[4096];


                                while (inputStream.available() > 0) {
                                    readed = inputStream.read(buff);
                                    if (readed <= 0) break;
                                    output = new String(buff, 0, readed);
                                    //result is a string to show in textview
                                }
                                publishProgress("adb_output" + output);
                            } else {
                                publishProgress("outputStream == null (occurence 2)");
                            }
                        } catch (IOException e) {
                            publishProgress(" IOException error " + e);
                        }
                    }
                }
                try {
                    if (outputStream != null) {
                        outputStream.writeBytes("exit\n");
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    publishProgress(" IOException error " + e);
                }
            }//end for loop


            //CLOSINGCONNECTION //should work

            //TODO better logging with errorcontent too
            publishProgress("Please waiting... installation running... When finished this Dialog disappears");

            //After pushing:
            try {
                if (outputStream != null) {
                    outputStream.close();
                } else {
                    publishProgress("outputStream closed already ");
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (adb != null) {
                    adb.waitFor();
                } else {
                    publishProgress("adb closed already ");
                }
            } catch (IOException e) {
                publishProgress("IOException error 2 " + e);
            } catch (InterruptedException e) {
                publishProgress("InterruptedException error 5 " + e);
            }
            if (adb != null) {
                adb.destroy();
            } else {
                publishProgress("adb already destroyed ");
            }

            //lets call our onProgressUpdate() method which runs on the UI thread
            publishProgress("Closing Connections. Installation complete. Thank you!");
            return null;
        }


        @Override
        protected void onPreExecute() {
            lockScreenOrientation();

            pDialog.getProgressHelper().setBarColor(Color.parseColor("#ff9900"));
            pDialog.setTitleText("installing on Fire...");
            pDialog.setCancelable(false);
            pDialog.showCancelButton(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setCancelText("Destroy Install-Process!");
            pDialog.setCancelClickListener(sDialog -> {
                cancel(true);
                sDialog.cancel();
            });
            pDialog.show();

        }

        protected void onProgressUpdate(String... v) {
            String passedValues = v[0];// + "," + v[1];
            pDialog.setContentText(passedValues);
        }

        protected void onPostExecute(final Void result) {
            installAPKdirectly = false;
            //this should be self explanatory
            pDialog.setCancelable(true);
            pDialog.dismissWithAnimation();
            unlockScreenOrientation();
            AppRate.with(MainActivity.this).checkAndShow();
        }

        @SuppressLint("SourceLockedOrientationActivity")
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

        public void onCancelled() {
            installAPKdirectly = false;
            Process adb2 = null;
            try {
                adb2 = Runtime.getRuntime().exec("sh");

            } catch (IOException e1) {
            }

            DataOutputStream outputStream2 = null;

            if (adb2 != null) {
                outputStream2 = new DataOutputStream(adb2.getOutputStream());
            }
            try {
                if (outputStream2 != null) {
                    outputStream2.writeBytes("/system/bin/adb kill-server" + "\n ");
                    outputStream2.flush();
                }
            } catch (IOException e) {
            }
            try {
                if (outputStream2 != null) {
                    outputStream2.close();
                }
                if (adb2 != null) {
                    adb2.waitFor();
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
            if (adb2 != null) {
                adb2.destroy();
            }

            cancel(true);
            //TODO stop installertask
            pDialog.setCancelable(true);
            pDialog.dismissWithAnimation();
            unlockScreenOrientation();
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
                            installAPK(uri.getEncodedPath());
                        }
                    }
                }
            } else {
                Uri uri = data.getData();
                installAPK(uri.getEncodedPath());
            }
        }
    }

    public static boolean isValidIPV4(final String s) {
        return IPV4_PATTERN.matcher(s).matches();
    }

    public void showWebpageHelp() {
        String url = "https://kulsch-it.blogspot.de/2015/10/sideloading-firetvfirestick.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public String detectArch() {
        Process detect = null;
        try {
            detect = Runtime.getRuntime().exec("sh");

        } catch (IOException e1) {
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

            } else {
            }
        } catch (IOException e1) {
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
        } catch (InterruptedException e) {
        }
        if (detect != null) {
            detect.destroy();
        }
        return output;
    }
}


