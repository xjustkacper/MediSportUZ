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

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onEdit(Note note);
        void onShare(Note note);
        void onDelete(Note note);
    }

    private List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public NotesAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

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

    @Override
    public int getItemCount() { return notes.size(); }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, category, date, btnShare, btnEdit, btnDelete;

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