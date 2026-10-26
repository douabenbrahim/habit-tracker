package com.example.habitbreaker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ══════════════════════════════════════════════════════════
 *  CLASSE : SessionManager
 * ══════════════════════════════════════════════════════════
 *
 * Gère la session de l'utilisateur connecté.
 * Stocke l'id et le username dans SharedPreferences pour que
 * l'utilisateur reste connecté même après fermeture de l'app.
 *
 * UTILISATION :
 *   SessionManager session = new SessionManager(context);
 *   session.connecter(userId, username);   // après login réussi
 *   long id = session.getUserId();         // dans les autres activités
 *   session.deconnecter();                 // bouton déconnexion
 */
public class SessionManager {

    private static final String PREFS_SESSION = "session_prefs";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_USERNAME  = "username";
    private static final long   DECONNECTE    = -1L;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /** Enregistre la session après une connexion réussie */
    public void connecter(long userId, String username) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    /** Efface la session (déconnexion) */
    public void deconnecter() {
        editor.putLong(KEY_USER_ID, DECONNECTE);
        editor.putString(KEY_USERNAME, "");
        editor.apply();
    }

    /** Retourne true si un utilisateur est connecté */
    public boolean estConnecte() {
        return prefs.getLong(KEY_USER_ID, DECONNECTE) != DECONNECTE;
    }

    /** Retourne l'id SQLite de l'utilisateur connecté */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, DECONNECTE);
    }

    /** Retourne le username de l'utilisateur connecté */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
}
