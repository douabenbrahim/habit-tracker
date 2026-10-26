package com.example.habitbreaker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ══════════════════════════════════════════════════════════
 *  ACTIVITÉ 2 : RegisterActivity  (Créer un compte)
 * ══════════════════════════════════════════════════════════
 *
 * Permet à un nouvel utilisateur de créer son compte.
 * Les données sont enregistrées dans la table SQLite "users".
 *
 * Vérifications :
 *   - Tous les champs obligatoires
 *   - Mot de passe ≥ 4 caractères
 *   - Les deux mots de passe identiques
 *   - Username non déjà utilisé
 *
 * Après inscription réussie → retour à LoginActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────
    private EditText champUsername;
    private EditText champPassword;
    private EditText champConfirm;
    private Button   boutonInscrire;
    private TextView lienConnexion;
    private TextView textErreur;

    // ── Helper ────────────────────────────────────────────────
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        // Lier les vues
        champUsername  = findViewById(R.id.regChampUsername);
        champPassword  = findViewById(R.id.regChampPassword);
        champConfirm   = findViewById(R.id.regChampConfirm);
        boutonInscrire = findViewById(R.id.regBoutonInscrire);
        lienConnexion  = findViewById(R.id.regLienConnexion);
        textErreur     = findViewById(R.id.regTextErreur);

        // ── BOUTON : S'inscrire ───────────────────────────────
        boutonInscrire.setOnClickListener(v -> tenterInscription());

        // ── LIEN : Déjà un compte ? ───────────────────────────
        lienConnexion.setOnClickListener(v -> finish()); // retourne au login
    }

    /**
     * Valide tous les champs et insère l'utilisateur en base.
     */
    private void tenterInscription() {
        String username = champUsername.getText().toString().trim();
        String password = champPassword.getText().toString();
        String confirm  = champConfirm.getText().toString();

        // ── Validation ────────────────────────────────────────

        if (TextUtils.isEmpty(username)) {
            afficherErreur("Veuillez saisir un nom d'utilisateur.");
            champUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            afficherErreur("Le nom d'utilisateur doit contenir au moins 3 caractères.");
            champUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            afficherErreur("Veuillez saisir un mot de passe.");
            champPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            afficherErreur("Le mot de passe doit contenir au moins 4 caractères.");
            champPassword.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            afficherErreur("Les mots de passe ne correspondent pas.");
            champConfirm.requestFocus();
            return;
        }

        // ── Vérifier si username déjà pris ───────────────────
        if (db.usernameExiste(username)) {
            afficherErreur("Ce nom d'utilisateur est déjà utilisé. Choisissez-en un autre.");
            champUsername.requestFocus();
            return;
        }

        // ── Insérer en base ───────────────────────────────────
        long nouvelId = db.insererUtilisateur(username, password);

        if (nouvelId != -1) {
            // Succès
            textErreur.setVisibility(View.GONE);
            Toast.makeText(this,
                "🎉 Compte créé avec succès ! Connectez-vous.",
                Toast.LENGTH_LONG).show();

            // Retourner vers le login
            finish();
        } else {
            afficherErreur("Une erreur est survenue. Réessayez.");
        }
    }

    /** Affiche un message d'erreur rouge sous le formulaire */
    private void afficherErreur(String message) {
        textErreur.setVisibility(View.VISIBLE);
        textErreur.setText("❌ " + message);
    }
}
