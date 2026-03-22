package com.nicohoffmann.auftragserfassung;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity zum Erstellen oder Bearbeiten eines Arbeitseintrags.
 * Ermöglicht die Eingabe von Datum, Baustelle und Beschreibung.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class NeuerEintragActivity extends AppCompatActivity {

    // ====================================
    // Static Variables
    // ====================================

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String EXTRA_EINTRAG_ID = "eintrag_id";
    public static final String EXTRA_DATUM = "datum";

    // ====================================
    // Instance Variables
    // ====================================

    private Button buttonDatum;
    private Spinner spinnerBaustelle;
    private EditText editTextBeschreibung;

    private LocalDate gewaehltesDatum;
    private List<Baustelle> baustellenListe = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    private Eintrag zuBearbeitenderEintrag = null;

    // ====================================
    // Business Logic Methods
    // ====================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neuer_eintrag);

        db = AppDatabase.getInstance(this);

        buttonDatum = findViewById(R.id.buttonDatum);
        Button buttonSpeichern = findViewById(R.id.buttonSpeichern);
        spinnerBaustelle = findViewById(R.id.spinnerBaustelle);
        editTextBeschreibung = findViewById(R.id.editTextBeschreibung);

        // ✅ Zurück-FAB
        FloatingActionButton fabZurueck = findViewById(R.id.fabZurueck);
        fabZurueck.setOnClickListener(v -> finish());

        gewaehltesDatum = LocalDate.now();
        buttonDatum.setText(gewaehltesDatum.format(DATUM_FORMAT));
        buttonDatum.setOnClickListener(v -> zeigDatumPicker());

        ladeBaustellen();

        buttonSpeichern.setOnClickListener(v -> speichereEintrag());

        int eintragId = getIntent().getIntExtra(EXTRA_EINTRAG_ID, -1);
        if (eintragId != -1) {
            ladeEintragZumBearbeiten(eintragId);
        }
        String extraDatum = getIntent().getStringExtra(EXTRA_DATUM);
        if (extraDatum != null) {
            gewaehltesDatum = LocalDate.parse(extraDatum, DATUM_FORMAT);
            buttonDatum.setText(extraDatum);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void ladeEintragZumBearbeiten(int id) {
        executor.execute(() -> {
            Eintrag eintrag = db.eintragDao().getById(id);
            if (eintrag == null) return;
            zuBearbeitenderEintrag = eintrag;

            runOnUiThread(() -> {
                gewaehltesDatum = LocalDate.parse(eintrag.getDatum(), DATUM_FORMAT);
                buttonDatum.setText(eintrag.getDatum());
                editTextBeschreibung.setText(eintrag.getBeschreibung());

                if (eintrag.getBaustelleId() != null) {
                    for (int i = 0; i < baustellenListe.size(); i++) {
                        if (baustellenListe.get(i).getId() == eintrag.getBaustelleId()) {
                            spinnerBaustelle.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });
    }

    private void ladeBaustellen() {
        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            baustellenListe = baustellen;
            ArrayAdapter<Baustelle> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, baustellen);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBaustelle.setAdapter(adapter);

            for (int i = 0; i < baustellen.size(); i++) {
                if (baustellen.get(i).isFavorit()) {
                    spinnerBaustelle.setSelection(i);
                    break;
                }
            }

            if (zuBearbeitenderEintrag != null && zuBearbeitenderEintrag.getBaustelleId() != null) {
                for (int i = 0; i < baustellen.size(); i++) {
                    if (baustellen.get(i).getId() == zuBearbeitenderEintrag.getBaustelleId()) {
                        spinnerBaustelle.setSelection(i);
                        break;
                    }
                }
            }
        });
    }

    private void speichereEintrag() {
        int position = spinnerBaustelle.getSelectedItemPosition();
        Integer baustelleId = baustellenListe.isEmpty() ? null : baustellenListe.get(position).getId();

        if (zuBearbeitenderEintrag != null) {
            zuBearbeitenderEintrag.setDatum(gewaehltesDatum.format(DATUM_FORMAT));
            zuBearbeitenderEintrag.setBeschreibung(editTextBeschreibung.getText().toString().trim());
            zuBearbeitenderEintrag.setBaustelleId(baustelleId);

            executor.execute(() -> {
                db.eintragDao().update(zuBearbeitenderEintrag);
                runOnUiThread(this::finish);
            });
        } else {
            Eintrag eintrag = new Eintrag();
            eintrag.setDatum(gewaehltesDatum.format(DATUM_FORMAT));
            eintrag.setBeschreibung(editTextBeschreibung.getText().toString().trim());
            eintrag.setBaustelleId(baustelleId);

            executor.execute(() -> {
                db.eintragDao().insert(eintrag);
                runOnUiThread(this::finish);
            });
        }
    }

    // ====================================
    // Utility Methods
    // ====================================

    private void zeigDatumPicker() {
        new DatePickerDialog(this, (view, jahr, monat, tag) -> {
            gewaehltesDatum = LocalDate.of(jahr, monat + 1, tag);
            buttonDatum.setText(gewaehltesDatum.format(DATUM_FORMAT));
        },
                gewaehltesDatum.getYear(),
                gewaehltesDatum.getMonthValue() - 1,
                gewaehltesDatum.getDayOfMonth()).show();
    }
}
