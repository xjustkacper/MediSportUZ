package com.example.medisportuz;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repozytorium izoluje źródło danych od UI.
 * Gdy przyjdzie czas na Firebase — zmienisz tylko tę klasę,
 * Fragment i ViewModel pozostają bez zmian.
 */
public class NoteRepository {

    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Context context) {
        noteDao = AppDatabase.getInstance(context).noteDao();
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
        // Firebase: return firestoreNotesLiveData();
    }

    public void insert(Note note) {
        executor.execute(() -> noteDao.insert(note));
        // Firebase: db.collection("notes").add(note);
    }

    public void update(Note note) {
        executor.execute(() -> noteDao.update(note));
        // Firebase: db.collection("notes").document(note.id).set(note);
    }

    public void delete(Note note) {
        executor.execute(() -> noteDao.delete(note));
        // Firebase: db.collection("notes").document(note.id).delete();
    }
}