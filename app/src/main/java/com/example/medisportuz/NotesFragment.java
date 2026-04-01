package com.example.medisportuz;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesFragment extends Fragment implements NotesAdapter.OnNoteClickListener {

    // SPP UUID — standardowy dla Bluetooth Classic
    private static final UUID BT_SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private NotesAdapter adapter;
    private NoteRepository repository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int BT_PERMISSION_REQUEST = 1002;
    private Note pendingShareNote;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        repository = new NoteRepository(requireContext());

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.notesRecyclerView);
        adapter = new NotesAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Obserwuj zmiany w bazie
        repository.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
        });

        // FAB — dodaj notatkę
        FloatingActionButton fab = view.findViewById(R.id.notesAddFab);
        fab.setOnClickListener(v -> showNoteDialog(null));

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == BT_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingShareNote != null) {
                shareViaBluetooth(pendingShareNote);
                pendingShareNote = null;
            }
        } else if (requestCode == BT_PERMISSION_REQUEST) {
            Toast.makeText(getContext(), "Brak uprawnień Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
    // ────────────────────────────────────────────────────────────────────────
    //  Dialog tworzenia / edycji notatki
    // ────────────────────────────────────────────────────────────────────────

    private void showNoteDialog(@Nullable Note existingNote) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_note, null);

        EditText etTitle   = dialogView.findViewById(R.id.dialogNoteTitle);
        EditText etContent = dialogView.findViewById(R.id.dialogNoteContent);
        Spinner  spCategory = dialogView.findViewById(R.id.dialogNoteCategory);

        String[] categories = {"trening", "dieta", "inne"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // Tryb edycji — wypełnij istniejące dane
        if (existingNote != null) {
            etTitle.setText(existingNote.title);
            etContent.setText(existingNote.content);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existingNote.category)) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existingNote == null ? "Nowa notatka" : "Edytuj notatkę")
                .setView(dialogView)
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String title   = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String cat     = spCategory.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Tytuł nie może być pusty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ← bezpośrednio if/else, bez żadnego wrappera
                    if (existingNote == null) {
                        Note note = new Note();
                        note.title     = title;
                        note.content   = content;
                        note.category  = cat;
                        note.createdAt = System.currentTimeMillis();
                        repository.insert(note);
                    } else {
                        existingNote.title    = title;
                        existingNote.content  = content;
                        existingNote.category = cat;
                        repository.update(existingNote);
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Akcje z adaptera
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void onEdit(Note note) {
        showNoteDialog(note);
    }

    @Override
    public void onDelete(Note note) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Usuń notatkę")
                .setMessage("Czy na pewno chcesz usunąć \"" + note.title + "\"?")
                .setPositiveButton("Usuń", (d, w) ->
                        repository.delete(note))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    @Override
    public void onShare(Note note) {
        // Pokaż wybór: Bluetooth lub systemowy share
        new AlertDialog.Builder(requireContext())
                .setTitle("Udostępnij notatkę")
                .setItems(new String[]{"Bluetooth", "Inne aplikacje"}, (dialog, which) -> {
                    if (which == 0) shareViaBluetooth(note);
                    else            shareViaSystem(note);
                })
                .show();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Udostępnianie przez system (Intent)
    // ────────────────────────────────────────────────────────────────────────

    private void shareViaSystem(Note note) {
        String text = note.title + "\n\n" + note.content;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Udostępnij przez..."));
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Udostępnianie przez Bluetooth Classic (SPP)
    // ────────────────────────────────────────────────────────────────────────

    private void shareViaBluetooth(Note note) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Toast.makeText(getContext(), "Urządzenie nie obsługuje Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!btAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Włącz Bluetooth i spróbuj ponownie", Toast.LENGTH_SHORT).show();
            return;
        }

        // ← Sprawdź uprawnienie BLUETOOTH_CONNECT (wymagane od Android 12)
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BT_PERMISSION_REQUEST);
            return;
        }
        // Pobierz sparowane urządzenia
        Set<BluetoothDevice> paired = btAdapter.getBondedDevices();
        if (paired.isEmpty()) {
            Toast.makeText(getContext(),
                    "Brak sparowanych urządzeń Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lista sparowanych urządzeń do wyboru
        String[] names    = new String[paired.size()];
        BluetoothDevice[] devices = new BluetoothDevice[paired.size()];
        int i = 0;
        for (BluetoothDevice device : paired) {
            names[i]   = device.getName();
            devices[i] = device;
            i++;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Wybierz urządzenie")
                .setItems(names, (dialog, which) ->
                        sendNoteViaBluetooth(devices[which], note))
                .show();
    }

    private void sendNoteViaBluetooth(BluetoothDevice device, Note note) {
        // Sprawdź uprawnienie raz na początku — obejmuje getName() i connect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Brak uprawnień Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Od tego miejsca getName() i createRfcomm... są już bezpieczne
        String deviceName = device.getName();
        String message = "=== " + note.title + " ===\n"
                + "Kategoria: " + note.category + "\n\n"
                + note.content;

        Toast.makeText(getContext(), "Łączenie z " + deviceName + "...",
                Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(BT_SPP_UUID);
                socket.connect();

                OutputStream out = socket.getOutputStream();
                out.write(message.getBytes("UTF-8"));
                out.flush();

                mainHandler.post(() ->
                        Toast.makeText(getContext(),
                                "Wysłano do " + deviceName, Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                mainHandler.post(() ->
                        Toast.makeText(getContext(),
                                "Błąd Bluetooth: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (IOException ignored) {}
                }
            }
        }).start();
    }
}