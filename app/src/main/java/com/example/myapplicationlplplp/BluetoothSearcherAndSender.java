package com.example.myapplicationlplplp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BluetoothSearcherAndSender {

    private static final String TAG = "BT_Searcher_Sender";
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothSearcherAndSender() {}

    /**
     * Ищет устройство, подключается, отправляет 0 или 1, закрывает соединение.
     *
     * @param targetName имя устройства, например "JDY-31-SPP"
     * @param value 0 или 1
     * @return true если успешно
     */
    public boolean findSendAndClose(String targetName, int value) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "Bluetooth не поддерживается");
            return false;
        }

        if (!adapter.isEnabled()) {
            Log.e(TAG, "Bluetooth выключен");
            return false;
        }

        // --- 1. Поиск устройства ---
        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        if (bonded == null || bonded.isEmpty()) {
            Log.e(TAG, "Нет спаренных устройств");
            return false;
        }

        BluetoothDevice device = null;
        for (BluetoothDevice d : bonded) {
            if (targetName.equals(d.getName())) {
                device = d;
                break;
            }
        }

        if (device == null) {
            Log.e(TAG, "Устройство не найдено: " + targetName);
            return false;
        }

        BluetoothSocket socket = null;

        try {
            // --- 2. Подключение ---
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            adapter.cancelDiscovery();
            socket.connect();

            OutputStream os = socket.getOutputStream();

            // --- 3. Отправка байта ---
            byte b = (byte) (value == 0 ? 0 : 1);
            os.write(b);
            os.flush();

            Log.i(TAG, "Отправлено: " + b);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка: ", e);
            return false;

        } finally {
            // --- 4. Закрытие ---
            if (socket != null) {
                try {
                    socket.close();
                    Log.i(TAG, "Сокет закрыт");
                } catch (IOException e) {
                    Log.e(TAG, "Не удалось закрыть сокет", e);
                }
            }
        }
    }
}
