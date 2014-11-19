package com.dlka.fireinstaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

public class MainActivity extends ListActivity implements
		OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

	private TemplateSource templateSource;
	private TemplateData template;

	public static final String PREFSFILE = "settings";
	private static final String ALWAYS_GOOGLE_PLAY = "always_link_to_google_play";
	private static final String TEMPLATEID = "templateid";
	public static final String SELECTED = "selected";
	private static final String APP_TAG = "com.dlka.fireinstaller";
	private static final String PROPERTY_ID = "App";
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
      }
    private AdView adView;
    

      HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
      

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
		//AppRater.appLaunched(this);
		  // Get tracker.
        Tracker t = (getTracker(
            TrackerName.APP_TRACKER));

        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("MainView");

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
        
        
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker t1 = analytics.newTracker(R.xml.global_tracker);
        t1.send(new HitBuilders.AppViewBuilder().build());
        
        // Create the adView.
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-8761501900041217/7037219683");
        adView.setAdSize(AdSize.BANNER);

        // Lookup your LinearLayout assuming it's been given
        // the attribute android:id="@+id/mainLayout".
        LinearLayout layout = (LinearLayout)findViewById(R.id.bannerLayout);

        // Add the adView to it.
        layout.addView(adView);

        // Initiate a generic request.
       // AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
        .addTestDevice("89CADD0B4B609A30ABDCB7ED4E90A8DE")
        .addTestDevice("CCCBB7E354C2E6E64DB5A399A77298ED") 
        .build();
        
        // Load the adView with the ad request.
        adView.loadAd(adRequest);

        
        
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
		//CheckBox checkbox = (CheckBox) findViewById(R.id.always_gplay);
	//	Spinner spinner = (Spinner) findViewById(R.id.format_select);
		templateSource = new TemplateSource(this);
		templateSource.open();
		List<TemplateData> formats = templateSource.list();
		ArrayAdapter<TemplateData> adapter = new ArrayAdapter<TemplateData>(this,
				android.R.layout.simple_spinner_item, formats);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//spinner.setAdapter(adapter);
		//spinner.setOnItemSelectedListener(this);
		SharedPreferences prefs = getSharedPreferences(PREFSFILE, 0);
		//checkbox.setChecked(prefs.getBoolean((ALWAYS_GOOGLE_PLAY), true));
		//checkbox.setChecked(true);
		int selection = 0;
		Iterator<TemplateData> it = formats.iterator();
		int count = 0;
		while (it.hasNext()) {
			template = it.next();
			if (template.id == prefs.getLong(TEMPLATEID, 0)) {
				selection = count;
				break;
			}
			template = null;
			count++;
		}
		//spinner.setSelection(selection);
		setListAdapter(new AppAdapter(this, R.layout.app_item,
				new ArrayList<SortablePackageInfo>(), R.layout.app_item));
		new ListTask(this, R.layout.app_item).execute("");
		
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
		SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
		editor.putBoolean(ALWAYS_GOOGLE_PLAY,true);
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
	protected void onStart()
	{
	super.onStart();
		
	}
	@Override
	protected void onStop()
	{
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
				
				if (!isNothingSelected()) {
					CharSequence buf = buildOutput();

					//String value =
					       // ((EditText) getView().findViewById(R.id.username)).getText().toString().trim();

					try {
						String qry=buf.toString(); // TODO: Query username from user and save (prefs? sqlite?) 
						String result=SendPost(qry);
						
						Log.d(APP_TAG, "qry");
						Log.d(APP_TAG, qry);
						Log.d(APP_TAG, result);
						

							Toast.makeText(this, "App pushed at FireTV", Toast.LENGTH_LONG).show();
						
					
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				else{;}
				Toast.makeText(this, "no app selected", Toast.LENGTH_LONG).show();
				
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
				//Uri uri = Uri.parse(getString(R.string.url_help)); MainActivity.openUri(this,uri);
				return true;
			} 
			case (R.id.item_mail):{
				StringBuffer buffer = new StringBuffer();
			    buffer.append("mailto:");
			    buffer.append("feedback@kulsch-it.de");
			    buffer.append("?subject=");
			    buffer.append("Fireinstaller");
			    buffer.append("&body=");
			    String uriString = buffer.toString().replace(" ", "%20");

			    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
			}
		}
		return true;
	}
	
	/**
	 * Share with the world.
	 */
	private void doStumble() {
		ListAdapter adapter = getListAdapter();
		int count = adapter.getCount();
		ArrayList<String> collect = new ArrayList<String>(); 
		for (int i = 0; i < count; i++) {
			SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
			if (spi.selected) {
				collect.add(spi.packageName);
			}
		}
		
		Collections.shuffle(collect);
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<collect.size();i++) {
			if (sb.length()>0) {
				sb.append(",");
			}
			sb.append(collect.get(i));
			if (sb.length()>700) { //fixme try increase 200->700
				Toast.makeText(this, "Select less apps!", Toast.LENGTH_LONG).show();		
				break; // prevent the url from growing overly large. 
			}
		}
		//TODO apk push
		Log.d("Fireinstaller","apk push called for" + sb.toString());
	}

	/**
	 * Construct what is to be shared/copied to the php parser
	 * 
	 * @return the html response
	 */
	private CharSequence buildOutput() {

		StringBuilder ret = new StringBuilder();
		ListAdapter adapter = getListAdapter();
		int count = adapter.getCount();

		int selected = 0;

		for (int i = 0; i < count; i++) {
			SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
			if (spi.selected) {
				selected++;
				Log.d("Fireinstaller", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);

				ret.append(spi.packageName);
				ret.append(":::");
				ret.append(spi.sourceDir);
				ret.append("::::");
				
			}
		}
		return ret;
	}
	public static String noNull(String input) {
		if (input == null) {
			return "";
		}
		return input;
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


	public static String createSourceLink(String installer, String packname) {
		return "https://www.google.com/search?q=" + packname;
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
		
		 PackageManager pm = getPackageManager(); 
		for (ApplicationInfo app : pm.getInstalledApplications(0)) {
   
	//Log.d("Fireinstaller", "package: " + app.packageName + ", sourceDir: " + app.sourceDir);

	
	Log.d("Fireinstaller", "package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);

		}
		return true;
	}
	
	/**
	 * Open an url in a webbrowser
	 * 
	 * @param ctx
	 *          a context
	 * @param uri
	 *          target
	 */
	public static void openUri(Context ctx, Uri uri) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			ctx.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
			// There are actually people who don't have a webbrowser installed
			//Toast.makeText(ctx, com.dlka.fireinstaller.R.string.msg_no_webbrowser, Toast.LENGTH_SHORT).show();
		}
	}
	      
	       
	        public String SendPost(String data) throws IOException   {
	            
		Log.d("Fireinstaller", "pushing to device");

	            return "success";
	        }
	        
	        /**
	         * Enum used to identify the tracker that needs to be used for tracking.
	         *
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
			  
	
}
