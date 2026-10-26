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
 *  ACTIVITÉ 1 : LoginActivity  (Écran de connexion)
 * ══════════════════════════════════════════════════════════
 *
 * Premier écran affiché au lancement de l'app.
 * Si l'utilisateur est déjà connecté (session active), on
 * le redirige directement vers HabitListActivity.
 *
 * Sinon, il saisit son username + mot de passe.
 * → Vérifié dans la base SQLite via DatabaseHelper.
 * → Si valide : session créée → HabitListActivity
 * → Si invalide : message d'erreur affiché
 *
 * Lien vers RegisterActivity pour créer un compte.
 */
public class LoginActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────
    private EditText champUsername;
    private EditText champPassword;
    private Button   boutonConnecter;
    private TextView lienInscription;
    private TextView textErreur;

    // ── Helpers ───────────────────────────────────────────────
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier si session déjà active
        session = new SessionManager(this);
        if (session.estConnecte()) {
            // Déjà connecté → aller directement à la liste
            allerVersListe();
            return;
        }

        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        // Lier les vues
        champUsername   = findViewById(R.id.champUsername);
        champPassword   = findViewById(R.id.champPassword);
        boutonConnecter = findViewById(R.id.boutonConnecter);
        lienInscription = findViewById(R.id.lienInscription);
        textErreur      = findViewById(R.id.textErreur);

        // ── BOUTON : Se connecter ─────────────────────────────
        boutonConnecter.setOnClickListener(v -> tenterConnexion());

        // ── LIEN : Créer un compte ────────────────────────────
        lienInscription.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * Valide les champs, interroge la base, crée la session.
     */
    private void tenterConnexion() {
        String username = champUsername.getText().toString().trim();
        String password = champPassword.getText().toString();

        // Validation basique
        if (TextUtils.isEmpty(username)) {
            champUsername.setError("Saisissez votre nom d'utilisateur");
            champUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            champPassword.setError("Saisissez votre mot de passe");
            champPassword.requestFocus();
            return;
        }

        // Vérifier les identifiants en base
        long userId = db.verifierConnexion(username, password);

        if (userId != -1) {
            // ✅ Connexion réussie
            session.connecter(userId, username);
            textErreur.setVisibility(View.GONE);
            Toast.makeText(this, "✅ Bienvenue, " + username + " !", Toast.LENGTH_SHORT).show();
            allerVersListe();
        } else {
            // ❌ Identifiants incorrects
            textErreur.setVisibility(View.VISIBLE);
            textErreur.setText("❌ Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    /** Lance HabitListActivity et ferme LoginActivity */
    private void allerVersListe() {
        Intent intent = new Intent(this, HabitListActivity.class);
        startActivity(intent);
        finish(); // empêche de revenir en arrière vers le login
    }
}
