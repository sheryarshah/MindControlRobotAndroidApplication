package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;


public class SpeedDialActivity extends Activity {

    private static final String TAG = "SpeedDialActivity";

    private static String mindHeadsetAddress = "20:68:9D:70:C7:CA";

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mindDevice;
    TextView AppStatus;
    TGDevice tgDevice;

    TextView BlinkLevelValue;
    ProgressBar BlinkLevelProgressBar;

    String number1 = "2019937915";
    String number2 = "6317071609";

    TextView SignalStatus;

    int poorSignalRaw;

    ProgressBar SignalProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_dial);

        BlinkLevelValue = (TextView) findViewById(R.id.BlinkLevelValue);
        BlinkLevelProgressBar = (ProgressBar) findViewById(R.id.BlinkLevelProgressBar);

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
                            AppStatus.setText("Disconnected mang\n");
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
                    }else if (poorSignalRaw<=50 && poorSignalRaw >=25){
                        SignalStatus.setTextColor(Color.YELLOW);
                        SignalStatus.setText("GOOD");
                    }else{
                        SignalStatus.setTextColor(Color.GREEN);
                        SignalStatus.setText("EXCELLENT");
                    }

                    break;
                case TGDevice.MSG_RAW_DATA:

                    break;
                case TGDevice.MSG_HEART_RATE:

                    break;
                case TGDevice.MSG_ATTENTION:
                    if (poorSignalRaw>0){
                        break;
                    }

                    break;
                case TGDevice.MSG_MEDITATION:
                    //  AppStatus.setText("Meditation: " +msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_BLINK:

                    if (poorSignalRaw>0){
                        break;
                    }

                    int blinkLevelProgress = msg.arg1;
                    BlinkLevelProgressBar.setProgress(blinkLevelProgress);
                    BlinkLevelValue.setText(String.valueOf(blinkLevelProgress));

                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //AppStatus.setText("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        tgDevice.close();
    }

    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            //tgDevice.connect(rawEnabled);
            tgDevice.connect(mindDevice);

    }

}
