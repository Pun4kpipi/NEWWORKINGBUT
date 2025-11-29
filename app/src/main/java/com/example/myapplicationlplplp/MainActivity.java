package com.example.myapplicationlplplp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onResume() {
        super.onResume();

        // Разрешение на точные будильники (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timePicker = findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Разрешения Bluetooth (Android 12+)
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH
                    },
                    0
            );
        }
    }

    public void onToggleClicked(View view) {
        ToggleButton tb = (ToggleButton) view;
        if (tb.isChecked()) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long trigger = cal.getTimeInMillis();
            if (trigger < System.currentTimeMillis()) {
                trigger += AlarmManager.INTERVAL_DAY;
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("cmd", 1);

            pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        trigger,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        trigger,
                        pendingIntent
                );
            }

            Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();

        } else {
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }

            AlarmReceiver.stopRingtone();

            Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                BluetoothHelper bh = new BluetoothHelper();
                try {
                    bh.connect(getApplicationContext());
                    bh.send('0');
                    bh.disconnect();
                } catch (IOException e) {
                    Log.e("", "Bluetooth ошибка", e);
                }
            }).start();
        }
    }

    public void onConnectClicked(View v) {
        Toast.makeText(this,
                "Bluetooth подключается автоматически.",
                Toast.LENGTH_SHORT).show();
    }
}
