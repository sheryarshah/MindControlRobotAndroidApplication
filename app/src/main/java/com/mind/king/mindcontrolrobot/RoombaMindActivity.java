package com.mind.king.mindcontrolrobot;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class RoombaMindActivity extends Activity {

    private static final String TAG = "MindActivity";

    BluetoothAdapter bluetoothAdapter, btAdapter;
    BluetoothDevice mindDevice;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();


    TextView tv;

    File myFile;
    OutputStreamWriter myOutWriter;
    FileOutputStream fOut;
    BufferedWriter newLineWriter;

    private static String mindHeadsetAddress = "20:68:9D:70:C7:CA";
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String address = "98:D3:31:80:59:BE";

    private ConnectedThread mConnectedThread;

    TextView blinkCounterText;
    TextView ActivityLevelValue;
    ProgressBar ActivityLevelProgressBar;


    private Handler mHandler = new Handler();

    TGDevice tgDevice;
    final boolean rawEnabled = false;

    boolean forward1 = true;
    boolean forward2 = true;
    boolean forward3 = true;
    boolean forward4 = true;
    boolean forward5 = true;
    boolean forward6 = true;
    boolean forward7 = true;
    boolean forward8 = true;
    boolean forward9 = true;
    boolean backward  = true;
    boolean left = true;
    boolean right = true;
    boolean stop = true;

    ProgressBar SignalProgressBar;

    TextView SignalStatus;
    TextView AppStatus;

    int poorSignalRaw;
    int blinkCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomba_mind);

        blinkCounterText = (TextView) findViewById(R.id.blinkCounterStatus);

        ActivityLevelValue = (TextView) findViewById(R.id.ActivityLevelValue);
        ActivityLevelProgressBar = (ProgressBar) findViewById(R.id.ActivityLevelProgressBar);

        SignalStatus = (TextView) findViewById(R.id.SignalStatus);

        SignalProgressBar = (ProgressBar) findViewById(R.id.SignalProgressBar);


        AppStatus = (TextView)findViewById(R.id.AppStatus);
        AppStatus.setText("");
        AppStatus.setText("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

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

      /*  myFile = new File("/sdcard/brainSensor.txt");
        try {
            myFile.createNewFile();
            fOut = new FileOutputStream(myFile);
            myOutWriter = new OutputStreamWriter(fOut);
            newLineWriter = new BufferedWriter(myOutWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
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
                //    address = String.valueOf(btAdapter.getRemoteDevice(device.getAddress().toString()));
                Log.d("Mac Addressess"," are:  "+btAdapter.getRemoteDevice(device.getAddress()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        mConnectedThread.write2(128);
        mConnectedThread.write2(131);
        mConnectedThread.write2(131);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tgDevice.close();
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
     /*   try {
            myOutWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            newLineWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
                            AppStatus.setText("Not paired\n");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            AppStatus.setText("Disconnected\n");
                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    poorSignalRaw = msg.arg1;
                    int signalProgress = (int) (200 - poorSignalRaw) / 2;
                    SignalProgressBar.setProgress(signalProgress);

                    if (poorSignalRaw > 50) {
                        SignalStatus.setTextColor(Color.RED);
                        SignalStatus.setText("POOR");
                        //  SignalProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    } else if (poorSignalRaw <= 50 && poorSignalRaw >= 25) {
                        SignalStatus.setTextColor(Color.YELLOW);
                        SignalStatus.setText("GOOD");
                    } else {
                        SignalStatus.setTextColor(Color.GREEN);
                        SignalStatus.setText("EXCELLENT");
                        //   SignalProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

                    }
                    break;
                case TGDevice.MSG_RAW_DATA:
                    //raw1 = msg.arg1;
                    //tv.append("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    // blinkCounterText.setText("Activity Level: " +msg.arg1);
                    if (poorSignalRaw > 0) {
                        mConnectedThread.write2(137);
                        mConnectedThread.write2(0);
                        mConnectedThread.write2(0);
                        mConnectedThread.write2(0);
                        mConnectedThread.write2(0);
                        forward1 = true;
                        forward2 = true;
                        forward3 = true;
                        forward4 = true;
                        forward5 = true;
                        forward6 = true;
                        forward7 = true;
                        forward8 = true;
                        forward9 = true;
                        right = true;
                        blinkCounter = 0;
                        mConnectedThread.write2(128);
                        mConnectedThread.write2(131);
                        mConnectedThread.write2(131);

                        break;
                    }

                    int activityLevelProgress = msg.arg1;
                    ActivityLevelProgressBar.setProgress(activityLevelProgress);

                    ActivityLevelValue.setText(String.valueOf(activityLevelProgress));

                    if (msg.arg1 > 20 && msg.arg1 < 30) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward1) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x00);
                                    mConnectedThread.write2(0x32);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward1 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);

                    } else if (msg.arg1 > 30 && msg.arg1 < 40) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward2) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x00);
                                    mConnectedThread.write2(0x64);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward2 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 40 && msg.arg1 < 50) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward3) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x00);
                                    mConnectedThread.write2(0x96);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward3 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 50 && msg.arg1 < 60) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward4) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x00);
                                    mConnectedThread.write2(0xc8);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward4 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 60 && msg.arg1 < 70) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward5) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x00);
                                    mConnectedThread.write2(0xfa);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward5 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 70 && msg.arg1 < 80) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward6) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x01);
                                    mConnectedThread.write2(0x2c);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward6 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 80 && msg.arg1 < 90) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward7) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x01);
                                    mConnectedThread.write2(0x90);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward7 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 > 90 && msg.arg1 < 95) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward8) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x01);
                                    mConnectedThread.write2(0xc2);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward8 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else if (msg.arg1 == 100) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (forward9) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0x01);
                                    mConnectedThread.write2(0x0f4);
                                    mConnectedThread.write2(128);
                                    mConnectedThread.write2(0);
                                    forward9 = false;
                                    backward = true;
                                    stop = true;
                                }
                            }
                        }, 50);
                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (stop) {
                                    mConnectedThread.write2(137);
                                    mConnectedThread.write2(0);
                                    mConnectedThread.write2(0);
                                    mConnectedThread.write2(0);
                                    mConnectedThread.write2(0);
                                    stop = false;
                                    forward1 = true;
                                    forward2 = true;
                                    forward3 = true;
                                    forward4 = true;
                                    forward5 = true;
                                    forward6 = true;
                                    forward7 = true;
                                    forward8 = true;
                                    forward9 = true;

                                }
                            }
                        }, 50);

                    }
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:
                    //  AppStatus.setText("Meditation: " + msg.arg1 + "\n");
                    //  tv.append("Meditation: " +msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_BLINK:
                    AppStatus.setText("Blink: " + msg.arg1 + "\n");
                    blinkCounterText.setText("Blink Counter: " + blinkCounter);
                    if (msg.arg1 > 50) {
                        blinkCounter++;
                        if (blinkCounter == 2) {
                            //   mHandler.postDelayed(new Runnable() {
                            //       @Override
                            //     public void run() {
                            if (right) {
                                mConnectedThread.write2(137);
                                mConnectedThread.write2(0);
                                mConnectedThread.write2(0xc8);
                                mConnectedThread.write2(0xff);
                                mConnectedThread.write2(0xff);
                                right = false;
                                forward1 = false;
                                forward2 = false;
                                forward3 = false;
                                forward4 = false;
                                forward5 = false;
                                forward6 = false;
                                forward7 = false;
                                forward8 = false;
                                forward9 = false;
                            }
                        }
                        // },100);
                        // }

                 //   }
                    if (blinkCounter == 4) {
                        forward1 = true;
                        forward2 = true;
                        forward3 = true;
                        forward4 = true;
                        forward5 = true;
                        forward6 = true;
                        forward7 = true;
                        forward8 = true;
                        forward9 = true;
                        right = true;
                        blinkCounter = 0;
                    }
            }
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
                default:
                    break;
            }
        }
    };


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    //   h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write2(int message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            //   byte[] msgBuffer = message.getBytes();
            byte msgBuffer = (byte) ( message & 0xff);
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }

    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            //tgDevice.connect(rawEnabled);
            tgDevice.connect(mindDevice);

        //tgDevice.ena
    }

}
