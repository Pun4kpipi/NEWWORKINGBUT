package com.example.myapplicationlplplp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
@SuppressLint("MissingPermission")
public class BluetoothHelper {

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private OutputStream out;

    /**
     * Opens an RFCOMM socket to the first bonded HC-05/HC-06 module.
     * Call from a background thread to avoid Network-on-Main-Thread exception.
     */
    public boolean connect(Context ctx) throws IOException {
        try {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt == null) throw new IOException("No Bluetooth adapter");

        BluetoothDevice target = null;
        for (BluetoothDevice d : bt.getBondedDevices()) {
            String name = d.getName();
            if (name != null && (name.contains("HC-05") || name.contains("HC-06"))) {
                target = d;
                break;
            }
        }
        if (target == null) throw new IOException("HC-05/06 not paired");

        socket.connect();          // blocks ~1 s
        out  = socket.getOutputStream();
        return true;
    }  catch (SecurityException sec) {
            throw new IOException("Bluetooth permission not granted", sec);
        }
    }

    /** Send single byte without blocking UI thread. */
    public void send(char c) throws IOException {
        if (out != null) out.write(c);
    }

    /** Clean close. */
    public void disconnect() throws IOException {
        if (out  != null) out.close();
        if (socket != null) socket.close();
    }

}
