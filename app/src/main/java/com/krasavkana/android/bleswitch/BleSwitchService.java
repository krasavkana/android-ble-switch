package com.krasavkana.android.bleswitch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;
import java.util.Vector;

import static java.lang.Thread.*;

//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

public class BleSwitchService extends Service {

    private final static String TAG = "BleSwitchService";

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private String mBleDeviceName;
    private String mBleDeviceAddress;
    private String mBleServiceUuid;
    private String mBleScanMode;
//    private String mBleUuid = "b3b36901-50d3-4044-808d-50835b13a6cd";
//    private String mBleAddress = "F2:82:B4:89:B0:A3";


    private final static String SCAN_LOW_LATENCY = "Latency";
    private final static String SCAN_LOW_POWER   = "Power";
    private final static String SCAN_BALANCED    = "Balance";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand()");

        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);

        mBleDeviceName = intent.getStringExtra("BLE_DEVICE_NAME");
        Log.d(TAG, "mBleDeviceName: " + mBleDeviceName);
        mBleServiceUuid = intent.getStringExtra("BLE_SERVICE_UUID");
        Log.d(TAG, "mBleServiceUuid: " + mBleServiceUuid);
        mBleDeviceAddress = intent.getStringExtra("BLE_DEVICE_ADDRESS");
        Log.d(TAG, "mBleDeviceAddress: " + mBleDeviceAddress);
        mBleScanMode = intent.getStringExtra("BLE_SCAN_MODE");
        Log.d(TAG, "mBleScanMode: " + mBleScanMode);
        Context context = getApplicationContext();
        String channelId = "default";
        String title = "Ble Switches That Control Smartphone";

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if(Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //     Notification　Channel 設定
            NotificationChannel channel = new NotificationChannel(
                    channelId, title, NotificationManager.IMPORTANCE_DEFAULT);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);

                Notification notification = new Notification.Builder(context, channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("Start Scanning Ble Switches")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();

                // startForeground
                startForeground(1, notification);
            }
        }

        // MainActivityによりデービスや権限等の存在が確認されている想定。
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager == null) { stopSelf(); }
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if (btAdapter == null) { stopSelf(); } else if (!btAdapter.isEnabled()){ stopSelf(); }

        startScanning();

        //return START_NOT_STICKY;
        return START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            Log.d(TAG, "rssi: " + result.getRssi());
            Log.d(TAG, "describeContents: " + result.describeContents());

            stopScanning();
            // this line for PochiruEco device
            result.getDevice().connectGatt(getApplicationContext(), false, leGattCallback);

            Log.d(TAG, "Service send ble cmd to remote-app via intent mechanism");
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.krasavkana.android.decoycamera", "com.krasavkana.android.decoycamera.CameraActivity");
            intent.putExtra("bleCommand", "Shoot!Shoot!");
            startActivity(intent);

            startScanning();
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed()");
        }
    };

    // Device gatt callback.
    private BluetoothGattCallback leGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            Log.d(TAG, "Bluetooth GATT Status: " + status);
//       }
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Bluetooth GATT Status: " + status);
            switch(newState){
                case BluetoothGatt.STATE_CONNECTED:
                    Log.d(TAG, "Bluetooth GATT State: CONNECTED");
                    gatt.disconnect();
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    Log.d(TAG, "Bluetooth GATT State: DISCONNECTED");
                    gatt.close();
                    break;
                default:
                    Log.d(TAG, "Bluetooth GATT State: " + newState);
                    break;
            }
        }
    };

    public void startScanning() {
        Log.d(TAG, "startScanning()");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ScanFilter.Builder builder = new ScanFilter.Builder();
                if (mBleDeviceName != null) {
                    builder.setDeviceName(mBleDeviceName);
                }
                if (mBleServiceUuid != null) {
                    builder.setServiceUuid(new ParcelUuid(UUID.fromString(mBleServiceUuid)));
                }
                if (mBleDeviceAddress != null) {
                    builder.setDeviceAddress(mBleDeviceAddress);
                }
                Vector<ScanFilter> filter = new Vector<ScanFilter>();
                filter.add(builder.build());
                ScanSettings.Builder settings = new ScanSettings.Builder();
                switch (mBleScanMode) {
                    case SCAN_LOW_LATENCY:
                        Log.d(TAG, "SCAN_MODE: LOW_LATENCY");
                        settings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                        break;
                    case SCAN_LOW_POWER:
                        Log.d(TAG, "SCAN_MODE: LOW_POWER");
                        settings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                        break;
                    case SCAN_BALANCED:
                        Log.d(TAG, "SCAN_MODE: BALANCED");
                        settings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
                        break;
                }
                btScanner.startScan(filter, settings.build(), leScanCallback);
            }
        });
    }

    public void stopScanning() {
        Log.d(TAG, "stopScanning()");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        // Service終了
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
