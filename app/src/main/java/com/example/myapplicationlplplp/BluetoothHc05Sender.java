package com.example.myapplicationlplplp; // <-- замени на свой пакет

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class BluetoothHc05Sender {

    private static final String TAG = "BluetoothHc05Sender";
    private static final String HC05_NAME = "HC-05"; // или своё имя модуля
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Одноразовая отправка данных на HC-05.
     * Вызывается, когда срабатывает будильник.
     */
    @SuppressLint("MissingPermission") // мы СНАЧАЛА проверяем разрешения, см. ниже
    public static void sendOneShot(Context context, String text) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "Bluetooth не поддерживается");
            return;
        }

        if (!adapter.isEnabled()) {
            Log.w(TAG, "Bluetooth выключен, не могу отправить");
            return;
        }

        // Для Android 12+ (API 31+) BLUETOOTH_CONNECT — runtime-разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Нет разрешения BLUETOOTH_CONNECT");
                return;
            }
        }

        BluetoothDevice hc05 = findPairedHc05(adapter);
        if (hc05 == null) {
            Log.e(TAG, "Спаренный HC-05 не найден");
            return;
        }

        BluetoothSocket socket = null;
        OutputStream os = null;

        try {
            socket = hc05.createRfcommSocketToServiceRecord(SPP_UUID);
            adapter.cancelDiscovery();
            socket.connect();

            os = socket.getOutputStream();
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.flush();

            Log.d(TAG, "Успешно отправил: " + text);

        } catch (IOException e) {
            Log.e(TAG, "Ошибка при отправке на HC-05", e);
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ignored) {}
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    @SuppressLint("MissingPermission") // getBondedDevices тоже помечен RequiresPermission
    private static BluetoothDevice findPairedHc05(BluetoothAdapter adapter) {
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        if (bonded == null || bonded.isEmpty()) return null;

        for (BluetoothDevice d : bonded) {
            String name = d.getName();
            if (name == null) continue;

            if (name.equals(HC05_NAME)) return d;
            if (name.contains("HC-05") || name.contains("HC05")) return d;
        }
        return null;
    }
}
