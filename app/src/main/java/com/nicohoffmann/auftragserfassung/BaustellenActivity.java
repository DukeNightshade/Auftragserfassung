package com.nicohoffmann.auftragserfassung;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class BaustellenActivity extends AppCompatActivity {

    private BaustellenAdapter adapter;
    private AppDatabase db;
    private final java.util.concurrent.ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baustellen);

        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBaustellen);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BaustellenAdapter(new ArrayList<>(), baustelle -> {
            executor.execute(() -> {
                baustelle.isFavorit = !baustelle.isFavorit;
                db.baustelleDao().update(baustelle);
            });
        });

        recyclerView.setAdapter(adapter);

        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            adapter.setBaustellen(baustellen);
        });

        FloatingActionButton fab = findViewById(R.id.fabBaustelleHinzufuegen);
        fab.setOnClickListener(v -> zeigeHinzufuegenDialog());
    }

    private void zeigeHinzufuegenDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_baustelle_hinzufuegen, null);
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextAdresse = dialogView.findViewById(R.id.editTextAdresse);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_titel_baustelle))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.button_speichern), (dialog, which) -> {
                    String name = editTextName.getText().toString().trim();
                    String adresse = editTextAdresse.getText().toString().trim();

                    if (!name.isEmpty()) {
                        Baustelle baustelle = new Baustelle(name, adresse, false);
                        executor.execute(() -> db.baustelleDao().insert(baustelle));
                    }
                })
                .setNegativeButton(getString(R.string.button_abbrechen), null)
                .show();
    }
}
