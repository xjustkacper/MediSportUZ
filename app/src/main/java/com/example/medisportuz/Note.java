package com.example.medisportuz;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
/**
 * @brief Model danych (encja) reprezentujący pojedynczą notatkę.
 * * Klasa ta jest mapowana przez bibliotekę Room na tabelę o nazwie "notes"
 * w lokalnej bazie danych SQLite. Przechowuje podstawowe informacje o notatce,
 * takie jak tytuł, treść, kategoria oraz czas utworzenia.
 */
@Entity(tableName = "notes")
public class Note {
    /**
     * @brief Unikalny identyfikator notatki (Klucz główny).
     * Wartość ta jest automatycznie generowana (inkrementowana) przez bazę danych
     * przy dodawaniu nowego rekordu.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    /**
     * @brief Tytuł notatki nadany przez użytkownika.
     */
    public String title;
    /**
     * @brief Główna treść (opis) notatki.
     */
    public String content;
    /**
     * @brief Kategoria, do której przypisana jest notatka.
     * Zazwyczaj przyjmuje jedną z predefiniowanych wartości, np.: "trening", "dieta", "inne".
     */
    public String category;   // "trening", "dieta", "inne"
    /**
     * @brief Dokładny czas utworzenia notatki.
     * Przechowywany jako znacznik czasu (timestamp) w standardzie Unix, wyrażony
     * w milisekundach (np. wynik metody System.currentTimeMillis()).
     */
    public long createdAt;    // timestamp milisekund
}