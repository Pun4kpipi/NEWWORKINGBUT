package com.example.myapplicationlplplp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
@SuppressLint("MissingPermission")
public class BluetoothHelper {

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private OutputStream out;
    public BluetoothHelper(){
        try {
            var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            var boundedDevices = bluetoothAdapter.getBondedDevices();
            var device = boundedDevices.stream().filter(d -> "JDY-31-SPP".equals(d.getName())).findFirst().orElseThrow();
            var bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean connect(Context ctx) throws IOException {
        try {
        socket.connect();
        out  = socket.getOutputStream();
        return true;
    }  catch (SecurityException sec) {
            throw new IOException("Bluetooth permission not granted", sec);
        }
    }


    public void send(char c) throws IOException {
        if (out != null) {
            out.write(c);
            out.write('\n');
            out.write('\r');
        }
    }


    public void disconnect() throws IOException {
        if (out  != null) out.close();
        if (socket != null) socket.close();
    }

}
