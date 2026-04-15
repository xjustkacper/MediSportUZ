package com.example.medisportuz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "StepCounterChannel";
    private static final int NOTIFICATION_ID = 1;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;
    private SharedPreferences prefs;

    private float stepsAtReset = -1;
    private int currentDaySteps = 0;
    
    // Algorytm buforowania (filtr machania ręką)
    private int stepBuffer = 0;
    private long lastStepTimeNs = 0;
    private static final long MIN_STEP_DELAY_NS = 200000000L; // 200ms
    private static final long MAX_STEP_DELAY_NS = 2000000000L; // 2s
    private static final int MIN_STEPS_TO_START = 6;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        
        checkMidnightReset();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification(currentDaySteps));

        registerSensors();
    }

    private void checkMidnightReset() {
        String lastDate = prefs.getString("last_recorded_date", "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!currentDate.equals(lastDate)) {
            // Nowy dzień - resetujemy licznik
            currentDaySteps = 0;
            stepsAtReset = -1; // Zostanie ustawione przy następnym odczycie sensora
            prefs.edit()
                    .putInt("last_recorded_steps", 0)
                    .putFloat("steps_at_reset", -1)
                    .putString("last_recorded_date", currentDate)
                    .apply();
            Log.d(TAG, "Reset o północy wykonany. Nowy dzień: " + currentDate);
        } else {
            stepsAtReset = prefs.getFloat("steps_at_reset", -1);
            currentDaySteps = prefs.getInt("last_recorded_steps", 0);
        }
    }

    private void registerSensors() {
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        checkMidnightReset(); // Dodatkowe sprawdzenie przy zmianie sensora
        
        long currentTimeNs = event.timestamp;

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                handleDetectorStep(currentTimeNs);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleCounterSync(event.values[0]);
        }
    }

    private void handleDetectorStep(long timeNs) {
        long delay = timeNs - lastStepTimeNs;
        if (delay < MIN_STEP_DELAY_NS) return;

        if (delay > MAX_STEP_DELAY_NS) {
            stepBuffer = 1;
        } else {
            stepBuffer++;
        }

        lastStepTimeNs = timeNs;

        if (stepBuffer == MIN_STEPS_TO_START) {
            currentDaySteps += MIN_STEPS_TO_START;
            saveAndNotify();
        } else if (stepBuffer > MIN_STEPS_TO_START) {
            currentDaySteps++;
            saveAndNotify();
        }
    }

    private void handleCounterSync(float totalSteps) {
        if (stepsAtReset == -1) {
            stepsAtReset = totalSteps;
            prefs.edit().putFloat("steps_at_reset", stepsAtReset).apply();
            Log.d(TAG, "Ustawiono nową bazę kroków (stepsAtReset): " + stepsAtReset);
        }

        int counterTotal = (int) (totalSteps - stepsAtReset);
        
        if (counterTotal > currentDaySteps) {
            currentDaySteps = counterTotal;
            saveAndNotify();
        }
    }

    private void saveAndNotify() {
        prefs.edit().putInt("last_recorded_steps", currentDaySteps).apply();
        updateNotification(currentDaySteps);
        broadcastStepUpdate(currentDaySteps);
    }

    private void broadcastStepUpdate(int steps) {
        Intent intent = new Intent("STEP_UPDATE");
        intent.putExtra("steps", steps);
        sendBroadcast(intent);
    }

    private void updateNotification(int steps) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, getNotification(steps));
        }
    }

    private Notification getNotification(int steps) {
        float calories = steps * 0.04f;
        String contentText = String.format(Locale.getDefault(), "Kroki: %d  |  Kalorie: %.0f kcal", steps, calories);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MediSportUZ++: Krokomierz")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_steps)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Licznik Kroków",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}
