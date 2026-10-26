package com.example.habitbreaker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * ══════════════════════════════════════════════════════════
 *  ACTIVITÉ 6 : StatsActivity  (Statistiques globales)
 * ══════════════════════════════════════════════════════════
 *
 * Affiche un résumé des performances de l'utilisateur connecté.
 * Les données viennent directement de SQLite.
 */
public class StatsActivity extends AppCompatActivity {

    private TextView textTotalHabitudes;
    private TextView textMeilleurStreak;
    private TextView textChampion;
    private TextView textTotalRechutes;
    private TextView textObjectifsAtteints;
    private TextView textEncouragement;
    private Button   boutonRetour;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Lier les vues
        textTotalHabitudes    = findViewById(R.id.statsTotalHabitudes);
        textMeilleurStreak    = findViewById(R.id.statsMeilleurStreak);
        textChampion          = findViewById(R.id.statsChampion);
        textTotalRechutes     = findViewById(R.id.statsTotalRechutes);
        textObjectifsAtteints = findViewById(R.id.statsObjectifsAtteints);
        textEncouragement     = findViewById(R.id.statsEncouragement);
        boutonRetour          = findViewById(R.id.statsRetour);

        // Charger les habitudes depuis SQLite et calculer les stats
        ArrayList<Habit> liste = db.chargerHabitudes(session.getUserId());
        calculerEtAfficher(liste);

        boutonRetour.setOnClickListener(v -> finish());
    }

    private void calculerEtAfficher(ArrayList<Habit> liste) {
        int totalHabitudes      = liste.size();
        int totalRechutes       = 0;
        int meilleurStreakGlobal = 0;
        int objectifsAtteints   = 0;
        String nomChampion      = "Aucune";

        for (Habit h : liste) {
            totalRechutes += h.getCompteur();

            if (h.getMeilleureStreak() > meilleurStreakGlobal) {
                meilleurStreakGlobal = h.getMeilleureStreak();
                nomChampion = h.getNom();
            }

            if (h.objectifAtteint()) {
                objectifsAtteints++;
            }
        }

        // Afficher les résultats
        textTotalHabitudes.setText("📋 Habitudes suivies : " + totalHabitudes);
        textMeilleurStreak.setText("🏆 Meilleur streak : " + meilleurStreakGlobal + " jour(s)");
        textChampion.setText("🥇 Champion : " + nomChampion);
        textTotalRechutes.setText("❌ Total rechutes : " + totalRechutes);
        textObjectifsAtteints.setText("🎯 Objectifs atteints : " + objectifsAtteints + " / " + totalHabitudes);

        // Message d'encouragement selon la situation
        if (totalHabitudes == 0) {
            textEncouragement.setText("Ajoutez votre première habitude !");
        } else if (objectifsAtteints == totalHabitudes) {
            textEncouragement.setText("🎊 Incroyable ! Tous vos objectifs sont atteints !");
        } else if (meilleurStreakGlobal >= 7) {
            textEncouragement.setText("🔥 Vous êtes sur la bonne voie. Continuez !");
        } else if (totalRechutes == 0) {
            textEncouragement.setText("✨ Zéro rechute ! Vous êtes discipliné(e) !");
        } else {
            textEncouragement.setText("💪 Chaque jour est une nouvelle chance. Lâchez pas !");
        }
    }
}
