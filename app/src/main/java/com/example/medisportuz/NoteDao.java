package com.example.medisportuz;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
/**
 * @brief Interfejs Data Access Object (DAO) dla encji Note.
 * * Klasa ta definiuje metody służące do interakcji z tabelą "notes" w bazie danych.
 * Biblioteka Room automatycznie generuje implementację tego interfejsu podczas
 * budowania projektu, wykonując odpowiednie zapytania SQL w tle.
 */
@Dao
public interface NoteDao {
    /**
     * @brief Wstawia nową notatkę do bazy danych.
     * * Jeśli w encji zdefiniowano auto-generowanie klucza głównego (id),
     * zostanie on automatycznie przypisany nowemu rekordowi.
     * * @param note Obiekt notatki do zapisania.
     */
    @Insert
    void insert(Note note);
    /**
     * @brief Aktualizuje istniejącą notatkę w bazie danych.
     * * Wyszukuje w tabeli rekord o takim samym kluczu głównym (id) jak w przekazanym
     * obiekcie i podmienia jego zawartość na nową.
     * * @param note Zmodyfikowany obiekt notatki.
     */
    @Update
    void update(Note note);
    /**
     * @brief Usuwa wskazaną notatkę z bazy danych.
     * * Identyfikuje rekord do usunięcia na podstawie klucza głównego (id) z przekazanego obiektu.
     * * @param note Obiekt notatki przeznaczony do usunięcia.
     */
    @Delete
    void delete(Note note);
    /**
     * @brief Pobiera wszystkie notatki z bazy danych, posortowane od najnowszej do najstarszej.
     * * Zapytanie wykorzystuje język SQL do wyciągnięcia wszystkich rekordów i posortowania ich
     * malejąco (DESC) względem pola `createdAt`. Zastosowanie opakowania `LiveData` sprawia,
     * że zapytanie jest asynchroniczne (działa w tle), a każdy komponent obserwujący te dane
     * (np. Fragment lub Activity) zostanie automatycznie powiadomiony o jakichkolwiek zmianach
     * w tabeli "notes" (np. po dodaniu lub usunięciu notatki).
     * * @return Obiekt LiveData zawierający listę wszystkich notatek.
     */
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    LiveData<List<Note>> getAllNotes();
}