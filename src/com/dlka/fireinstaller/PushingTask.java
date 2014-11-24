package com.dlka.fireinstaller;

import java.io.DataOutputStream;

import android.content.Context;
import android.os.AsyncTask;


	public class PushingTask extends AsyncTask {
	    private NotificationHelper mNotificationHelper;
	  
		public void DownloadTask(Context context){
	        mNotificationHelper = new NotificationHelper(context);
	    }
	 
	    protected void onPreExecute(){
	        //Create the notification in the statusbar
	        mNotificationHelper.createNotification();
	    }
	 
	    protected void onProgressUpdate(Integer... progress) {
	        //This method runs on the UI thread, it receives progress updates
	        //from the background thread and publishes them to the status bar
	        mNotificationHelper.progressUpdate(progress[0]);
	    }
	    protected void onPostExecute(Void result)    {
	        //The task is complete, tell the status bar about it
	        mNotificationHelper.completed();
	    }

		public void execute() {
			// TODO Auto-generated method stub
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			return null;
		}

	
		
	}