package com.dlka.fireinstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

	public String fireip="192.168.1.106";    
	
	private final String mailtag="0.5";

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

        LinearLayout layout = (LinearLayout)findViewById(R.id.bannerLayout);

        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       
        .addTestDevice("89CADD0B4B609A30ABDCB7ED4E90A8DE")
        .addTestDevice("CCCBB7E354C2E6E64DB5A399A77298ED")  //current Nexus 4
        .build();
        
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
		templateSource = new TemplateSource(this);
		templateSource.open();
		List<TemplateData> formats = templateSource.list();
		ArrayAdapter<TemplateData> adapter = new ArrayAdapter<TemplateData>(this,
				android.R.layout.simple_spinner_item, formats);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SharedPreferences prefs = getSharedPreferences(PREFSFILE, 0);
		//int selection = 0;
		Iterator<TemplateData> it = formats.iterator();
		//int count = 0;
		while (it.hasNext()) {
			template = it.next();
			if (template.id == prefs.getLong(TEMPLATEID, 0)) {
			//	selection = count;
				break;
			}
			template = null;
			//count++;
		}
		setListAdapter(new AppAdapter(this, R.layout.app_item,
				new ArrayList<SortablePackageInfo>(), R.layout.app_item));
		new ListTask(this, R.layout.app_item).execute("");
		
		//when button pressed: copyMenuSelect();

	}

	
	
	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
		SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
		editor.putBoolean(ALWAYS_GOOGLE_PLAY,true);
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
	public boolean dispatchTouchEvent(MotionEvent event) {

	    View v = getCurrentFocus();
	    boolean ret = super.dispatchTouchEvent(event);

	    if (v instanceof EditText) {
	        View w = getCurrentFocus();
	        int scrcoords[] = new int[2];
	        w.getLocationOnScreen(scrcoords);
	        float x = event.getRawX() + w.getLeft() - scrcoords[0];
	        float y = event.getRawY() + w.getTop() - scrcoords[1];

	        Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
	        if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

	            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
	        }
	    }
	return ret;
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
				//TODO implement help screen

				Toast.makeText(this, "Isn't implemented yet.", Toast.LENGTH_LONG).show();
			
				return true;
			} 
			case (R.id.item_mail):{
				StringBuffer buffer = new StringBuffer();
			    buffer.append("mailto:");
			    buffer.append("feedback@kulsch-it.de");
			    buffer.append("?subject=");
			    buffer.append("Fireinstaller"+mailtag);
			    buffer.append("&body=Please provide Androidversion and Device Model for Bugreport if you know it.");
			    String uriString = buffer.toString().replace(" ", "%20");

			    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
			}
		}
		return true;
	}
	
	private void copyMenuSelect() {
		
		if (!isNothingSelected()) {
			CharSequence buf = buildOutput();

		fireip =
			        ((EditText) findViewById(R.id.editText1)).getText().toString().trim();

			        Log.d("Fireinstaller","IP ausgelesen:"+fireip);

			try {
				String qry=buf.toString(); // TODO: Save IP into prefs or file 
				String result=PushApk(qry);
				
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

		
	//	 PackageManager pm = getPackageManager(); 
	//	for (ApplicationInfo app : pm.getInstalledApplications(0)) {
	//		Log.d("Fireinstaller2", "package: " + app.packageName + ", sourceDir: " + app.sourceDir);
	//	}

		for (int i = 0; i < count; i++) {
			SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
			if (spi.selected) {
				Log.d("Fireinstaller2", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);
				
				//ret.append(spi.packageName);
				//ret.append(":::");
				//ret.append(spi.sourceDir);
				//ret.append("::::");
				ret.append(i+" ");
				
				        File file =new File(spi.sourceDir);
				        InputStream myInput;
				        Log.d("Fireinstaller2", "filesrc " +file.toString());
					
				        String sdpath = Environment.getExternalStorageDirectory().getPath();
				        try {
				        	
				        	// Set the output folder on the Scard
				            File directory = new File(sdpath + "/fireinstaller");
				            // Create the folder if it doesn't exist:
				            if (!directory.exists()) {
				            directory.mkdirs();
				            }
				            
				        	
				        	File exist=new File(Environment.getExternalStorageDirectory()+"/fireinstaller/temp.apk");
				            Log.d("Fireinstaller2", "filetargt " +exist.toString());
					        Log.d("Fireinstaller2", "1 "+ exist.exists());
						    // Set the output file stream up:
				            myInput = new FileInputStream(file.toString());
				            OutputStream myOutput = new FileOutputStream(exist.toString());
				            // Transfer bytes from the input file to the output file
				            byte[] buffer = new byte[1024];
				            int length;
				            while ((length = myInput.read(buffer)) > 0) {
				            myOutput.write(buffer, 0, length);
				            }
				            // Close and clear the streams
				            myOutput.flush();
				            myOutput.close();
				            myInput.close();
				            Toast.makeText(MainActivity.this, "temp.apk created", Toast.LENGTH_LONG)
				            .show();
				            } catch (IOException e) {
				            // TODO Auto-generated catch block
				            e.printStackTrace();
				            }
				    
			//move apk to fire tv here
			
		}}
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
		// AppAdapter aa = (AppAdapter) getListAdapter();
		// SortablePackageInfo spi = aa.getItem(position);
		// PackageManager pm = getPackageManager(); 
		// for (ApplicationInfo app : pm.getInstalledApplications(0)) {
		// Log.d("Fireinstaller", "package: " + app.packageName + ", sourceDir: " + app.sourceDir);

			
			
			
   
	//Log.d("Fireinstaller", "package: " + spi.packageName + ", sourceDir: " + spi.sourceDir + ", ip:"+fireip);

		//}
		return true;
	}
	

	public static void openUri(Context ctx, Uri uri) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			ctx.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
			
			Toast.makeText(ctx, "no webbrowser found", Toast.LENGTH_SHORT).show();
		}
	}
	      
	       
	        public String PushApk(String data) throws IOException   {
	            
		Log.d("Fireinstaller", "pushing to device "+fireip);

//TODO pushing to firetv
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
