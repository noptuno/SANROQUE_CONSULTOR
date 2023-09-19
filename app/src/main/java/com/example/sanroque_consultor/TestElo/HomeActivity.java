package com.example.sanroque_consultor.TestElo;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sanroque_consultor.R;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;

import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends BaseActivity {

    static  AvailableScanner curAvailableScanner=null;

    private static ArrayList<DCSScannerInfo> mSNAPIList=new ArrayList<DCSScannerInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector_test);


        Application.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);

        FindLector();

    }


    static MyAsyncTask cmdExecTask=null;
    private void FindLector() {

        mSNAPIList.clear();
        updateScannersList();
        for(DCSScannerInfo device:getActualScannersList()){
            Log.e("Device", device.getScannerID()+"");
            if(device.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI){
                mSNAPIList.add(device);
            }
        }

        if(mSNAPIList.size() == 0){
            Log.e("mSNAPIList = 0", mSNAPIList.size() + "");
        }else if(mSNAPIList.size() >1){
            // Multiple SNAPI scanners. Show the dialog and navigate to available scanner list.
            Log.e("mSNAPIList > 1", mSNAPIList.size() + "");
        }else {
            // Only one SNAPI scanner available
            if(mSNAPIList.get(0).isActive()){
                Log.e("mSNAPIList = is active", mSNAPIList.size() + "");
               // finish();
            }else{
                // Try to connect available scanner
                cmdExecTask=new MyAsyncTask(mSNAPIList.get(0));
                cmdExecTask.execute();
            }
        }
    }

    private static CustomProgressDialog progressDialog;

    private class MyAsyncTask extends AsyncTask<Void,DCSScannerInfo,Boolean> {
        private DCSScannerInfo  scanner;
        public MyAsyncTask(DCSScannerInfo scn){
            this.scanner=scn;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!isFinishing()) {
                progressDialog = new CustomProgressDialog(HomeActivity.this, "Connecting To scanner. Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result =connect(scanner.getScannerID());
            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS){
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(!isFinishing()) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
            if (!b) {
                Toast.makeText(getApplicationContext(), "Error No hay lector", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
