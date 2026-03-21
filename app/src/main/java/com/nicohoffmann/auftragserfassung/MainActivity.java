package com.nicohoffmann.auftragserfassung;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Hauptaktivität der Anwendung.
 * Zeigt Einträge in Wochen- oder Monatsansicht an.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    // ====================================
    // Static Variables
    // ====================================

    private static final int MAX_EINTRAEGE_PRO_TAG = 2;
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter MONAT_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);

    // ====================================
    // Instance Variables
    // ====================================

    private EintraegeAdapter eintraegeAdapter;
    private List<Eintrag> alleEintraege = new ArrayList<>();
    private List<Baustelle> alleBaustellen = new ArrayList<>();
    private LocalDate aktuellerMontag;
    private YearMonth aktuellerMonat;
    private boolean istMonatsansicht = false;

    private TextView textViewWochendatum;
    private TextView textViewKalenderwoche;
    private RecyclerView recyclerViewEintraege;
    private RecyclerView recyclerViewKalender;
    private LinearLayout layoutWochentagHeader;

    // ====================================
    // Business Logic Methods
    // ====================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aktuellerMontag = LocalDate.now().with(DayOfWeek.MONDAY);
        aktuellerMonat = YearMonth.now();

        AppDatabase db = AppDatabase.getInstance(this);

        recyclerViewEintraege = findViewById(R.id.recyclerViewEintraege);
        recyclerViewEintraege.setLayoutManager(new LinearLayoutManager(this));
        eintraegeAdapter = new EintraegeAdapter(new ArrayList<>());
        recyclerViewEintraege.setAdapter(eintraegeAdapter);

        // BottomSheet bei Eintrag-Klick
        eintraegeAdapter.setOnEintragClickListener(item -> {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.dialog_eintrag_detail,
                    dialog.findViewById(android.R.id.content),
                    false
            );

            ((TextView) view.findViewById(R.id.detailBaustelle)).setText(item.getBaustelleName());
            ((TextView) view.findViewById(R.id.detailDatum)).setText(getString(R.string.detail_datum, item.getEintrag().getDatum()));
            ((TextView) view.findViewById(R.id.detailZeit)).setText(getString(R.string.detail_zeit, item.getEintrag().getZeitVon(), item.getEintrag().getZeitBis()));
            ((TextView) view.findViewById(R.id.detailBeschreibung)).setText(item.getEintrag().getBeschreibung());

            dialog.setContentView(view);
            dialog.show();
        });

        recyclerViewKalender = findViewById(R.id.recyclerViewKalender);
        recyclerViewKalender.setLayoutManager(new GridLayoutManager(this, 7));

        layoutWochentagHeader = findViewById(R.id.layoutWochentagHeader);
        textViewWochendatum = findViewById(R.id.textViewWochendatum);
        textViewKalenderwoche = findViewById(R.id.textViewKalenderwoche);

        ImageButton buttonVorigeWoche = findViewById(R.id.buttonVorigeWoche);
        ImageButton buttonNaechsteWoche = findViewById(R.id.buttonNaechsteWoche);
        Button buttonAktuelleWoche = findViewById(R.id.buttonAktuelleWoche);

        buttonVorigeWoche.setOnClickListener(v -> navigiereZurueck());
        buttonNaechsteWoche.setOnClickListener(v -> navigiereVor());
        buttonAktuelleWoche.setOnClickListener(v -> zurueckZurAktuellenAnsicht());

        MaterialButtonToggleGroup toggleAnsicht = findViewById(R.id.toggleAnsicht);
        toggleAnsicht.check(R.id.buttonWochenansicht);
        toggleAnsicht.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                istMonatsansicht = checkedId == R.id.buttonMonatsansicht;
                aktualisiereAnsicht();
            }
        });

        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            alleBaustellen = baustellen;
            aktualisiereWochenliste(alleEintraege);
        });

        db.eintragDao().getAlleEintraege().observe(this, eintraege -> {
            alleEintraege = eintraege;
            aktualisiereAnsicht();
        });

        FloatingActionButton fabNeuerEintrag = findViewById(R.id.fabNeuerEintrag);
        fabNeuerEintrag.setOnClickListener(v ->
                startActivity(new Intent(this, NeuerEintragActivity.class))
        );

        FloatingActionButton fabBaustellen = findViewById(R.id.fabBaustellen);
        fabBaustellen.setOnClickListener(v ->
                startActivity(new Intent(this, BaustellenActivity.class))
        );

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    aktualisiereWochenliste(alleEintraege);
                } else {
                    List<Eintrag> gefiltert = alleEintraege.stream()
                            .filter(e -> e.getBeschreibung() != null &&
                                    e.getBeschreibung().toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toList());
                    aktualisiereWochenliste(gefiltert);
                }
                return true;
            }
        });

        aktualisiereAnsicht();
    }

    // ====================================
    // Utility Methods
    // ====================================

    private void navigiereZurueck() {
        if (istMonatsansicht) {
            aktuellerMonat = aktuellerMonat.minusMonths(1);
        } else {
            aktuellerMontag = aktuellerMontag.minusWeeks(1);
        }
        aktualisiereAnsicht();
    }

    private void navigiereVor() {
        if (istMonatsansicht) {
            aktuellerMonat = aktuellerMonat.plusMonths(1);
        } else {
            aktuellerMontag = aktuellerMontag.plusWeeks(1);
        }
        aktualisiereAnsicht();
    }

    private void zurueckZurAktuellenAnsicht() {
        aktuellerMontag = LocalDate.now().with(DayOfWeek.MONDAY);
        aktuellerMonat = YearMonth.now();
        aktualisiereAnsicht();
    }

    private void aktualisiereAnsicht() {
        if (istMonatsansicht) {
            recyclerViewEintraege.setVisibility(View.GONE);
            recyclerViewKalender.setVisibility(View.VISIBLE);
            layoutWochentagHeader.setVisibility(View.VISIBLE);
            aktualisiereMonatsinfo();
            aktualisiereMonatsliste();
        } else {
            recyclerViewEintraege.setVisibility(View.VISIBLE);
            recyclerViewKalender.setVisibility(View.GONE);
            layoutWochentagHeader.setVisibility(View.GONE);
            aktualisiereWochenliste(alleEintraege);
        }
    }

    private void aktualisiereMonatsinfo() {
        textViewWochendatum.setText(aktuellerMonat.format(MONAT_FORMAT));
        textViewKalenderwoche.setText("");
    }

    private void aktualisiereWocheninfo() {
        LocalDate sonntag = aktuellerMontag.plusDays(6);
        int kw = aktuellerMontag.get(WeekFields.of(Locale.GERMAN).weekOfWeekBasedYear());
        textViewWochendatum.setText(getString(
                R.string.label_wochendatum,
                aktuellerMontag.format(DATUM_FORMAT),
                sonntag.format(DATUM_FORMAT)
        ));
        textViewKalenderwoche.setText(getString(R.string.label_kalenderwoche, kw));
    }

    private void aktualisiereMonatsliste() {
        List<LocalDate> tage = new ArrayList<>();
        LocalDate ersterTag = aktuellerMonat.atDay(1);

        int startOffset = ersterTag.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < startOffset; i++) {
            tage.add(null);
        }

        for (int i = 1; i <= aktuellerMonat.lengthOfMonth(); i++) {
            tage.add(aktuellerMonat.atDay(i));
        }

        MonatsAdapter monatsAdapter = new MonatsAdapter(tage, alleEintraege, this::onKalenderTagGeklickt);
        recyclerViewKalender.setAdapter(monatsAdapter);
    }

    private void onKalenderTagGeklickt(LocalDate datum) {
        aktuellerMontag = datum.with(DayOfWeek.MONDAY);
        istMonatsansicht = false;

        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleAnsicht);
        toggle.check(R.id.buttonWochenansicht);
    }

    private void aktualisiereWochenliste(List<Eintrag> eintraege) {
        aktualisiereWocheninfo();

        List<EintraegeAdapter.ListItem> items = new ArrayList<>();

        Map<String, List<Eintrag>> gruppiertNachTag = eintraege.stream()
                .collect(Collectors.groupingBy(Eintrag::getDatum));

        for (int i = 0; i < 7; i++) {
            LocalDate tag = aktuellerMontag.plusDays(i);
            String datum = tag.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String tagName = tag.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.GERMAN);

            List<Eintrag> tagEintraege = Objects.requireNonNullElse(
                    gruppiertNachTag.get(datum), new ArrayList<>()
            );

            items.add(new EintraegeAdapter.HeaderItem(tagName));

            int anzahl = Math.min(tagEintraege.size(), MAX_EINTRAEGE_PRO_TAG);
            for (int j = 0; j < anzahl; j++) {
                Eintrag e = tagEintraege.get(j);
                String baustelleName = alleBaustellen.stream()
                        .filter(b -> b.getId() == (e.getBaustelleId() != null ? e.getBaustelleId() : -1))
                        .map(Baustelle::getName)
                        .findFirst()
                        .orElse("");
                items.add(new EintraegeAdapter.EintragItem(e, baustelleName));
            }

            if (tagEintraege.size() > MAX_EINTRAEGE_PRO_TAG) {
                items.add(new EintraegeAdapter.MehrItem());
            }
        }

        eintraegeAdapter.setItems(items);
    }
}
