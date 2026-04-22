package com.example.medisportuz;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * @brief Główna klasa bazy danych aplikacji korzystająca z biblioteki Room.
 * * Klasa ta definiuje konfigurację bazy danych i służy jako główny punkt dostępu
 * do utrwalonych danych. Implementuje wzorzec Singleton, aby zapewnić istnienie
 * tylko jednej instancji bazy w całym cyklu życia aplikacji, co pozwala uniknąć
 * problemów z synchronizacją i nadmiernego zużycia zasobów.
 */
@Database(entities = {Note.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    /**
     * @brief Uzyskuje dostęp do obiektu Data Access Object (DAO) dla notatek.
     * * @return Interfejs NoteDao służący do wykonywania operacji CRUD na tabeli notatek.
     */
    public abstract NoteDao noteDao();

    /**
     * @brief Unikalna instancja bazy danych. Słowo kluczowe volatile gwarantuje
     * widoczność zmian tej zmiennej dla wszystkich wątków.
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * @brief Zwraca jedyną instancję bazy danych AppDatabase (Singleton).
     * * Metoda wykorzystuje mechanizm "double-checked locking", aby bezpiecznie
     * utworzyć instancję w środowisku wielowątkowym. Jeśli baza jeszcze nie istnieje,
     * zostaje zbudowana z nazwą "medisport_db".
     * * @param context Kontekst aplikacji potrzebny do zainicjalizowania bazy danych.
     * Zaleca się przekazanie context.getApplicationContext().
     * @return Pojedyncza instancja klasy AppDatabase.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "medisport_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}