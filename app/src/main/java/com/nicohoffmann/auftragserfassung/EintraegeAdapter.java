package com.nicohoffmann.auftragserfassung;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RecyclerView-Adapter zur Darstellung von Einträgen gruppiert nach Wochentagen.
 * Unterstützt drei Listentypen: Header, Eintrag und "Mehr anzeigen".
 * @author Nico Hoffmann
 * @version 1.0
 */
public class EintraegeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ====================================
    // Static Variables
    // ====================================

    private static final int TYP_HEADER = 0;
    private static final int TYP_EINTRAG = 1;
    private static final int TYP_MEHR = 2;
    private static final int MAX_EINTRAEGE_PRO_TAG = 2;

    // ====================================
    // Instance Variables
    // ====================================

    private List<ListItem> items;
    private OnEintragClickListener clickListener;
    private OnTagEintragClickListener tagEintragListener;

    // ====================================
    // Constructors
    // ====================================

    public EintraegeAdapter(List<ListItem> items) {
        this.items = items;
    }

    // ====================================
    // Business Logic Methods
    // ====================================

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof HeaderItem) return TYP_HEADER;
        if (items.get(position) instanceof EintragItem) return TYP_EINTRAG;
        return TYP_MEHR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYP_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_tag_header, parent, false));
        } else if (viewType == TYP_EINTRAG) {
            return new EintragViewHolder(inflater.inflate(R.layout.item_eintrag, parent, false));
        } else {
            return new MehrViewHolder(inflater.inflate(R.layout.item_mehr, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder, position);
        } else if (holder instanceof EintragViewHolder) {
            bindEintrag((EintragViewHolder) holder, position);
        } else if (holder instanceof MehrViewHolder) {
            bindMehr((MehrViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ====================================
    // Bind Methods
    // ====================================

    private void bindHeader(HeaderViewHolder h, int position) {
        HeaderItem headerItem = (HeaderItem) items.get(position);
        h.textView.setText(headerItem.getTag());
        h.textViewDatum.setText(headerItem.getDatum());

        if (headerItem.getArbeitszeit() != null && !headerItem.getArbeitszeit().isEmpty()) {
            // Zeit vorhanden → anzeigen
            h.textViewArbeitszeit.setText(headerItem.getArbeitszeit());
            h.textViewArbeitszeit.setTextColor(
                    h.itemView.getContext().getColor(R.color.colorTextSecondary));
            h.textViewArbeitszeit.setVisibility(View.VISIBLE);
        } else if (headerItem.getArbeitszeit() == null) {
            // Kein Eintrag → komplett ausblenden
            h.textViewArbeitszeit.setText("");
            h.textViewArbeitszeit.setVisibility(View.GONE);
        } else {
            // Eintrag vorhanden, aber keine Zeit → Placeholder
            h.textViewArbeitszeit.setText(
                    h.itemView.getContext().getString(R.string.placeholder_arbeitszeit));
            h.textViewArbeitszeit.setTextColor(
                    h.itemView.getContext().getColor(R.color.colorAccent));
            h.textViewArbeitszeit.setVisibility(View.VISIBLE);
        }

        h.buttonTagEintrag.setOnClickListener(v -> {
            if (tagEintragListener != null) {
                tagEintragListener.onTagEintragClick(headerItem.getRawDatum());
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void bindEintrag(EintragViewHolder h, int position) {
        EintragItem item = (EintragItem) items.get(position);
        h.textViewZeit.setText(item.getEintrag().getZeitVon() + " – " + item.getEintrag().getZeitBis());
        h.textViewBaustelle.setText(item.getBaustelleName());
        h.textViewBeschreibung.setText(item.getEintrag().getBeschreibung());
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onEintragClick(item);
        });
    }

    private void bindMehr(MehrViewHolder h, int position) {
        MehrItem mehrItem = (MehrItem) items.get(position);
        long versteckt = mehrItem.getAlleEintraege().stream()
                .filter(e -> e.getDatum().equals(mehrItem.getDatum()))
                .count() - MAX_EINTRAEGE_PRO_TAG;
        h.textViewMehr.setText(h.itemView.getContext()
                .getString(R.string.label_mehr_anzahl, versteckt));
        h.itemView.setOnClickListener(v -> expandMehr(h, mehrItem));
    }

    private void expandMehr(MehrViewHolder h, MehrItem mehrItem) {
        int pos = h.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_ID) return;
        items.remove(pos);

        List<Eintrag> tagesEintraege = mehrItem.getAlleEintraege().stream()
                .filter(e -> e.getDatum().equals(mehrItem.getDatum()))
                .collect(Collectors.toList());

        int einfuegeIndex = pos;
        for (int i = MAX_EINTRAEGE_PRO_TAG; i < tagesEintraege.size(); i++) {
            Eintrag e = tagesEintraege.get(i);
            String baustelleName = mehrItem.getAlleBaustellen().stream()
                    .filter(b -> b.getId() == (e.getBaustelleId() != null ? e.getBaustelleId() : -1))
                    .map(Baustelle::getName)
                    .findFirst()
                    .orElse("");
            items.add(einfuegeIndex++, new EintragItem(e, baustelleName));
        }

        notifyItemRemoved(pos);
        notifyItemRangeInserted(pos, tagesEintraege.size() - MAX_EINTRAEGE_PRO_TAG);
    }

    // ====================================
    // Utility Methods
    // ====================================

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<ListItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setOnEintragClickListener(OnEintragClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTagEintragClickListener(OnTagEintragClickListener listener) {
        this.tagEintragListener = listener;
    }

    // ====================================
    // Interfaces
    // ====================================

    public interface OnEintragClickListener {
        void onEintragClick(EintragItem item);
    }

    public interface OnTagEintragClickListener {
        void onTagEintragClick(String datum);
    }

    public interface ListItem {}

    // ====================================
    // Inner Classes
    // ====================================

    public static class HeaderItem implements ListItem {
        private final String tag;
        private final String datum;
        private final String rawDatum;
        private final String arbeitszeit;

        public HeaderItem(String tag, String datum, String rawDatum, String arbeitszeit) {
            this.tag = tag;
            this.datum = datum;
            this.rawDatum = rawDatum;
            this.arbeitszeit = arbeitszeit;
        }

        public String getTag() { return tag; }
        public String getDatum() { return datum; }
        public String getRawDatum() { return rawDatum; }
        public String getArbeitszeit() { return arbeitszeit; }
    }

    public static class EintragItem implements ListItem {
        private final Eintrag eintrag;
        private final String baustelleName;

        public EintragItem(Eintrag eintrag, String baustelleName) {
            this.eintrag = eintrag;
            this.baustelleName = baustelleName;
        }

        public Eintrag getEintrag() { return eintrag; }
        public String getBaustelleName() { return baustelleName; }
    }

    public static class MehrItem implements ListItem {
        private final String datum;
        private final List<Eintrag> alleEintraege;
        private final List<Baustelle> alleBaustellen;

        public MehrItem(String datum, List<Eintrag> alleEintraege, List<Baustelle> alleBaustellen) {
            this.datum = datum;
            this.alleEintraege = alleEintraege;
            this.alleBaustellen = alleBaustellen;
        }

        public String getDatum() { return datum; }
        public List<Eintrag> getAlleEintraege() { return alleEintraege; }
        public List<Baustelle> getAlleBaustellen() { return alleBaustellen; }
    }

    // ====================================
    // ViewHolders
    // ====================================

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textViewDatum;
        TextView textViewArbeitszeit;
        MaterialButton buttonTagEintrag;

        HeaderViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textViewTagHeader);
            textViewDatum = v.findViewById(R.id.textViewTagDatum);
            textViewArbeitszeit = v.findViewById(R.id.textViewTagArbeitszeit);
            buttonTagEintrag = v.findViewById(R.id.buttonTagEintrag);
        }
    }

    static class EintragViewHolder extends RecyclerView.ViewHolder {
        TextView textViewZeit;
        TextView textViewBaustelle;
        TextView textViewBeschreibung;

        EintragViewHolder(View v) {
            super(v);
            textViewZeit = v.findViewById(R.id.textViewZeit);
            textViewBaustelle = v.findViewById(R.id.textViewBaustelle);
            textViewBeschreibung = v.findViewById(R.id.textViewBeschreibung);
        }
    }

    static class MehrViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMehr;

        MehrViewHolder(View v) {
            super(v);
            textViewMehr = v.findViewById(R.id.textViewMehr);
        }
    }
}
