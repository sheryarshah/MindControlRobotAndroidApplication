package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class RoombaButtonControlActivity extends Activity {

    private static final String TAG = "ButtonActivity";
    private static final int BT_SETTINGS = 2;
    private static final int RESULT_SETTINGS = 1;

    Button forwardBtn, backwardBtn, spinLeftBtn, spinRightBtn, startBtn, modeBtn, roombaStopBtn, dockBtn;
    private StringBuilder sb = new StringBuilder();

    Handler h;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler

    private ConnectedThread mConnectedThread;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String address = "98:D3:31:80:59:BE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomba_button_control);


        forwardBtn = (Button) findViewById(R.id.btnOn);                  // button LED ON
        backwardBtn = (Button) findViewById(R.id.btnOff);                // button LED OFF
        roombaStopBtn =(Button) findViewById(R.id.stopRoomba);
        spinLeftBtn =(Button) findViewById(R.id.SpinLeftBtn);
        spinRightBtn =(Button) findViewById(R.id.SpinRightBtn);
        startBtn =(Button) findViewById(R.id.start);
        modeBtn =(Button) findViewById(R.id.mode);
        dockBtn =(Button) findViewById(R.id.dock);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        // btSocket = new BTSocketCreation(btAdapter);
        // btSocket = new BTService();
        checkBTState();

        //    btSocket.createBTSocket();


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
                            backwardBtn.setEnabled(true);
                            forwardBtn.setEnabled(true);
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        dockBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(143);
                //   Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(128);
                //   Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        modeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth

                mConnectedThread.write2(131);
                //    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        spinLeftBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(137);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0xc8);
                mConnectedThread.write2(0x00);
                mConnectedThread.write2(0x01);
                //  Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        spinRightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(137);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0xc8);
                mConnectedThread.write2(0xff);
                mConnectedThread.write2(0xff);
                //  Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                //  mConnectedThread.write("1");    // Send "1" via Bluetooth
                mConnectedThread.write2(137);
                mConnectedThread.write2(0x01);
                mConnectedThread.write2(0xf4);
                mConnectedThread.write2(128);
                mConnectedThread.write2(0);
                //    Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        //BACKWARD
        backwardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // btnOff.setEnabled(false);
                //   mConnectedThread.write("0");    // Send "0" via Bluetooth
                mConnectedThread.write2(137);
                mConnectedThread.write2(255);
                mConnectedThread.write2(56);
                mConnectedThread.write2(128);
                mConnectedThread.write2(0);
                //   Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

        roombaStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //    mConnectedThread.write("2");
                mConnectedThread.write2(137);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                mConnectedThread.write2(0);
                //  Toast.makeText(getBaseContext(), "Roomba Stop", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_speed_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_user_speed_settings:
                Intent i = new Intent(this,UserSpeedSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
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

        // mConnectedThread.write2(129);
        //mConnectedThread.write2(11);

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

    //METHOD CHECKS BT STATE AND REQUESTS PERMISSION TO TURN ON BT IF NOT TURNED ON
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d("Bluetooth", "...Bluetooth ON...");
                // btSocket.createBTSocket();
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


}
