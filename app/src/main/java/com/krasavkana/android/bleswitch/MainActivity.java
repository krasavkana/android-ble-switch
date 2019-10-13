package com.krasavkana.android.bleswitch;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
//import android.support.v7.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Fragment;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private String bleName;
    private String bleUuid;
    private String bleAddress;
    private String bleScan;

    //PochiruEco
    private static final String DEFAULT_BLE_UUID = "b3b36901-50d3-4044-808d-50835b13a6cd";

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"Start Service...");

                Intent intent = new Intent(getApplication(), BleSwitchService.class);
                intent.putExtra("REQUEST_CODE", 1);
                Log.d(TAG,"bleDeviceAddress: " + bleAddress);
                if(bleAddress.length() > 0) {
                    intent.putExtra("BLE_DEVICE_ADDRESS", bleAddress);
                }
                Log.d(TAG,"bleServiceUuid: " + bleUuid);
                if(bleUuid.length() > 0) {
                    intent.putExtra("BLE_SERVICE_UUID", bleUuid);
                }
                Log.d(TAG,"bleDeviceName: " + bleName);
                if(bleName.length() > 0) {
                    intent.putExtra("BLE_DEVICE_NAME", bleName);
                }
                Log.d(TAG,"bleScanMode: " + bleScan);
                if(bleScan.length() > 0) {
                    intent.putExtra("BLE_SCAN_MODE", bleScan);
                }

                // Serviceの開始
                if(Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                }else{
                    startService(intent);
                }
                findViewById(R.id.start_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.stop_button).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"Stop Service...");
                Intent intent = new Intent(getApplication(), BleSwitchService.class);
                // Serviceの停止
                stopService(intent);
                findViewById(R.id.start_button).setVisibility(View.VISIBLE);
                findViewById(R.id.stop_button).setVisibility(View.INVISIBLE);
            }
        });

        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs BT service");
            builder.setMessage("Cannot invoke service that scans Ble switches.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
            return;
        }

        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (btAdapter == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs BT function");
            builder.setMessage("Cannot invoke service that scans Ble switches.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
            return;
        }

        BluetoothLeScanner btScanner = btAdapter.getBluetoothLeScanner();

        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        // レイアウトルートの背景をテーマ設定の値によって変更
        RelativeLayout root = findViewById(R.id.root);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        switch (mPref.getString("preference_theme", getString(R.string.default_value_preference_theme))) {
            case "light":
                root.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case "dark":
                root.setBackgroundColor(Color.parseColor("#000000"));
                break;
        }
//        mPref = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        bleName = mPref.getString("bledevicename", "");
        Log.d(TAG, "blename: " + bleName);
        ((TextView) findViewById(R.id.name_value)).setText(bleName);

        bleUuid = mPref.getString("bleserviceuuid", "");
        if (bleUuid != null && bleUuid.length() == 0) {
            bleUuid = DEFAULT_BLE_UUID;
        }
        Log.d(TAG, "bleuuid: " + bleUuid);
        ((TextView) findViewById(R.id.uuid_value)).setText(bleUuid);

        bleAddress = mPref.getString("bledeviceaddress", "");
        Log.d(TAG, "bleaddress: " + bleAddress);
        if (bleAddress.length() == 12) {
            bleAddress = getBleDeviceAddress(bleAddress);
        }
        ((TextView) findViewById(R.id.address_value)).setText(bleAddress);

        bleScan = mPref.getString("preference_blescan", getString(R.string.default_value_preference_blescan));
        Log.d(TAG, "blescan: " + bleScan);
        ((TextView) findViewById(R.id.scan_value)).setText(bleScan);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // メニューが選択されたときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem1:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.menuItem2:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getBleDeviceAddress(String adr){

        StringBuilder sb = new StringBuilder();
        sb.append(adr.substring(0,2));
        sb.append(":");
        sb.append(adr.substring(2,4));
        sb.append(":");
        sb.append(adr.substring(4,6));
        sb.append(":");
        sb.append(adr.substring(6,8));
        sb.append(":");
        sb.append(adr.substring(8,10));
        sb.append(":");
        sb.append(adr.substring(10,12));
        return sb.toString();
    }

}
