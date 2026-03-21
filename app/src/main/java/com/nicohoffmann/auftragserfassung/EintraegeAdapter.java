package com.nicohoffmann.auftragserfassung;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nicohoffmann.auftragserfassung.model.Eintrag;
import java.util.List;

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

    // ====================================
    // Instance Variables
    // ====================================

    private List<ListItem> items;
    private OnEintragClickListener clickListener;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).textView.setText(((HeaderItem) items.get(position)).getTag());
        } else if (holder instanceof EintragViewHolder) {
            EintragItem item = (EintragItem) items.get(position);
            EintragViewHolder h = (EintragViewHolder) holder;
            h.textViewZeit.setText(item.getEintrag().getZeitVon() + " – " + item.getEintrag().getZeitBis());
            h.textViewBaustelle.setText(item.getBaustelleName());
            h.textViewBeschreibung.setText(item.getEintrag().getBeschreibung());

            h.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onEintragClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
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

    // ====================================
    // Inner Classes / Interfaces
    // ====================================

    public interface OnEintragClickListener {
        void onEintragClick(EintragItem item);
    }

    public interface ListItem {}

    public static class HeaderItem implements ListItem {
        private final String tag;
        public HeaderItem(String tag) { this.tag = tag; }
        public String getTag() { return tag; }
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

    public static class MehrItem implements ListItem {}

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        HeaderViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textViewTagHeader);
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
        MehrViewHolder(View v) { super(v); }
    }
}
