package com.example.myapplicationlplplp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;
import android.bluetooth.BluetoothAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import android.provider.Settings;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private BluetoothHelper bt;
    private static final int REQ_BT_PERM = 101;   // arbitrary code
    private boolean btConnected = false;


    private void ensureBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQ_BT_PERM);
                return;   // don’t start BT until user grants
            }
        }
        // permission already OK → start your connect thread here
        startBluetoothThread();
    }

    private void startBluetoothThread() {
        new Thread(() -> {
            try {
                boolean ok = bt.connect(MainActivity.this);
                if (ok) {
                    btConnected = true;
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Arduino connected", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                // *** SURFACE THE REAL REASON ***
                Log.e("BT", "Connect failed", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Connect failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager)
                    getSystemService(ALARM_SERVICE);
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
        bt = new BluetoothHelper();

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
            // If the time is in the past, add one day
            if (trigger < System.currentTimeMillis()) {
                trigger += AlarmManager.INTERVAL_DAY;
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                if (btConnected) {
                    try {
                        bt.send('1');
                    } catch (IOException e) {
                        btConnected = false;
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Not connected – tap Connect first",
                                    Toast.LENGTH_SHORT).show());
                }
            }).start();

        } else {
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show();
            }

            // >>> SEND STOP COMMAND TO ARDUINO <<<
            new Thread(() -> {
                if (btConnected) {
                    try {
                        bt.send('0');
                    } catch (IOException e) {
                        btConnected = false;
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Not connected – tap Connect first",
                                    Toast.LENGTH_SHORT).show());
                }
            }).start();
        }

    }

    public void onConnectClicked(View v) {
        ensureBluetoothPermission();
    }
}