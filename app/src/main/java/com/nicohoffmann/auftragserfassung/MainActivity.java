package com.nicohoffmann.auftragserfassung;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nicohoffmann.auftragserfassung.database.AppDatabase;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Hauptaktivität der Anwendung.
 * Zeigt die Einträge der aktuellen Woche gruppiert nach Wochentagen an
 * und ermöglicht die Suche nach Einträgen.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    // ====================================
    // Static Variables
    // ====================================

    private static final int MAX_EINTRAEGE_PRO_TAG = 2;

    // ====================================
    // Instance Variables
    // ====================================

    private EintraegeAdapter adapter;
    private List<Eintrag> alleEintraege = new ArrayList<>();
    private List<Baustelle> alleBaustellen = new ArrayList<>();

    // ====================================
    // Business Logic Methods
    // ====================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEintraege);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EintraegeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            alleBaustellen = baustellen;
            aktualisiereListe(alleEintraege);
        });

        db.eintragDao().getAlleEintraege().observe(this, eintraege -> {
            alleEintraege = eintraege;
            aktualisiereListe(eintraege);
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
                    aktualisiereListe(alleEintraege);
                } else {
                    List<Eintrag> gefiltert = alleEintraege.stream()
                            .filter(e -> e.getBeschreibung() != null &&
                                    e.getBeschreibung().toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toList());
                    aktualisiereListe(gefiltert);
                }
                return true;
            }
        });
    }

    // ====================================
    // Utility Methods
    // ====================================

    private void aktualisiereListe(List<Eintrag> eintraege) {
        List<EintraegeAdapter.ListItem> items = new ArrayList<>();

        Map<String, List<Eintrag>> gruppiertNachTag = eintraege.stream()
                .collect(Collectors.groupingBy(Eintrag::getDatum));

        LocalDate heute = LocalDate.now();
        LocalDate montag = heute.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 5; i++) {
            LocalDate tag = montag.plusDays(i);
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

        adapter.setItems(items);
    }
}
