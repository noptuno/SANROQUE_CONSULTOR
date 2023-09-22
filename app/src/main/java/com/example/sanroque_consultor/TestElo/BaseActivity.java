package com.example.sanroque_consultor.TestElo;

import static com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_BT_NORMAL;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sanroque_consultor.R;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements ScannerAppEngine,IDcsSdkApiDelegate {

    private static ArrayList<DCSScannerInfo> mScannerInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mScannerInfoList = Application.mScannerInfoList;

        if (Application.sdkHandler == null) {
            Application.sdkHandler = new SDKHandler(this, true);
        }
        Application.sdkHandler.dcssdkSetDelegate(this);
        initializeDcsSdkWithAppSettings();

        int notifications_mask = 0;
        notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value);
        Application.sdkHandler.dcssdkSubsribeForEvents(notifications_mask);

    }


    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {

    }

    @Override
    public void dcssdkEventScannerDisappeared(int i) {

    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {
        dataHandler.obtainMessage(Constants.SESSION_ESTABLISHED, dcsScannerInfo).sendToTarget();
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int i) {

    }

    @Override
    public void dcssdkEventBarcode(byte[] bytes, int i, int i1) {

    }

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {

    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {

    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {

    }

    @Override
    public void initializeDcsSdkWithAppSettings() {
        int notifications_mask = 0;
        notifications_mask |= (DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value);
        Application.sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
    }

    @Override
    public void showMessageBox(String message) {

        Log.e("showMessageBox","showMessageBox");

    }

    @Override
    public int showBackgroundNotification(String text) {
        return 0;
    }

    @Override
    public int dismissBackgroundNotifications() {
        return 0;
    }

    @Override
    public boolean isInBackgroundMode(Context context) {
        return false;
    }

    @Override
    public void addDevListDelegate(IScannerAppEngineDevListDelegate delegate) {

    }


    private static ArrayList<IScannerAppEngineDevConnectionsDelegate> mDevConnDelegates = new ArrayList<IScannerAppEngineDevConnectionsDelegate>();
    @Override
    public void addDevConnectionsDelegate(IScannerAppEngineDevConnectionsDelegate delegate) {
        if (mDevConnDelegates == null)
            mDevConnDelegates = new ArrayList<IScannerAppEngineDevConnectionsDelegate>();
        mDevConnDelegates.add(delegate);
    }

    @Override
    public void addDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate) {

    }

    @Override
    public void removeDevListDelegate(IScannerAppEngineDevListDelegate delegate) {

    }

    @Override
    public void removeDevConnectiosDelegate(IScannerAppEngineDevConnectionsDelegate delegate) {

    }

    @Override
    public void removeDevEventsDelegate(IScannerAppEngineDevEventsDelegate delegate) {

    }

    @Override
    public List<DCSScannerInfo> getActualScannersList() {
        return mScannerInfoList;
    }

    @Override
    public DCSScannerInfo getScannerInfoByIdx(int dev_index) {
        return null;
    }

    @Override
    public DCSScannerInfo getScannerByID(int scannerId) {
        return null;
    }

    @Override
    public void raiseDeviceNotificationsIfNeeded() {
        Log.e("raiseDeviceNotificationsIfNeeded","raiseDeviceNotificationsIfNeeded");
    }

    @Override
    public void updateScannersList() {

        if (Application.sdkHandler != null) {
            Log.e("Entro","si");
            mScannerInfoList.clear();
            ArrayList<DCSScannerInfo> scannerTreeList = new ArrayList<DCSScannerInfo>();
            Application.sdkHandler.dcssdkGetAvailableScannersList(scannerTreeList);
            Application.sdkHandler.dcssdkGetActiveScannersList(scannerTreeList);
           createFlatScannerList(scannerTreeList);
        }
    }
    private void createFlatScannerList(ArrayList<DCSScannerInfo> scannerTreeList) {
        for (DCSScannerInfo s :
                scannerTreeList) {
            addToScannerList(s);
        }
    }
    private void addToScannerList(DCSScannerInfo s) {
        mScannerInfoList.add(s);
        if (s.getAuxiliaryScanners() != null) {
            for (DCSScannerInfo aux :
                    s.getAuxiliaryScanners().values()) {
                addToScannerList(aux);
            }
        }
    }

    @Override
    public DCSSDKDefs.DCSSDK_RESULT connect(int scannerId) {
        if (Application.sdkHandler != null) {
            Application.intentionallyDisconnected = false;
               // Application.sdkHandler.dcssdkTerminateCommunicationSession(HomeActivity.curAvailableScanner.getScannerId());
            return Application.sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
        } else {
            return DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
        }
    }

    @Override
    public void disconnect(int scannerId) {

    }

    @Override
    public DCSSDKDefs.DCSSDK_RESULT setAutoReconnectOption(int scannerId, boolean enable) {
        return null;
    }

    @Override
    public void enableScannersDetection(boolean enable) {

    }

    @Override
    public void enableBluetoothScannerDiscovery(boolean enable) {

    }

    @Override
    public void configureNotificationAvailable(boolean enable) {

    }

    @Override
    public void configureNotificationActive(boolean enable) {

    }

    @Override
    public void configureNotificationBarcode(boolean enable) {

    }

    @Override
    public void configureNotificationImage(boolean enable) {

    }

    @Override
    public void configureNotificationVideo(boolean enable) {

    }

    @Override
    public void configureOperationalMode(DCSSDKDefs.DCSSDK_MODE mode) {

    }

    @Override
    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        return false;
    }

    @Override
    public boolean executeSSICommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        return false;
    }
    private static ArrayList<IScannerAppEngineDevEventsDelegate> mDevEventsDelegates = new ArrayList<IScannerAppEngineDevEventsDelegate>();
    ;

    protected Handler dataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case Constants.BARCODE_RECEIVED:

                    Log.e("Recibiendo", "Barcode Received");
                    Barcode barcode = (Barcode) msg.obj;
                    Application.barcodeData.add(barcode);
                    for (IScannerAppEngineDevEventsDelegate delegate : mDevEventsDelegates) {
                        if (delegate != null) {
                            delegate.scannerBarcodeEvent(barcode.getBarcodeData(), barcode.getBarcodeType(), barcode.getFromScannerID());
                        }
                    }

                    break;
            }
        }


    };
}
