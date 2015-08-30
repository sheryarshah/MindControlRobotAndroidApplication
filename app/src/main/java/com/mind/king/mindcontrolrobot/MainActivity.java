package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends Activity {

    private static final int RESULT_SETTINGS = 1;
    private static final int BT_SETTINGS = 2;

    private BluetoothAdapter btAdapter = null;

    Button roombaControlBtn, calibrationBtn;

    BTSocketCreation btSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showUserSettings();

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        btSocket = new BTSocketCreation(btAdapter);
        checkBTState();

        calibrationBtn = (Button) findViewById(R.id.calibrateBtn);
        roombaControlBtn = (Button) findViewById(R.id.roombaControllerBtn);

        calibrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(MainActivity.this, SensorCalibrationActivity.class);
                startActivity(myIntent);
            }
        });

        roombaControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(MainActivity.this, RoombaControlActivity.class);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, UserSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                break;
        }

        //start BT service once user has turned on BT
        if(requestCode == BT_SETTINGS){
            if(btAdapter.isEnabled()){
                //  btSocket.createBTSocket();
            }
            else{
                Toast.makeText(this, "Bluetooth not on", Toast.LENGTH_LONG).show();
                finish();
            }

        }

    }

    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        builder.append("\n BT Module MAC Address: "
                + sharedPrefs.getString("prefBTModule", "NULL"));

        builder.append("\n Mindwave Headset MAC Address: "
                + sharedPrefs.getString("prefBTMindwave", "NULL"));

    }


    //METHOD CHECKS BT STATE AND REQUESTS PERMISSION TO TURN ON BT IF NOT TURNED ON
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d("Bluetooth", "...Bluetooth ON...");
                //  btSocket.createBTSocket();
                // startService(new Intent(this, BTService.class));
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BT_SETTINGS);

            }
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d("Mac Addresses"," are:  "+btAdapter.getRemoteDevice(device.getAddress()));
            }
        }

    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

}