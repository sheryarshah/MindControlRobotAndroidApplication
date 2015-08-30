package com.mind.king.mindcontrolrobot;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Sheryar Shah on 8/9/2015.
 */
public class BTSocketCreation {

    private static final String TAG = "BTSocketCreation";


    private BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket = null;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private String address = "98:D3:31:80:59:BE";

    public BTSocketCreation(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
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

    public void createBTSocket() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

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
            this.setBtSocket(btSocket);
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket..." +getBtSocket());

    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    public void errorExit(String title, String message){
        Log.d(TAG, message);
        // Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    }

    public void destroyBTSocket(){
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
