package com.example.medisportuz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * @brief Adapter dla listy (RecyclerView) obsługujący wyświetlanie notatek.
 * * Klasa ta odpowiada za przekształcenie obiektów modelu Note na reprezentację
 * wizualną (pojedyncze kafelki/elementy listy) oraz za obsługę zdarzeń interakcji
 * z poszczególnymi elementami.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    /**
     * @brief Interfejs definiujący akcje użytkownika na pojedynczej notatce.
     * * Pozwala na oddelegowanie obsługi zdarzeń kliknięcia (edycja, udostępnianie,
     * usuwanie) z adaptera do nadrzędnego komponentu (np. Fragmentu).
     */
    public interface OnNoteClickListener {
        /**
         * @brief Wywoływana po kliknięciu przycisku edycji.
         * @param note Obiekt notatki do edycji.
         */
        void onEdit(Note note);
        /**
         * @brief Wywoływana po kliknięciu przycisku udostępniania.
         * @param note Obiekt notatki do udostępnienia.
         */
        void onShare(Note note);
        /**
         * @brief Wywoływana po kliknięciu przycisku usuwania.
         * @param note Obiekt notatki do usunięcia.
         */
        void onDelete(Note note);
    }
    /**
     * @brief Aktualna lista notatek do wyświetlenia.
     */
    private List<Note> notes = new ArrayList<>();
    /**
     * @brief Referencja do nasłuchiwacza zdarzeń kliknięcia.
     */
    private final OnNoteClickListener listener;
    /**
     * @brief Formatter daty służący do konwersji znacznika czasu na czytelny tekst.
     */
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    /**
     * @brief Konstruktor adaptera notatek.
     * @param listener Obiekt implementujący interfejs OnNoteClickListener,
     * który obsłuży akcje na elementach listy.
     */
    public NotesAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }
    /**
     * @brief Aktualizuje listę notatek w adapterze i odświeża widok.
     * @param notes Nowa lista obiektów Note do wyświetlenia.
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }
    /**
     * @brief Tworzy nowy obiekt NoteViewHolder i inicjalizuje jego widok.
     * * Metoda wywoływana przez RecyclerView tylko wtedy, gdy potrzebny jest nowy
     * widok elementu (gdy na ekranie pojawia się nowy element, a nie ma wolnych
     * widoków do ponownego użycia).
     * * @param parent Grupa widoków (ViewGroup), do której zostanie dołączony nowy widok.
     * @param viewType Typ widoku nowego elementu.
     * @return Nowy obiekt NoteViewHolder zawierający wygenerowany widok.
     */
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }
    /**
     * @brief Wiąże dane z określonym obiektem NoteViewHolder.
     * * Metoda ta pobiera notatkę z podanej pozycji i aktualizuje zawartość
     * komponentów interfejsu wewnątrz ViewHolder (teksty, data) oraz podpina
     * akcje pod przyciski.
     * * @param holder ViewHolder, który powinien zostać zaktualizowany.
     * @param position Pozycja elementu na liście notatek.
     */
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.title);
        holder.content.setText(note.content);
        holder.category.setText(note.category);
        holder.date.setText(sdf.format(new Date(note.createdAt)));

        holder.btnShare.setOnClickListener(v -> listener.onShare(note));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(note));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(note));
    }
    /**
     * @brief Zwraca całkowitą liczbę elementów na liście.
     * @return Rozmiar aktualnej listy notatek.
     */
    @Override
    public int getItemCount() { return notes.size(); }
    /**
     * @brief Klasa wewnętrzna przechowująca odniesienia do komponentów UI pojedynczego elementu listy.
     * * Wzorzec ViewHolder minimalizuje liczbę wywołań findViewById(),
     * co znacznie poprawia wydajność przewijania list w RecyclerView.
     */
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        /** Elementy tekstowe i przyciski akcji (tutaj zaimplementowane jako TextView z obsługą kliknięć) */
        TextView title, content, category, date, btnShare, btnEdit, btnDelete;
        /**
         * @brief Konstruktor ViewHoldera, wyszukujący i przypisujący widoki.
         * @param item Główny widok (layout) pojedynczego elementu listy.
         */
        NoteViewHolder(View item) {
            super(item);
            title    = item.findViewById(R.id.noteTitle);
            content  = item.findViewById(R.id.noteContent);
            category = item.findViewById(R.id.noteCategory);
            date     = item.findViewById(R.id.noteDate);
            btnShare  = item.findViewById(R.id.noteBtnShare);
            btnEdit   = item.findViewById(R.id.noteBtnEdit);
            btnDelete = item.findViewById(R.id.noteBtnDelete);
        }
    }
}