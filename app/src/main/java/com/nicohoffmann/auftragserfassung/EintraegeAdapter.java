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

public class EintraegeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYP_HEADER = 0;
    private static final int TYP_EINTRAG = 1;
    private static final int TYP_MEHR = 2;

    public abstract static class ListItem {}

    public static class HeaderItem extends ListItem {
        public String tag;
        public HeaderItem(String tag) { this.tag = tag; }
    }

    public static class EintragItem extends ListItem {
        public Eintrag eintrag;
        public String baustelleName;
        public EintragItem(Eintrag eintrag, String baustelleName) {
            this.eintrag = eintrag;
            this.baustelleName = baustelleName;
        }
    }

    public static class MehrItem extends ListItem {}

    private List<ListItem> items;

    public EintraegeAdapter(List<ListItem> items) {
        this.items = items;
    }

    public void setItems(List<ListItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

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
            ((HeaderViewHolder) holder).textView.setText(((HeaderItem) items.get(position)).tag);
        } else if (holder instanceof EintragViewHolder) {
            EintragItem item = (EintragItem) items.get(position);
            EintragViewHolder h = (EintragViewHolder) holder;
            h.textViewZeit.setText(item.eintrag.zeitVon + " – " + item.eintrag.zeitBis);
            h.textViewBaustelle.setText(item.baustelleName);
            h.textViewBeschreibung.setText(item.eintrag.beschreibung);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        HeaderViewHolder(View v) { super(v); textView = v.findViewById(R.id.textViewTagHeader); }
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
