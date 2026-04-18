package com.example.medisportuz;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NoteTest {

    @Test
    public void testNoteCreation() {
        Note note = new Note();
        note.id = 1;
        note.title = "Test Title";
        note.content = "Test Content";
        note.category = "trening";
        note.createdAt = 123456789L;

        assertEquals(1, note.id);
        assertEquals("Test Title", note.title);
        assertEquals("Test Content", note.content);
        assertEquals("trening", note.category);
        assertEquals(123456789L, note.createdAt);
    }
}