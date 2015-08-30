package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

import java.util.UUID;


public class ControlBrightnessActivity extends Activity {

    private static final String TAG = "BrightnessActivity";

    private static String mindHeadsetAddress = "20:68:9D:70:C7:CA";
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mindDevice;
    TextView AppStatus;
    TGDevice tgDevice;
    TextView ActivityLevelValue;
    ProgressBar ActivityLevelProgressBar;

    TextView SignalStatus;

    ProgressBar SignalProgressBar;


    int poorSignalRaw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_brightness);

        ActivityLevelValue = (TextView) findViewById(R.id.ActivityLevelValue);
        ActivityLevelProgressBar = (ProgressBar) findViewById(R.id.ActivityLevelProgressBar);

        SignalStatus = (TextView) findViewById(R.id.SignalStatus);

        SignalProgressBar = (ProgressBar) findViewById(R.id.SignalProgressBar);

        AppStatus = (TextView)findViewById(R.id.AppStatus);
        AppStatus.setText("");
        AppStatus.setText("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if(bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else {
            mindDevice = bluetoothAdapter.getRemoteDevice(mindHeadsetAddress);
        	/* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
        }

    }

    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            AppStatus.setText("Connecting...\n");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            AppStatus.setText("Connected.\n");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            AppStatus.setText("Can't find\n");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            AppStatus.setText("not paired\n");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            AppStatus.setText("Disconnected\n");
                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    //             AppStatus.setText("PoorSignal: " + msg.arg1 + "\n");

                    poorSignalRaw = msg.arg1;
                    int signalProgress = (int) (200-poorSignalRaw)/2;
                    SignalProgressBar.setProgress(signalProgress);

                    if (poorSignalRaw>50){
                        SignalStatus.setTextColor(Color.RED);
                        SignalStatus.setText("POOR");
                       // SignalProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.ADD);
                    }else if (poorSignalRaw<=50 && poorSignalRaw >=25){
                        SignalStatus.setTextColor(Color.YELLOW);
                        SignalStatus.setText("GOOD");
                    }else{
                        SignalStatus.setTextColor(Color.GREEN);
                        SignalStatus.setText("EXCELLENT");
                      //  SignalProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.ADD);
                    }

                    break;
                case TGDevice.MSG_RAW_DATA:
                    //raw1 = msg.arg1;
                    //                AppStatus.setText("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    //                  AppStatus.setText("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    //att = msg.arg1;
                    //                   AppStatus.setText("Attention: " + msg.arg1 + "\n");
                    if (poorSignalRaw>0){
                        break;
                    }
                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, msg.arg1);
                    int activityLevelProgress = msg.arg1;
                    ActivityLevelProgressBar.setProgress(activityLevelProgress);

                    ActivityLevelValue.setText(String.valueOf(activityLevelProgress));
                    setBrightness(activityLevelProgress);

                    break;
                case TGDevice.MSG_MEDITATION:
                    //  AppStatus.setText("Meditation: " +msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_BLINK:
                    // AppStatus.setText("Blink: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //AppStatus.setText("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //AppStatus.setText("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
                default:
                    break;
            }
        }
    };

    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            //tgDevice.connect(rawEnabled);
            tgDevice.connect(mindDevice);

        //tgDevice.ena
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tgDevice.close();
    }

    private void setBrightness(int activityLevelProgress) {
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.screenBrightness = (float)(activityLevelProgress)/100;
        if (lp.screenBrightness<.01f) lp.screenBrightness=.01f;
        w.setAttributes(lp);

    }


}
