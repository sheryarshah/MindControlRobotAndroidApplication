package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class RoombaAcclerometerActivity extends Activity implements SensorEventListener {

    private static final String TAG = "RoombaAcclerometer";

    Button start, mode, roombaStop, begin;
    // TextView txtArduino;
    Handler h;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;
    boolean go = false;

    boolean forward = true;
    boolean backward  = true;
    boolean left = true;
    boolean right = true;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    //private String address = "00:15:FF:F2:59:03";

    //ORIGINAL bt USING
    // private String address = "20:13:07:26:47:87";

    private String address = "98:D3:31:80:59:BE";

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomba_acclerometer);

        //  btnOn = (Button) findViewById(R.id.btnOn);                  // button LED ON
        //   btnOff = (Button) findViewById(R.id.btnOff);                // button LED OFF
        roombaStop=(Button) findViewById(R.id.stopRoomba);
        //   spinLeft=(Button) findViewById(R.id.SpinLeftBtn);
        //   spinRight=(Button) findViewById(R.id.SpinRightBtn);
        start=(Button) findViewById(R.id.start);
        mode=(Button) findViewById(R.id.mode);
        begin=(Button) findViewById(R.id.begin);
        //    txtArduino = (TextView) findViewById(R.id.txtArduino);      // for display the received data from the Arduino

        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
                            //    txtArduino.setText("Data from Arduino: " + sbprint);            // update TextView
                            //    btnOff.setEnabled(true);
                            //   btnOn.setEnabled(true);
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(128);
                //   Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        mode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth

                mConnectedThread.write2(131);
                //    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        begin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth

                go = true;
                left = true;
                right = true;
                forward = true;
                backward = true;
                //    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        roombaStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //    mConnectedThread.write("2");
                mConnectedThread.write2(137);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                go = false;
                Toast.makeText(getBaseContext(), "Roomba Stop", Toast.LENGTH_SHORT).show();
            }
        });
    }

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

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }

        mSensorManager.unregisterListener(this);
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


    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }else {

            if(go){

                float deltaX = Math.abs(mLastX - x);
                float deltaY = Math.abs(mLastY - y);
                float deltaZ = Math.abs(mLastZ - z);
                if (deltaX < NOISE) deltaX = (float) 0.0;
                if (deltaY < NOISE) deltaY = (float) 0.0;
                if (deltaZ < NOISE) deltaZ = (float) 0.0;
                mLastX = x;
                mLastY = y;
                mLastZ = z;



                if (mLastY > 4) {
                    Log.d("FLAG", "Forward flag = " +forward);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(forward){
                                mConnectedThread.write2(137);
                                mConnectedThread.write2(255);
                                mConnectedThread.write2(56);
                                mConnectedThread.write2(128);
                                mConnectedThread.write2(0);
                                forward = false;
                                backward = true;
                                right = true;
                                left = true;
                            }
                        }
                    },500);

                    //   mConnectedThread.write2(137);


                } else if (mLastY < -4) {
                    Log.d("FLAG", "Backward flag = " +backward);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(backward){
                                mConnectedThread.write2(137);
                                mConnectedThread.write2(0);
                                mConnectedThread.write2(200);
                                mConnectedThread.write2(128);
                                mConnectedThread.write2(0);

                                backward = false;
                                forward = true;
                                right = true;
                                left = true;
                            }
                        }
                    },500);


                }

                if (mLastX > 7) {

                    Log.d("FLAG", "Right flag = " +right);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(right){
                                mConnectedThread.write2(137);
                                mConnectedThread.write2(0);
                                mConnectedThread.write2(0xc8);
                                mConnectedThread.write2(0x00);
                                mConnectedThread.write2(0x01);

                                right = false;
                                left = true;
                                backward = true;
                                forward = true;
                            }
                        }
                    },500);


                } else if (mLastX < -7) {

                    Log.d("FLAG", "Left flag = " +left);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(left){
                                mConnectedThread.write2(137);
                                mConnectedThread.write2(0);
                                mConnectedThread.write2(0xc8);
                                mConnectedThread.write2(0xff);
                                mConnectedThread.write2(0xff);
                                left = false;
                                right = true;
                                backward = true;
                                forward = true;
                            }
                        }
                    },500);


                }

            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
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

}
