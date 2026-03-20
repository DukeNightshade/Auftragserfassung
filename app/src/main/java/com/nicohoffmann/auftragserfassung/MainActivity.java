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
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private EintraegeAdapter adapter;
    private AppDatabase db;
    private List<Eintrag> alleEintraege = new ArrayList<>();
    private List<Baustelle> alleBaustellen = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEintraege);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EintraegeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db.baustelleDao().getAlleBaustellen().observe(this, baustellen -> {
            alleBaustellen = baustellen;
            aktualisiereliste(alleEintraege);
        });

        db.eintragDao().getAlleEintraege().observe(this, eintraege -> {
            alleEintraege = eintraege;
            aktualisiereliste(eintraege);
        });

        FloatingActionButton fab = findViewById(R.id.fabNeuerEintrag);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, NeuerEintragActivity.class))
        );

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    aktualisiereliste(alleEintraege);
                } else {
                    List<Eintrag> gefiltert = alleEintraege.stream()
                            .filter(e -> e.beschreibung != null &&
                                    e.beschreibung.toLowerCase().contains(newText.toLowerCase()))
                            .collect(Collectors.toList());
                    aktualisiereliste(gefiltert);
                }
                return true;
            }
        });
    }

    private void aktualisiereliste(List<Eintrag> eintraege) {
        List<EintraegeAdapter.ListItem> items = new ArrayList<>();

        Map<String, List<Eintrag>> gruppiertNachTag = eintraege.stream()
                .collect(Collectors.groupingBy(e -> e.datum));

        // Aktuelle Woche Montag–Freitag
        LocalDate heute = LocalDate.now();
        LocalDate montag = heute.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 5; i++) {
            LocalDate tag = montag.plusDays(i);
            String datum = tag.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String tagName = tag.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.GERMAN);

            List<Eintrag> tagEintraege = gruppiertNachTag.getOrDefault(datum, new ArrayList<>());

            items.add(new EintraegeAdapter.HeaderItem(tagName));

            int anzahl = Math.min(tagEintraege.size(), 2);
            for (int j = 0; j < anzahl; j++) {
                Eintrag e = tagEintraege.get(j);
                String baustelleName = alleBaustellen.stream()
                        .filter(b -> b.id == (e.baustelleId != null ? e.baustelleId : -1))
                        .map(b -> b.name)
                        .findFirst()
                        .orElse("");
                items.add(new EintraegeAdapter.EintragItem(e, baustelleName));
            }

            if (tagEintraege.size() > 2) {
                items.add(new EintraegeAdapter.MehrItem());
            }
        }

        adapter.setItems(items);
    }
}
