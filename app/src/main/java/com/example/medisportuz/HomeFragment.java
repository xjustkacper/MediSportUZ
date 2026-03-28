package com.example.medisportuz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView greeting = view.findViewById(R.id.homeGreeting);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Najpierw ustaw awaryjnie z obiektu Auth (zanim dane spłyną z bazy)
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                greeting.setText(getString(R.string.home_greeting, user.getDisplayName()));
            }

            // Następnie pobierz najświeższe dane zFirestore!
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String imie = document.getString("imię");
                                if (imie != null && !imie.isEmpty()) {
                                    greeting.setText(getString(R.string.home_greeting, imie));
                                }
                            }
                        }
                    });
        }

        return view;
    }
}
