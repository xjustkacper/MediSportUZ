package com.example.medisportuz;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @brief Klasa repozytorium zarządzająca dostępem do danych notatek.
 * * Repozytorium implementuje wzorzec architektoniczny, który izoluje źródło danych
 * (np. lokalną bazę Room) od warstwy interfejsu użytkownika (UI). Służy jako "pojedyncze
 * źródło prawdy" dla danych. Dzięki takiemu podejściu, gdy w przyszłości zajdzie potrzeba
 * migracji na inne rozwiązanie (np. Firebase Firestore), zmiany będą wymagały modyfikacji
 * wyłącznie tej klasy, podczas gdy Fragmenty i ViewModele pozostaną nietknięte.
 */
public class NoteRepository {
    /**
     * @brief Obiekt dostępu do danych (DAO) dla lokalnej bazy Room.
     */
    private final NoteDao noteDao;
    /**
     * @brief Pula wątków składająca się z pojedynczego wątku w tle.
     * Używana do asynchronicznego wykonywania operacji zapisu/usuwania (Insert, Update, Delete)
     * poza głównym wątkiem aplikacji (UI thread), co zapobiega zablokowaniu interfejsu.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * @brief Konstruktor repozytorium.
     * * Inicjalizuje połączenie z lokalną bazą danych i pobiera obiekt NoteDao.
     * * @param context Kontekst aplikacji wymagany do utworzenia/pobrania instancji AppDatabase.
     */
    public NoteRepository(Context context) {
        noteDao = AppDatabase.getInstance(context).noteDao();
    }
    /**
     * @brief Pobiera z bazy strumień danych zawierający wszystkie notatki.
     * * Zwraca obiekt LiveData, który automatycznie odświeży widok w przypadku
     * jakichkolwiek zmian w tabeli notatek.
     * * @return Obiekt LiveData z listą notatek.
     */
    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
        // Firebase: return firestoreNotesLiveData();
    }
    /**
     * @brief Zapisuje nową notatkę w bazie danych.
     * * Operacja jest zlecana do wykonania w tle za pomocą ExecutorService.
     * * @param note Obiekt nowej notatki do zapisania.
     */
    public void insert(Note note) {
        executor.execute(() -> noteDao.insert(note));
        // Firebase: db.collection("notes").add(note);
    }
    /**
     * @brief Aktualizuje istniejącą notatkę w bazie danych.
     * * Operacja jest zlecana do wykonania w tle za pomocą ExecutorService.
     * * @param note Obiekt notatki zawierający zaktualizowane dane.
     */
    public void update(Note note) {
        executor.execute(() -> noteDao.update(note));
        // Firebase: db.collection("notes").document(note.id).set(note);
    }
    /**
     * @brief Usuwa wskazaną notatkę z bazy danych.
     * * Operacja jest zlecana do wykonania w tle za pomocą ExecutorService.
     * * @param note Obiekt notatki przeznaczony do usunięcia.
     */
    public void delete(Note note) {
        executor.execute(() -> noteDao.delete(note));
        // Firebase: db.collection("notes").document(note.id).delete();
    }
}