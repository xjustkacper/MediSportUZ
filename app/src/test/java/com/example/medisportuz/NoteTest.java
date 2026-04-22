package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 * @brief Zestaw testów jednostkowych dla modelu danych Note (Notatka).
 * * Weryfikuje poprawność tworzenia struktury obiektu, zdolność do bezpiecznego
 * przechowywania stanu wewnętrznego (danych) oraz jego późniejszego odczytu.
 */
public class NoteTest {
    /**
     * @brief Testuje podstawowy cykl przypisywania danych do encji Note.
     * Upewnia się, że wszystkie pola klasy (id, tytuł, treść, kategoria, data)
     * prawidłowo przyjmują i zwracają wyznaczone typy zmiennych.
     */
    @Test
    public void testNoteCreation() {
        // --- GIVEN (Zakładając utworzenie nowej, pustej instancji notatki w pamięci) ---
        Note note = new Note();
        // --- WHEN (Kiedy zainicjalizujemy wszystkie pola modelu konkretnymi danymi testowymi) ---
        note.id = 1;
        note.title = "Test Title";
        note.content = "Test Content";
        note.category = "trening";
        note.createdAt = 123456789L;
        // --- THEN (Wtedy oczekujemy, że stan obiektu został trwale zmieniony,
        // a odczytane wartości będą dokładnie takie same, jak te zadeklarowane wyżej) ---
        assertEquals(1, note.id);
        assertEquals("Test Title", note.title);
        assertEquals("Test Content", note.content);
        assertEquals("trening", note.category);
        assertEquals(123456789L, note.createdAt);
    }
}