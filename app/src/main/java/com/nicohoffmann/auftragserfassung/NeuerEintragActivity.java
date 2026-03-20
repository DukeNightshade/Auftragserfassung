package com.nicohoffmann.auftragserfassung;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NeuerEintragActivity extends AppCompatActivity {

    private TextView textViewDatum, textViewPause;
    private Button buttonZeitVon, buttonZeitBis, buttonSpeichern;
    private Spinner spinnerBaustelle;
    private EditText editTextBeschreibung;

    private LocalTime zeitVon, zeitBis;
    private List<Baustelle> baustellenListe = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    private static final DateTimeFormatter ZEIT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neuer_eintrag);

        db = AppDatabase.getInstance(this);

        textViewDatum = findViewById(R.id.textViewDatum);
        textViewPause = findViewById(R.id.textViewPause);
        buttonZeitVon = findViewById(R.id.buttonZeitVon);
        buttonZeitBis = findViewById(R.id.buttonZeitBis);
        buttonSpeichern = findViewById(R.id.buttonSpeichern);
        spinnerBaustelle = findViewById(R.id.spinnerBaustelle);
        editTextBeschreibung = findViewById(R.id.editTextBeschreibung);

        textViewDatum.setText(LocalDate.now().format(DATUM_FORMAT));

        ladeBaustellen();

        buttonZeitVon.setOnClickListener(v -> zeigZeitPicker(true));
        buttonZeitBis.setOnClickListener(v -> zeigZeitPicker(false));
        buttonSpeichern.setOnClickListener(v -> speichereEintrag());
    }

    private void ladeBaustellen() {
        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            baustellenListe = baustellen;
            ArrayAdapter<Baustelle> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, baustellen);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBaustelle.setAdapter(adapter);

            // Favorit vorauswählen
            for (int i = 0; i < baustellen.size(); i++) {
                if (baustellen.get(i).isFavorit) {
                    spinnerBaustelle.setSelection(i);
                    break;
                }
            }
        });
    }

    private void zeigZeitPicker(boolean istVon) {
        LocalTime jetzt = LocalTime.now();
        new TimePickerDialog(this, (view, stunde, minute) -> {
            LocalTime zeit = LocalTime.of(stunde, minute);
            if (istVon) {
                zeitVon = zeit;
                buttonZeitVon.setText(zeit.format(ZEIT_FORMAT));
            } else {
                zeitBis = zeit;
                buttonZeitBis.setText(zeit.format(ZEIT_FORMAT));
            }
            berechnePause();
        }, jetzt.getHour(), jetzt.getMinute(), true).show();
    }

    private void berechnePause() {
        if (zeitVon == null || zeitBis == null) return;
        long minuten = java.time.Duration.between(zeitVon, zeitBis).toMinutes();
        int pause = 0;
        if (minuten > 600) pause = 60;
        else if (minuten > 540) pause = 45;
        else if (minuten > 360) pause = 30;
        textViewPause.setText(getString(R.string.label_pause, pause));
    }

    private void speichereEintrag() {
        if (zeitVon == null || zeitBis == null) {
            Toast.makeText(this, getString(R.string.fehler_zeit), Toast.LENGTH_SHORT).show();
            return;
        }

        Eintrag eintrag = new Eintrag();
        eintrag.datum = LocalDate.now().format(DATUM_FORMAT);
        eintrag.zeitVon = zeitVon.format(ZEIT_FORMAT);
        eintrag.zeitBis = zeitBis.format(ZEIT_FORMAT);
        eintrag.beschreibung = editTextBeschreibung.getText().toString().trim();

        long minuten = java.time.Duration.between(zeitVon, zeitBis).toMinutes();
        if (minuten > 600) eintrag.pauseMinuten = 60;
        else if (minuten > 540) eintrag.pauseMinuten = 45;
        else if (minuten > 360) eintrag.pauseMinuten = 30;

        int position = spinnerBaustelle.getSelectedItemPosition();
        if (!baustellenListe.isEmpty()) {
            eintrag.baustelleId = baustellenListe.get(position).id;
        }

        executor.execute(() -> {
            db.eintragDao().insert(eintrag);
            runOnUiThread(this::finish);
        });
    }
}
