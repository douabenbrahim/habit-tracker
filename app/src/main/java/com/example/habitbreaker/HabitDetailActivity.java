package com.example.habitbreaker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

/**
 * ══════════════════════════════════════════════════════════
 *  ACTIVITÉ 5 : HabitDetailActivity  (Détail d'une habitude)
 * ══════════════════════════════════════════════════════════
 *
 * Affiche le détail complet d'une habitude et permet
 * d'enregistrer les actions (résistance / rechute).
 *
 * Changement V3 : après chaque action, on appelle
 * db.mettreAJourHabitude(habit) pour persister dans SQLite.
 */
public class HabitDetailActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────
    private TextView    textNom;
    private TextView    textStreak;
    private TextView    textMeilleureStreak;
    private TextView    textRechutes;
    private TextView    textProgression;
    private TextView    textMessage;
    private ProgressBar progressBar;
    private Button      boutonResiste;
    private Button      boutonEchoue;
    private Button      boutonReinitialiser;
    private Button      boutonRetour;

    // ── Données ───────────────────────────────────────────────
    private DatabaseHelper db;
    private Habit          habitCourante;

    // Couleurs
    private static final int VERT   = 0xFF2E7D32;
    private static final int ROUGE  = 0xFFC62828;
    private static final int ORANGE = 0xFFE65100;
    private static final int GRIS   = 0xFF757575;

    // Messages de motivation
    private static final String[] MESSAGES_RESISTANCE = {
        "💪 Bravo ! Vous êtes plus fort que vos habitudes !",
        "🔥 Incroyable ! Continuez sur cette lancée !",
        "⭐ Chaque jour de résistance est une victoire !",
        "🏆 Vous construisez une meilleure version de vous !",
        "✨ La discipline d'aujourd'hui = la liberté de demain !",
        "🌟 Vous êtes capable de tout surmonter !",
        "💎 La persévérance est votre super-pouvoir !",
        "🚀 Un jour de plus vers votre objectif !"
    };

    private static final String[] MESSAGES_ECHEC = {
        "😔 Ce n'est qu'un obstacle, relevez-vous !",
        "💙 Chaque échec est une leçon. Recommencez !",
        "🌱 La chute n'est pas la fin du voyage.",
        "🤝 Soyez indulgent avec vous-même. Demain est nouveau.",
        "🔄 Le chemin n'est pas linéaire. Continuez !"
    };

    private static final String[] MESSAGES_OBJECTIF = {
        "🎉 OBJECTIF ATTEINT ! Vous êtes extraordinaire !",
        "🏅 Félicitations ! Vous avez brisé cette habitude !",
        "🎊 Incroyable ! Fixez-vous un nouvel objectif !"
    };

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        db = new DatabaseHelper(this);

        // Récupérer l'index de l'habitude depuis HabitListActivity
        int index = getIntent().getIntExtra("index", -1);

        // On recharge les habitudes depuis SQLite pour avoir les données fraîches
        SessionManager session = new SessionManager(this);
        ArrayList<Habit> liste = db.chargerHabitudes(session.getUserId());

        if (index < 0 || index >= liste.size()) {
            Toast.makeText(this, "Habitude introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        habitCourante = liste.get(index);

        // Lier les vues
        textNom             = findViewById(R.id.textNomHabitude);
        textStreak          = findViewById(R.id.textStreak);
        textMeilleureStreak = findViewById(R.id.textMeilleureStreak);
        textRechutes        = findViewById(R.id.textRechutes);
        textProgression     = findViewById(R.id.textProgression);
        textMessage         = findViewById(R.id.textMessage);
        progressBar         = findViewById(R.id.progressBar);
        boutonResiste       = findViewById(R.id.boutonResiste);
        boutonEchoue        = findViewById(R.id.boutonEchoue);
        boutonReinitialiser = findViewById(R.id.boutonReinitialiser);
        boutonRetour        = findViewById(R.id.boutonRetour);

        mettreAJourAffichage();

        // ── BOUTON : J'ai résisté ─────────────────────────────
        boutonResiste.setOnClickListener(v -> {
            habitCourante.resiste();
            db.mettreAJourHabitude(habitCourante); // ← SAUVEGARDE SQLite
            mettreAJourAffichage();

            String message = habitCourante.objectifAtteint()
                ? MESSAGES_OBJECTIF[random.nextInt(MESSAGES_OBJECTIF.length)]
                : MESSAGES_RESISTANCE[random.nextInt(MESSAGES_RESISTANCE.length)];
            afficherMessage(message, VERT);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });

        // ── BOUTON : J'ai craqué ──────────────────────────────
        boutonEchoue.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("😔 J'ai craqué")
                .setMessage("Confirmer ? Votre streak sera remis à 0.")
                .setPositiveButton("Oui", (dialog, which) -> {
                    habitCourante.echoue();
                    db.mettreAJourHabitude(habitCourante); // ← SAUVEGARDE SQLite
                    mettreAJourAffichage();
                    String message = MESSAGES_ECHEC[random.nextInt(MESSAGES_ECHEC.length)];
                    afficherMessage(message, ROUGE);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Non", null)
                .show();
        });

        // ── BOUTON : Réinitialiser les rechutes ───────────────
        boutonReinitialiser.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("🔄 Réinitialiser")
                .setMessage("Remettre le compteur de rechutes à zéro ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    habitCourante.reinitialiserCompteur();
                    db.mettreAJourHabitude(habitCourante); // ← SAUVEGARDE SQLite
                    mettreAJourAffichage();
                    afficherMessage("🔄 Compteur remis à zéro !", GRIS);
                })
                .setNegativeButton("Non", null)
                .show();
        });

        // ── BOUTON : Retour ───────────────────────────────────
        boutonRetour.setOnClickListener(v -> finish());
    }

    /** Met à jour tous les éléments de l'interface */
    private void mettreAJourAffichage() {
        textNom.setText(habitCourante.getNom());

        int streak = habitCourante.getStreak();
        if (streak == 0) {
            textStreak.setText("⚪ Streak : 0 jour");
            textStreak.setTextColor(GRIS);
        } else if (habitCourante.objectifAtteint()) {
            textStreak.setText("🏆 Streak : " + streak + " jour(s) — OBJECTIF ATTEINT !");
            textStreak.setTextColor(VERT);
        } else {
            textStreak.setText("🔥 Streak : " + streak + " jour(s)");
            textStreak.setTextColor(ORANGE);
        }

        textMeilleureStreak.setText("⭐ Meilleur streak : " + habitCourante.getMeilleureStreak() + " jour(s)");

        int rechutes = habitCourante.getCompteur();
        textRechutes.setText("❌ Rechutes : " + rechutes);
        textRechutes.setTextColor(rechutes == 0 ? VERT : ROUGE);

        int pct = habitCourante.getProgressionPourcentage();
        progressBar.setProgress(pct);

        textProgression.setText(
            "🎯 Objectif : " + streak + " / " + habitCourante.getObjectif()
            + " jours (" + pct + "%)"
        );
        textProgression.setTextColor(habitCourante.objectifAtteint() ? VERT : 0xFF424242);
    }

    private void afficherMessage(String message, int couleur) {
        textMessage.setText(message);
        textMessage.setTextColor(couleur);
        textMessage.setVisibility(View.VISIBLE);
    }
}
