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
        int command = intent.getIntExtra("cmd", 1);
        Log.d(TAG, "AlarmReceiver got a command:  " + command);

        if (activeRingtone != null) {
            activeRingtone.stop();
            activeRingtone = null;
        }

        new Thread(() -> {
            BluetoothHelper bh = new BluetoothHelper();
            try {
                bh.connect(context);
                bh.send(command == 1 ? '1' : '0');
                bh.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Bluetooth error", e);
            }
        }).start();

        if (command == 1) {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (ringtoneUri == null) ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            activeRingtone = RingtoneManager.getRingtone(context, ringtoneUri);
            if (activeRingtone != null) activeRingtone.play();

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(2000);

            Toast.makeText(context, "It's time to take your medicine", Toast.LENGTH_LONG).show();
        }
    }

    public static void stopRingtone() {
        if (activeRingtone != null) {
            activeRingtone.stop();
            activeRingtone = null;
        }
    }
}
