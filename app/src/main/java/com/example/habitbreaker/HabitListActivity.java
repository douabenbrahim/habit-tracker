package com.example.habitbreaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * ══════════════════════════════════════════════════════════
 *  ACTIVITÉ 3 : HabitListActivity  (Liste des habitudes)
 * ══════════════════════════════════════════════════════════
 *
 * Écran principal après connexion. Affiche les habitudes
 * de l'utilisateur connecté uniquement (grâce au user_id).
 *
 * Les données viennent de SQLite (plus de SharedPreferences).
 *
 * Interactions :
 *   Clic simple  → HabitDetailActivity (passe l'index)
 *   Clic long    → Dialogue de suppression (SQLite DELETE)
 *   Bouton "+"   → AddHabitActivity
 *   Bouton Stats → StatsActivity
 *   Bouton 🚪    → Déconnexion → LoginActivity
 */
public class HabitListActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────
    private ListView listViewHabitudes;
    private Button   boutonAjouter;
    private Button   boutonStats;
    private Button   boutonDeconnecter;
    private TextView textVide;
    private TextView textBienvenue;

    // ── Données ───────────────────────────────────────────────
    private ArrayList<Habit> listeHabitudes;
    private HabitAdapter     adaptateur;
    private DatabaseHelper   db;
    private SessionManager   session;
    private long             userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier la session
        session = new SessionManager(this);
        if (!session.estConnecte()) {
            // Pas de session → retour au login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_habit_list);

        db     = new DatabaseHelper(this);
        userId = session.getUserId();

        // Lier les vues
        listViewHabitudes = findViewById(R.id.listViewHabitudes);
        boutonAjouter     = findViewById(R.id.boutonAjouter);
        boutonStats       = findViewById(R.id.boutonStats);
        boutonDeconnecter = findViewById(R.id.boutonDeconnecter);
        textVide          = findViewById(R.id.textVide);
        textBienvenue     = findViewById(R.id.textBienvenue);

        // Message de bienvenue personnalisé
        textBienvenue.setText("👋 Bonjour, " + session.getUsername() + " !");

        // Charger les habitudes depuis SQLite
        listeHabitudes = db.chargerHabitudes(userId);

        // Créer et lier l'adaptateur
        adaptateur = new HabitAdapter(this, listeHabitudes);
        listViewHabitudes.setAdapter(adaptateur);

        mettreAJourMessageVide();

        // ── Clic simple → Détail ──────────────────────────────
        listViewHabitudes.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HabitListActivity.this, HabitDetailActivity.class);
            intent.putExtra("index", position);
            startActivity(intent);
        });

        // ── Clic long → Supprimer ─────────────────────────────
        listViewHabitudes.setOnItemLongClickListener((parent, view, position, id) -> {
            Habit h = listeHabitudes.get(position);
            new AlertDialog.Builder(HabitListActivity.this)
                .setTitle("🗑 Supprimer")
                .setMessage("Supprimer \"" + h.getNom() + "\" ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Supprimer de SQLite
                    db.supprimerHabitude(h.getDbId());
                    // Supprimer de la liste en mémoire
                    listeHabitudes.remove(position);
                    adaptateur.notifyDataSetChanged();
                    mettreAJourMessageVide();
                    Toast.makeText(HabitListActivity.this,
                        "Habitude supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
            return true;
        });

        // ── Bouton Ajouter ────────────────────────────────────
        boutonAjouter.setOnClickListener(v ->
            startActivity(new Intent(HabitListActivity.this, AddHabitActivity.class))
        );

        // ── Bouton Stats ──────────────────────────────────────
        boutonStats.setOnClickListener(v ->
            startActivity(new Intent(HabitListActivity.this, StatsActivity.class))
        );

        // ── Bouton Déconnecter ────────────────────────────────
        boutonDeconnecter.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("🚪 Déconnexion")
                .setMessage("Voulez-vous vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    session.deconnecter();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger depuis SQLite à chaque retour sur cet écran
        // pour refléter les mises à jour faites dans HabitDetailActivity
        listeHabitudes.clear();
        listeHabitudes.addAll(db.chargerHabitudes(userId));
        adaptateur.notifyDataSetChanged();
        mettreAJourMessageVide();
    }

    /** Affiche un message si la liste est vide */
    private void mettreAJourMessageVide() {
        if (listeHabitudes.isEmpty()) {
            textVide.setVisibility(View.VISIBLE);
            listViewHabitudes.setVisibility(View.GONE);
        } else {
            textVide.setVisibility(View.GONE);
            listViewHabitudes.setVisibility(View.VISIBLE);
        }
    }
}
