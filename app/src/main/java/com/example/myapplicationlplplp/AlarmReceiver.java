package com.example.myapplicationlplplp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static Ringtone activeRingtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        int command = intent.getIntExtra("cmd", 1); // 1 = включить, 0 = выключить
        Log.d(TAG, "AlarmReceiver получил команду: " + command);

        // Сначала остановим предыдущий Ringtone (если есть)
        if (activeRingtone != null) {
            activeRingtone.stop();
            activeRingtone = null;
        }

        // Отправка команды на устройство через Bluetooth
        new Thread(() -> {
            BluetoothHelper bh = new BluetoothHelper();
            try {
                bh.connect(context);
                bh.send(command == 1 ? '1' : '0');
                bh.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Bluetooth ошибка", e);
            }
        }).start();

        // Если команда = включить, запускаем звук и вибрацию
        if (command == 1) {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneUri == null) ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            activeRingtone = RingtoneManager.getRingtone(context, ringtoneUri);
            if (activeRingtone != null) activeRingtone.play();

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(2000);

            Toast.makeText(context, "⏰ Wake up!", Toast.LENGTH_LONG).show();
        }
    }

    // Метод для остановки Ringtone извне (MainActivity)
    public static void stopRingtone() {
        if (activeRingtone != null) {
            activeRingtone.stop();
            activeRingtone = null;
        }
    }
}
