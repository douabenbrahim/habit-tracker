package com.example.habitbreaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * ══════════════════════════════════════════════════════════
 *  CLASSE : DatabaseHelper  (SQLiteOpenHelper)
 * ══════════════════════════════════════════════════════════
 *
 * C'est le "chef d'orchestre" de la base de données SQLite.
 * Elle crée les tables, met à jour la structure, et fournit
 * des méthodes pour lire/écrire les données.
 *
 * TABLES :
 *   1. users   → stocke les comptes (id, username, password)
 *   2. habits  → stocke les habitudes liées à chaque user
 *
 * UTILISATION (exemple) :
 *   DatabaseHelper db = new DatabaseHelper(context);
 *   db.insererUtilisateur("alice", "1234");
 *   ArrayList<Habit> liste = db.chargerHabitudes(userId);
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── Informations de la base ───────────────────────────────
    private static final String NOM_BASE      = "habitbreaker.db";
    private static final int    VERSION_BASE  = 1;

    // ── Table USERS ───────────────────────────────────────────
    public static final String TABLE_USERS       = "users";
    public static final String COL_USER_ID       = "id";
    public static final String COL_USERNAME      = "username";
    public static final String COL_PASSWORD      = "password";

    // ── Table HABITS ──────────────────────────────────────────
    public static final String TABLE_HABITS      = "habits";
    public static final String COL_HABIT_ID      = "id";
    public static final String COL_HABIT_USER_ID = "user_id";   // clé étrangère → users.id
    public static final String COL_HABIT_NOM     = "habit_name";
    public static final String COL_HABIT_STREAK  = "streak";
    public static final String COL_HABIT_MEILLEUR= "meilleur_streak";
    public static final String COL_HABIT_COMPTEUR= "compteur";
    public static final String COL_HABIT_OBJECTIF= "objectif";

    // ── Constructeur ──────────────────────────────────────────
    public DatabaseHelper(Context context) {
        super(context, NOM_BASE, null, VERSION_BASE);
    }

    /**
     * onCreate() est appelé UNE SEULE FOIS lors de la première ouverture.
     * C'est ici qu'on crée les tables SQL.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Créer la table des utilisateurs
        String creerTableUsers =
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_USER_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USERNAME + " TEXT NOT NULL UNIQUE, " +   // username unique !
            COL_PASSWORD + " TEXT NOT NULL" +
            ");";

        // Créer la table des habitudes
        String creerTableHabits =
            "CREATE TABLE " + TABLE_HABITS + " (" +
            COL_HABIT_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_HABIT_USER_ID  + " INTEGER NOT NULL, " +
            COL_HABIT_NOM      + " TEXT NOT NULL, " +
            COL_HABIT_STREAK   + " INTEGER DEFAULT 0, " +
            COL_HABIT_MEILLEUR + " INTEGER DEFAULT 0, " +
            COL_HABIT_COMPTEUR + " INTEGER DEFAULT 0, " +
            COL_HABIT_OBJECTIF + " INTEGER DEFAULT 7, " +
            "FOREIGN KEY (" + COL_HABIT_USER_ID + ") REFERENCES " +
            TABLE_USERS + "(" + COL_USER_ID + ")" +
            ");";

        db.execSQL(creerTableUsers);
        db.execSQL(creerTableHabits);
    }

    /**
     * onUpgrade() est appelé si on change VERSION_BASE.
     * Pour simplifier on recrée tout (acceptable pour un projet universitaire).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ════════════════════════════════════════════════════════════
    //  MÉTHODES UTILISATEURS
    // ════════════════════════════════════════════════════════════

    /**
     * Insère un nouvel utilisateur dans la base.
     * Retourne l'id généré, ou -1 si erreur (username déjà pris).
     */
    public long insererUtilisateur(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valeurs = new ContentValues();
        valeurs.put(COL_USERNAME, username.trim());
        valeurs.put(COL_PASSWORD, password);
        long id = db.insert(TABLE_USERS, null, valeurs);
        db.close();
        return id;
    }

    /**
     * Vérifie les identifiants de connexion.
     * Retourne l'id de l'utilisateur si valide, -1 sinon.
     */
    public long verifierConnexion(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COL_USER_ID},
            COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
            new String[]{username.trim(), password},
            null, null, null
        );
        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
        }
        cursor.close();
        db.close();
        return userId;
    }

    /**
     * Vérifie si un username existe déjà (pour éviter les doublons).
     */
    public boolean usernameExiste(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COL_USER_ID},
            COL_USERNAME + " = ?",
            new String[]{username.trim()},
            null, null, null
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    // ════════════════════════════════════════════════════════════
    //  MÉTHODES HABITUDES
    // ════════════════════════════════════════════════════════════

    /**
     * Charge toutes les habitudes d'un utilisateur donné.
     * Chaque habitude est reconstruite depuis la ligne SQL.
     */
    public ArrayList<Habit> chargerHabitudes(long userId) {
        ArrayList<Habit> liste = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
            TABLE_HABITS,
            null,  // toutes les colonnes
            COL_HABIT_USER_ID + " = ?",
            new String[]{String.valueOf(userId)},
            null, null, null
        );

        while (cursor.moveToNext()) {
            // Lire chaque colonne et reconstruire l'objet Habit
            long   id        = cursor.getLong(cursor.getColumnIndexOrThrow(COL_HABIT_ID));
            String nom       = cursor.getString(cursor.getColumnIndexOrThrow(COL_HABIT_NOM));
            int    streak    = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HABIT_STREAK));
            int    meilleur  = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HABIT_MEILLEUR));
            int    compteur  = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HABIT_COMPTEUR));
            int    objectif  = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HABIT_OBJECTIF));

            Habit h = new Habit(nom);
            h.setDbId(id);
            h.forcerStreak(streak);
            h.forcerMeilleureStreak(meilleur);
            h.forcerCompteur(compteur);
            h.setObjectif(objectif);

            liste.add(h);
        }

        cursor.close();
        db.close();
        return liste;
    }

    /**
     * Insère une nouvelle habitude pour un utilisateur.
     * Retourne l'id SQLite généré.
     */
    public long insererHabitude(long userId, Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valeurs = new ContentValues();
        valeurs.put(COL_HABIT_USER_ID,  userId);
        valeurs.put(COL_HABIT_NOM,      habit.getNom());
        valeurs.put(COL_HABIT_STREAK,   habit.getStreak());
        valeurs.put(COL_HABIT_MEILLEUR, habit.getMeilleureStreak());
        valeurs.put(COL_HABIT_COMPTEUR, habit.getCompteur());
        valeurs.put(COL_HABIT_OBJECTIF, habit.getObjectif());
        long id = db.insert(TABLE_HABITS, null, valeurs);
        db.close();
        return id;
    }

    /**
     * Met à jour une habitude existante (après résistance ou rechute).
     */
    public void mettreAJourHabitude(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valeurs = new ContentValues();
        valeurs.put(COL_HABIT_NOM,      habit.getNom());
        valeurs.put(COL_HABIT_STREAK,   habit.getStreak());
        valeurs.put(COL_HABIT_MEILLEUR, habit.getMeilleureStreak());
        valeurs.put(COL_HABIT_COMPTEUR, habit.getCompteur());
        valeurs.put(COL_HABIT_OBJECTIF, habit.getObjectif());
        db.update(TABLE_HABITS, valeurs, COL_HABIT_ID + " = ?",
                  new String[]{String.valueOf(habit.getDbId())});
        db.close();
    }

    /**
     * Supprime une habitude par son id SQLite.
     */
    public void supprimerHabitude(long habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABITS, COL_HABIT_ID + " = ?",
                  new String[]{String.valueOf(habitId)});
        db.close();
    }
}
