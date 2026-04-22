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
/**
 * @brief Usługa działająca w tle (Foreground Service) odpowiedzialna za zliczanie kroków użytkownika.
 * * Wykorzystuje sprzętowe sensory urządzenia (TYPE_STEP_COUNTER oraz TYPE_STEP_DETECTOR)
 * do monitorowania aktywności. Posiada zaimplementowany własny filtr niwelujący wpływ
 * przypadkowych ruchów ręką. O północy licznik ulega wyzerowaniu. Kiedy użytkownik
 * osiągnie swój dzienny cel, serwis wysyła powiadomienie typu "High Priority"
 * i zatrzymuje swoje działanie, aby oszczędzać baterię.
 */
public class StepCounterService extends Service implements SensorEventListener {

    private static final String TAG = "StepCounterService";
    /** ID kanału powiadomienia ciągłego (Foreground) */
    private static final String CHANNEL_ID = "StepCounterChannel";
    /** ID kanału dla powiadomienia o osiągnięciu celu */
    private static final String GOAL_CHANNEL_ID = "StepGoalChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int GOAL_NOTIFICATION_ID = 2;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;
    private SharedPreferences prefs;
    /** * @brief Stan sprzętowego licznika w momencie resetu.
     * Ponieważ systemowy TYPE_STEP_COUNTER zlicza kroki od momentu uruchomienia telefonu,
     * musimy przechowywać wartość odcięcia (offset), aby obliczyć kroki dla danego dnia.
     */
    private float stepsAtReset = -1;
    /** Aktualna, dzienna liczba kroków obliczona przez aplikację */
    private int currentDaySteps = 0;
    /** Domyślny dzienny cel użytkownika, pobierany z ustawień */
    private int stepGoal = 10000;
    /** Flaga określająca, czy cel został dzisiaj osiągnięty */
    private boolean goalReachedToday = false;

    // --- Algorytm buforowania (filtr "machania ręką") ---

