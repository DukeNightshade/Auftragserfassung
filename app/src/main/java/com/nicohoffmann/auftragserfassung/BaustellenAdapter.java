package com.nicohoffmann.auftragserfassung;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.nicohoffmann.auftragserfassung.model.Baustelle;
import java.util.List;

/**
 * RecyclerView-Adapter zur Darstellung einer Liste von Baustellen.
 * Unterstützt effiziente Listenaktualisierung via DiffUtil und Favorit-Toggle.
 * @author Nico Hoffmann
 * @version 1.0
 */
public class BaustellenAdapter extends RecyclerView.Adapter<BaustellenAdapter.ViewHolder> {

    // ====================================
    // Instance Variables
    // ====================================

    private List<Baustelle> baustellen;
    private final OnFavoritClickListener favoritListener;

    // ====================================
    // Constructors
    // ====================================

    public BaustellenAdapter(List<Baustelle> baustellen, OnFavoritClickListener favoritListener) {
        this.baustellen = baustellen;
        this.favoritListener = favoritListener;
    }

    // ====================================
    // Business Logic Methods
    // ====================================

    public void setBaustellen(List<Baustelle> neueListe) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return baustellen != null ? baustellen.size() : 0;
            }

            @Override
            public int getNewListSize() {
                return neueListe != null ? neueListe.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return baustellen.get(oldPos).getId() == neueListe.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Baustelle alt = baustellen.get(oldPos);
                Baustelle neu = neueListe.get(newPos);
                return alt.getName().equals(neu.getName())
                        && alt.getAdresse().equals(neu.getAdresse())
                        && alt.isFavorit() == neu.isFavorit();
            }
        });
        this.baustellen = neueListe;
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_baustelle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Baustelle baustelle = baustellen.get(position);
        holder.textViewName.setText(baustelle.getName());
        holder.textViewAdresse.setText(baustelle.getAdresse());
        holder.buttonFavorit.setImageResource(
                baustelle.isFavorit()
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );
        holder.buttonFavorit.setOnClickListener(v -> favoritListener.onFavoritClick(baustelle));
    }

    @Override
    public int getItemCount() {
        return baustellen != null ? baustellen.size() : 0;
    }

    // ====================================
    // Utility Methods
    // ====================================


    // ====================================
    // Inner Classes
    // ====================================

    public interface OnFavoritClickListener {
        void onFavoritClick(Baustelle baustelle);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewAdresse;
        ImageButton buttonFavorit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAdresse = itemView.findViewById(R.id.textViewAdresse);
            buttonFavorit = itemView.findViewById(R.id.buttonFavorit);
        }
    }
}
