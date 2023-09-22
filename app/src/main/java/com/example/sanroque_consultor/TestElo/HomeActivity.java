package com.example.sanroque_consultor.TestElo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.sanroque_consultor.R;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    static  AvailableScanner curAvailableScanner=null;

    private static ArrayList<DCSScannerInfo> mSNAPIList=new ArrayList<DCSScannerInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector_test);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbPermissionReceiver, filter);
        mandarSolicitud();
        FindLector();
    }

    private void mandarSolicitud(){
        Application.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
    }

    private BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e("recibidousb",action);
            synchronized(this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                if (intent.getBooleanExtra("permission", false)) {
                    if (device != null) {
                        Log.e("usb", "acepto");

                        FindLector();
                    }
                } else {
                    Log.e("usb", "cancelo");
                    mandarSolicitud();
                }
            }
        }
    };

    @Override
    public void dcssdkEventBarcode(byte[] bytes, int i, int i1) {

        String barcodeText = new String(bytes);
        Log.e("aca2",barcodeText);

    }
    private DCSScannerInfo  scanner;
    static MyAsyncTask cmdExecTask=null;

    private static ArrayList<IScannerAppEngineDevConnectionsDelegate> mDevConnDelegates = new ArrayList<IScannerAppEngineDevConnectionsDelegate>();

    private void FindLector() {

        updateScannersList();
        for(DCSScannerInfo device:getActualScannersList()){

            if(device.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI){
                scanner = device;
            }

        }

        if (scanner!=null){
            cmdExecTask=new MyAsyncTask(scanner);
            cmdExecTask.execute();
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