    /** Licznik zarejestrowanych, poprawnych ruchów w bieżącej serii kroków */
    private int stepBuffer = 0;
    /** Znacznik czasu ostatniego wykrytego kroku w nanosekundach */
    private long lastStepTimeNs = 0;
    /** Minimalny dopuszczalny odstęp między krokami (200ms) - odsiewa zakłócenia */
    private static final long MIN_STEP_DELAY_NS = 200000000L; // 200ms
    /** Maksymalny odstęp między krokami (2s) - powyżej traktowane jako przerwa w spacerze */
    private static final long MAX_STEP_DELAY_NS = 2000000000L; // 2s
    /** Minimalna liczba kroków, aby system uznał, że zaczął się spacer, a nie pojedynczy ruch */
    private static final int MIN_STEPS_TO_START = 6;
    /**
     * @brief Wywoływana przy tworzeniu usługi. Inicjuje konfigurację i sensory.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("MediSportPrefs", Context.MODE_PRIVATE);
        
        checkMidnightReset();
        loadStepGoal();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        createNotificationChannel();
        createGoalNotificationChannel();
        // Uruchomienie jako Foreground Service, zapobiegające ubiciu procesu przez system Android
        startForeground(NOTIFICATION_ID, getNotification(currentDaySteps));

        registerSensors();
    }
    /**
     * @brief Wczytuje aktualny cel kroków z ustawień użytkownika.
     */
    private void loadStepGoal() {
        String goalStr = prefs.getString("step_goal", "10000");
        try {
            stepGoal = Integer.parseInt(goalStr);
        } catch (NumberFormatException e) {
            stepGoal = 10000;
        }
        goalReachedToday = prefs.getBoolean("goal_reached_today", false);
    }
    /**
     * @brief Sprawdza, czy nastąpiła zmiana daty. Jeśli tak, zeruje dzienny licznik kroków.
     * * Zapisuje nową datę do preferencji, aby wymusić ponowne odczytanie offsetu sprzętowego.
     */
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
                    .putBoolean("goal_reached_today", false)
                    .apply();
            goalReachedToday = false;
            Log.d(TAG, "Reset o północy wykonany. Nowy dzień: " + currentDate);
        } else {
            stepsAtReset = prefs.getFloat("steps_at_reset", -1);
            currentDaySteps = prefs.getInt("last_recorded_steps", 0);
            goalReachedToday = prefs.getBoolean("goal_reached_today", false);
        }
    }
    /**
     * @brief Rejestruje usługę jako nasłuchiwacza na zmiany sprzętowe czujników ruchu.
     */
    private void registerSensors() {
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }
    /**
     * @brief Wywoływana za każdym razem, gdy Activity wywołuje startService().
     * * Pozwala odświeżyć dane, np. zmieniony cel z ustawień. START_STICKY oznacza,
     * że system spróbuje podnieść usługę w razie jej ubicia przez brak zasobów.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Reload goal in case it was changed from Settings
        loadStepGoal();
        return START_STICKY;
    }
    /**
     * @brief Odbiera nowe dane z podpiętych czujników kroków.
     * @param event Obiekt zdarzenia zawierający typ sensora i jego aktualne wartości.
     */
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
    /**
     * @brief Analizuje pojedyncze kroki za pomocą autorskiego filtru.
     * * Jeśli czas między krokami jest prawidłowy i nazbiera się ich minimalna seria
     * (MIN_STEPS_TO_START), są one dodawane do głównego licznika.
     * @param timeNs Znacznik czasu zdarzenia (w nanosekundach).
     */
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
    /**
     * @brief Synchronizuje licznik dzienny z globalnym krokomierzem sprzętowym.
     * * Rozwiązuje problem "zgubienia" kroków, gdy usługa została na chwilę zamknięta przez system.
     * @param totalSteps Całkowita liczba kroków ze sprzętowego sensora TYPE_STEP_COUNTER.
     */
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
    /**
     * @brief Zapisuje zaktualizowaną liczbę kroków do pamięci, wysyła komunikat (Broadcast)
     * do Activity oraz sprawdza, czy dzienny cel został osiągnięty.
     */
    private void saveAndNotify() {
        prefs.edit().putInt("last_recorded_steps", currentDaySteps).apply();
        broadcastStepUpdate(currentDaySteps);

        // Sprawdź czy cel został osiągnięty
        if (currentDaySteps >= stepGoal && !goalReachedToday) {
            goalReachedToday = true;
            prefs.edit().putBoolean("goal_reached_today", true).apply();

            // Wyślij popup powiadomienie o osiągnięciu celu
            showGoalReachedNotification();

            // Zatrzymaj serwis (onDestroy zajmie się usunięciem powiadomienia w tle)
            Log.d(TAG, "Cel osiągnięty! Zatrzymuję serwis. Kroki: " + currentDaySteps);
            stopSelf();
            return;
        }

        updateNotification(currentDaySteps);
    }
    /**
     * @brief Wyświetla jednorazowe, wyróżniające się powiadomienie z gratulacjami
     * w momencie osiągnięcia wyznaczonego przez użytkownika celu.
     */
    private void showGoalReachedNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        String contentText = String.format(Locale.getDefault(),
                "Zrobiłeś %d kroków! Cel %d kroków osiągnięty!", currentDaySteps, stepGoal);

        Notification notification = new NotificationCompat.Builder(this, GOAL_CHANNEL_ID)
                .setContentTitle("🎉 Cel dzienny osiągnięty!")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_steps)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(false)
                .build();

        notificationManager.notify(GOAL_NOTIFICATION_ID, notification);
    }
    /**
     * @brief Rozsyła aktualną liczbę kroków jako intencję (Broadcast).
     * Jeśli aplikacja jest na ekranie, fragment odbierze te dane i zaktualizuje UI.
     */
    private void broadcastStepUpdate(int steps) {
        Intent intent = new Intent("STEP_UPDATE");
        intent.setPackage(getPackageName());
        intent.putExtra("steps", steps);
        sendBroadcast(intent);
    }
    /**
     * @brief Odświeża zawartość nieusuwalnego powiadomienia na pasku powiadomień.
     */
    private void updateNotification(int steps) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, getNotification(steps));
        }
    }
    /**
     * @brief Generuje obiekt Notification dla trybu Foreground.
     * @param steps Liczba kroków wyświetlana w powiadomieniu.
     * @return Gotowy obiekt powiadomienia.
     */
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
    /**
     * @brief Tworzy systemowy kanał powiadomień "Licznik Kroków" (wymagane od Android 8.0 Oreo).
     * Posiada niski priorytet, co zapobiega irytującemu dźwiękowi przy każdej aktualizacji kroku.
     */
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
    /**
     * @brief Tworzy systemowy kanał powiadomień "Licznik Kroków" (wymagane od Android 8.0 Oreo).
     * Posiada niski priorytet, co zapobiega irytującemu dźwiękowi przy każdej aktualizacji kroku.
     */
    private void createGoalNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel goalChannel = new NotificationChannel(
                    GOAL_CHANNEL_ID,
                    "Cel Kroków",
                    NotificationManager.IMPORTANCE_HIGH
            );
            goalChannel.setDescription("Powiadomienie o osiągnięciu dziennego celu kroków");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(goalChannel);
        }
    }
    /** Nieużywane w tym zastosowaniu. Wymagane przez interfejs SensorEventListener. */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    /** Usługa typu Started (nie Bound), więc zwracany jest null. */
    @Override
    public IBinder onBind(Intent intent) { return null; }
    /**
     * @brief Wywoływana podczas zatrzymywania usługi.
     * Zwalnia zasoby systemowe, odpina sensory oraz czyści powiadomienie z paska.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        try {
            stopForeground(true);
        } catch (Exception e) {
            Log.e(TAG, "Error stopping foreground", e);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
    }
}
