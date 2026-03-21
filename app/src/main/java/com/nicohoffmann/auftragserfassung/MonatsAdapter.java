package com.nicohoffmann.auftragserfassung;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RecyclerView-Adapter für die Monatskalenderansicht.
 * Stellt Tage farblich dar je nachdem ob Einträge vorhanden sind.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class MonatsAdapter extends RecyclerView.Adapter<MonatsAdapter.ViewHolder> {

    // ====================================
    // Static Variables
    // ====================================

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ====================================
    // Instance Variables
    // ====================================

    private final List<LocalDate> tage;
    private Set<String> tageWithEintraege = new HashSet<>();
    private final OnTagClickListener listener;
    private final LocalDate heute;

    // ====================================
    // Constructors
    // ====================================

    public MonatsAdapter(List<LocalDate> tage, List<Eintrag> eintraege, OnTagClickListener listener) {
        this.tage = tage;
        this.listener = listener;
        this.heute = LocalDate.now();
        aktualisiereEintraege(eintraege);
    }

    // ====================================
    // Business Logic Methods
    // ====================================

    public void aktualisiereEintraege(List<Eintrag> eintraege) {
        Set<String> neueTage = eintraege.stream()
                .map(Eintrag::getDatum)
                .collect(Collectors.toSet());

        for (int i = 0; i < tage.size(); i++) {
            LocalDate tag = tage.get(i);
            if (tag == null) continue;

            String datum = tag.format(DATUM_FORMAT);
            boolean warEnthalten = tageWithEintraege.contains(datum);
            boolean istEnthalten = neueTage.contains(datum);

            if (warEnthalten != istEnthalten) {
                notifyItemChanged(i);
            }
        }

        this.tageWithEintraege = neueTage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kalender_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate tag = tage.get(position);
        Context context = holder.itemView.getContext();

        if (tag == null) {
            holder.textViewTag.setText("");
            holder.textViewTag.setBackground(null);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.textViewTag.setText(String.valueOf(tag.getDayOfMonth()));

        String datumString = tag.format(DATUM_FORMAT);
        boolean hatEintrag = tageWithEintraege.contains(datumString);
        boolean istHeute = tag.equals(heute);

        if (istHeute) {
            holder.textViewTag.setBackgroundResource(R.drawable.bg_tag_heute);
            holder.textViewTag.setTextColor(Color.WHITE);
        } else if (hatEintrag) {
            holder.textViewTag.setBackgroundResource(R.drawable.bg_tag_mit_eintrag);
            holder.textViewTag.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.textViewTag.setBackground(null);
            holder.textViewTag.setTextColor(ContextCompat.getColor(context, R.color.colorTagOhneEintrag));
        }

        holder.itemView.setOnClickListener(v -> listener.onTagClick(tag));
    }

    @Override
    public int getItemCount() {
        return tage != null ? tage.size() : 0;
    }

    // ====================================
    // Inner Classes
    // ====================================

    public interface OnTagClickListener {
        void onTagClick(LocalDate datum);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTag = itemView.findViewById(R.id.textViewTag);
        }
    }
}
