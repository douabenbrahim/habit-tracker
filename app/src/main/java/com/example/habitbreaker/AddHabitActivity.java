package com.example.habitbreaker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ══════════════════════════════════════════════════════════
 *  ACTIVITÉ 4 : AddHabitActivity  (Ajouter une habitude)
 * ══════════════════════════════════════════════════════════
 *
 * Permet d'ajouter une nouvelle habitude liée à l'utilisateur
 * connecté. L'habitude est directement insérée dans SQLite.
 *
 * Champs :
 *   - Nom de l'habitude (obligatoire)
 *   - Objectif en jours (facultatif, défaut = 7)
 */
public class AddHabitActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────
    private EditText champNom;
    private EditText champObjectif;
    private Button   boutonEnregistrer;
    private Button   boutonAnnuler;

    // ── Helpers ───────────────────────────────────────────────
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Lier les vues
        champNom          = findViewById(R.id.champNomHabitude);
        champObjectif     = findViewById(R.id.champObjectif);
        boutonEnregistrer = findViewById(R.id.boutonEnregistrer);
        boutonAnnuler     = findViewById(R.id.boutonAnnuler);

        boutonEnregistrer.setOnClickListener(v -> enregistrer());
        boutonAnnuler.setOnClickListener(v -> finish());
    }

    /**
     * Valide les champs et insère l'habitude dans SQLite.
     */
    private void enregistrer() {
        // Valider le nom
        String nom = champNom.getText().toString().trim();
        if (TextUtils.isEmpty(nom)) {
            champNom.setError("Veuillez saisir un nom !");
            champNom.requestFocus();
            return;
        }

        // Lire l'objectif (défaut : 7 si vide ou invalide)
        int objectif = 7;
        String objStr = champObjectif.getText().toString().trim();
        if (!TextUtils.isEmpty(objStr)) {
            try {
                objectif = Integer.parseInt(objStr);
                if (objectif <= 0) objectif = 7;
            } catch (NumberFormatException e) {
                objectif = 7;
            }
        }

        // Créer l'objet Habit
        Habit nouvelleHabitude = new Habit(nom);
        nouvelleHabitude.setObjectif(objectif);

        // Insérer dans SQLite (lié au user connecté)
        long userId = session.getUserId();
        long newId  = db.insererHabitude(userId, nouvelleHabitude);

        if (newId != -1) {
            Toast.makeText(this, "✅ Habitude ajoutée !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout.", Toast.LENGTH_SHORT).show();
        }
    }
}
