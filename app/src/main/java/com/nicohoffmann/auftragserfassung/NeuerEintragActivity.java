package com.nicohoffmann.auftragserfassung;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity zum Erstellen oder Bearbeiten eines Arbeitseintrags.
 * Ermöglicht die Eingabe von Datum, Arbeitszeiten, Baustelle und Beschreibung.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class NeuerEintragActivity extends AppCompatActivity {

    // ====================================
    // Static Variables
    // ====================================

    private static final DateTimeFormatter ZEIT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int PAUSE_LANG = 60;
    private static final int PAUSE_MITTEL = 45;
    private static final int PAUSE_KURZ = 30;
    private static final long GRENZE_LANG = 600;
    private static final long GRENZE_MITTEL = 540;
    private static final long GRENZE_KURZ = 360;
    public static final String EXTRA_EINTRAG_ID = "eintrag_id";

    // ====================================
    // Instance Variables
    // ====================================

    private TextView textViewPause;
    private Button buttonZeitVon;
    private Button buttonZeitBis;
    private Spinner spinnerBaustelle;
    private EditText editTextBeschreibung;

    private LocalTime zeitVon;
    private LocalTime zeitBis;
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

        TextView textViewDatum = findViewById(R.id.textViewDatum);
        textViewPause = findViewById(R.id.textViewPause);
        buttonZeitVon = findViewById(R.id.buttonZeitVon);
        buttonZeitBis = findViewById(R.id.buttonZeitBis);
        Button buttonSpeichern = findViewById(R.id.buttonSpeichern);
        spinnerBaustelle = findViewById(R.id.spinnerBaustelle);
        editTextBeschreibung = findViewById(R.id.editTextBeschreibung);

        textViewDatum.setText(LocalDate.now().format(DATUM_FORMAT));

        ladeBaustellen();

        buttonZeitVon.setOnClickListener(v -> zeigZeitPicker(true));
        buttonZeitBis.setOnClickListener(v -> zeigZeitPicker(false));
        buttonSpeichern.setOnClickListener(v -> speichereEintrag());

        // Bearbeitungsmodus: Eintrag-ID aus Intent lesen
        int eintragId = getIntent().getIntExtra(EXTRA_EINTRAG_ID, -1);
        if (eintragId != -1) {
            ladeEintragZumBearbeiten(eintragId);
        }
    }

    private void ladeEintragZumBearbeiten(int id) {
        executor.execute(() -> {
            Eintrag eintrag = db.eintragDao().getById(id);
            if (eintrag == null) return;
            zuBearbeitenderEintrag = eintrag;

            runOnUiThread(() -> {
                zeitVon = LocalTime.parse(eintrag.getZeitVon(), ZEIT_FORMAT);
                zeitBis = LocalTime.parse(eintrag.getZeitBis(), ZEIT_FORMAT);
                buttonZeitVon.setText(eintrag.getZeitVon());
                buttonZeitBis.setText(eintrag.getZeitBis());
                editTextBeschreibung.setText(eintrag.getBeschreibung());
                textViewPause.setText(getString(R.string.label_pause,
                        berechnePauseMinuten(zeitVon, zeitBis)));

                // Baustelle vorauswählen
                for (int i = 0; i < baustellenListe.size(); i++) {
                    if (baustellenListe.get(i).getId() == eintrag.getBaustelleId()) {
                        spinnerBaustelle.setSelection(i);
                        break;
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

            // Wenn Bearbeitung: nach Laden der Baustellen nochmals vorauswählen
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
        if (zeitVon == null || zeitBis == null) {
            Toast.makeText(this, getString(R.string.fehler_zeit), Toast.LENGTH_SHORT).show();
            return;
        }

        int position = spinnerBaustelle.getSelectedItemPosition();
        Integer baustelleId = baustellenListe.isEmpty() ? null : baustellenListe.get(position).getId();

        if (zuBearbeitenderEintrag != null) {
            // Bearbeitung: bestehenden Eintrag updaten
            zuBearbeitenderEintrag.setZeitVon(zeitVon.format(ZEIT_FORMAT));
            zuBearbeitenderEintrag.setZeitBis(zeitBis.format(ZEIT_FORMAT));
            zuBearbeitenderEintrag.setBeschreibung(editTextBeschreibung.getText().toString().trim());
            zuBearbeitenderEintrag.setPauseMinuten(berechnePauseMinuten(zeitVon, zeitBis));
            zuBearbeitenderEintrag.setBaustelleId(baustelleId);

            executor.execute(() -> {
                db.eintragDao().update(zuBearbeitenderEintrag);
                runOnUiThread(this::finish);
            });
        } else {
            // Neu anlegen
            Eintrag eintrag = new Eintrag();
            eintrag.setDatum(LocalDate.now().format(DATUM_FORMAT));
            eintrag.setZeitVon(zeitVon.format(ZEIT_FORMAT));
            eintrag.setZeitBis(zeitBis.format(ZEIT_FORMAT));
            eintrag.setBeschreibung(editTextBeschreibung.getText().toString().trim());
            eintrag.setPauseMinuten(berechnePauseMinuten(zeitVon, zeitBis));
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
            textViewPause.setText(getString(R.string.label_pause, berechnePauseMinuten(zeitVon, zeitBis)));
        }, jetzt.getHour(), jetzt.getMinute(), true).show();
    }

    private int berechnePauseMinuten(LocalTime von, LocalTime bis) {
        if (von == null || bis == null) return 0;
        long minuten = Duration.between(von, bis).toMinutes();
        if (minuten > GRENZE_LANG) return PAUSE_LANG;
        if (minuten > GRENZE_MITTEL) return PAUSE_MITTEL;
        if (minuten > GRENZE_KURZ) return PAUSE_KURZ;
        return 0;
    }
}
