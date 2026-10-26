package com.example.habitbreaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * ══════════════════════════════════════════════════════════
 *  ADAPTATEUR : HabitAdapter
 * ══════════════════════════════════════════════════════════
 *
 * Relie la ArrayList<Habit> à la ListView.
 * Utilise le layout personnalisé list_item_habit.xml.
 *
 * Chaque ligne affiche :
 *   - Nom de l'habitude
 *   - Streak actuel avec emoji et couleur dynamique
 *   - ProgressBar vers l'objectif
 *   - Nombre de rechutes
 */
public class HabitAdapter extends ArrayAdapter<Habit> {

    private Context          context;
    private ArrayList<Habit> liste;

    public HabitAdapter(Context context, ArrayList<Habit> liste) {
        super(context, 0, liste);
        this.context = context;
        this.liste   = liste;
    }

    /**
     * getView() est appelé par Android pour chaque ligne visible.
     * On remplit notre layout avec les données de l'habitude.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Réutiliser une vue existante si possible (optimisation mémoire)
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_habit, parent, false);
        }

        Habit habit = liste.get(position);

        // Récupérer les vues du layout
        TextView    textNom      = convertView.findViewById(R.id.itemNom);
        TextView    textStreak   = convertView.findViewById(R.id.itemStreak);
        TextView    textRechutes = convertView.findViewById(R.id.itemRechutes);
        ProgressBar progressBar  = convertView.findViewById(R.id.itemProgress);
        TextView    textObjectif = convertView.findViewById(R.id.itemObjectif);

        // Remplir les données
        textNom.setText(habit.getNom());

        // Streak avec couleur et emoji selon l'état
        if (habit.getStreak() == 0) {
            textStreak.setText("⚪ Streak : 0 jour");
            textStreak.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else if (habit.objectifAtteint()) {
            textStreak.setText("🏆 " + habit.getStreak() + " jours — Objectif atteint !");
            textStreak.setTextColor(0xFF2E7D32); // Vert foncé
        } else {
            textStreak.setText("🔥 Streak : " + habit.getStreak() + " jour(s)");
            textStreak.setTextColor(0xFFE65100); // Orange
        }

        // Rechutes
        textRechutes.setText("❌ Rechutes : " + habit.getCompteur());

        // ProgressBar
        progressBar.setMax(100);
        progressBar.setProgress(habit.getProgressionPourcentage());

        // Label progression
        textObjectif.setText(habit.getStreak() + " / " + habit.getObjectif() + " jours");

        return convertView;
    }
}
