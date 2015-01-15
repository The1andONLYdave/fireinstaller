package com.dlka.fireinstaller;

import android.util.Log;

import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Created by David-Lee on 15.01.2015.
 */
public class FireConnectorAsync extends GroundyTask {
    @Override
    protected TaskResult doInBackground() {
        Log.d("fireconnector","doInBackground starting");
        // you can send parameters to the task using a Bundle (optional)
        String fireip = getStringArg("fireip");
        Integer counter=getIntArg("counter");
        //working with big strings
        String directory=getStringArg("dirs", "no-default-value");


        // lots of code
        updateProgress(0);
        //CONNECTING //should work if we call it only once (singleton making)
        Log.d("fireconnector", "connecting adb to " + fireip);
        Process adb = null;
        try {
            adb = Runtime.getRuntime().exec("sh");

        } catch (IOException e1) {
            Log.e("fireconnector", "error");
        }

        DataOutputStream outputStream = new DataOutputStream(adb.getOutputStream());
        try {
            outputStream.writeBytes("/system/bin/adb" + " connect " + fireip + "\n ");
            outputStream.flush();
            Log.d("fireconnector", "/system/bin/adb" + " connect " + fireip + "\n ");

        } catch (IOException e1) {
            Log.e("fireconnector", "error");
        }


        //INSTALLING //maybe work
        String dir = directory.substring(3);
        updateProgress(1);

        if(!dir.contains(":::")){return succeeded().add("the_result","cant split string");}
        String[] sourceDir = dir.split(":::");
        updateProgress(2);

        for (int i = 0; i < counter; i++) {
            //first dirs -- first 3 :
            updateProgress(i + 3);
            //then split as often as ValueOf counter by stripping first chars till :::


                try {
                    //move apk to fire tv here
                    //Foreach Entry do and show progress thing:
                    Log.d("fireconnector", "/system/bin/adb install " + sourceDir[i] + "\n");

                    outputStream.writeBytes("/system/bin/adb install " + sourceDir[i] + "\n");

                    outputStream.flush();


                } catch (IOException e) {
                    Log.e("fireconnector", "error");
                }


            } //end for loop






        //CLOSINGCONNECTION //should work

        //TODO better logging with errorcontent too

        //After pushing:
        try {
            outputStream.close();
            adb.waitFor();
        } catch (IOException e) {
            Log.e("fireconnector", "error");
        } catch (InterruptedException e) {
            Log.e("fireconnector", "error");
        }
        adb.destroy();




        // return a TaskResult depending on the success of your task
        // and optionally pass some results back
        return succeeded().add("the_result", "some result");
    }







}