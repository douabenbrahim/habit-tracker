package com.example.habitbreaker;

import java.io.Serializable;

/**
 * MODÈLE : Habit (Version 3 — avec SQLite)
 *
 * Représente une mauvaise habitude à briser.
 *
 * Nouveauté V3 :
 *   dbId  → l'identifiant dans la table SQLite
 *           Permet de faire UPDATE ou DELETE sur la bonne ligne.
 *
 * Champs :
 *   dbId             → id dans la base SQLite (0 si pas encore inséré)
 *   nom              → nom de l'habitude
 *   compteur         → nombre de rechutes
 *   streak           → jours consécutifs de résistance
 *   meilleureStreak  → meilleur streak historique
 *   objectif         → cible en jours fixée par l'utilisateur
 */
public class Habit implements Serializable {

    private long   dbId;           // ← NOUVEAU : lien avec SQLite
    private String nom;
    private int    compteur;
    private int    streak;
    private int    meilleureStreak;
    private int    objectif;

    // ── Constructeur ──────────────────────────────────────────
    public Habit(String nom) {
        this.dbId            = 0;   // 0 = pas encore en base
        this.nom             = nom;
        this.compteur        = 0;
        this.streak          = 0;
        this.meilleureStreak = 0;
        this.objectif        = 7;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public long   getDbId()               { return dbId; }
    public void   setDbId(long dbId)      { this.dbId = dbId; }
    public String getNom()                { return nom; }
    public void   setNom(String nom)      { this.nom = nom; }
    public int    getCompteur()           { return compteur; }
    public int    getStreak()             { return streak; }
    public int    getMeilleureStreak()    { return meilleureStreak; }
    public int    getObjectif()           { return objectif; }
    public void   setObjectif(int o)      { this.objectif = o; }

    // ── Méthodes de restauration (appelées par DatabaseHelper) ─
    /** Force la valeur du streak sans passer par resiste() */
    public void forcerStreak(int valeur) {
        this.streak = valeur;
    }

    /** Force le meilleur streak lors de la restauration */
    public void forcerMeilleureStreak(int valeur) {
        this.meilleureStreak = valeur;
    }

    /** Force le compteur de rechutes lors de la restauration */
    public void forcerCompteur(int valeur) {
        this.compteur = valeur;
    }

    // ── Actions ───────────────────────────────────────────────

    /**
     * L'utilisateur a résisté aujourd'hui.
     * Streak +1, met à jour le record si battu.
     */
    public void resiste() {
        this.streak++;
        if (this.streak > this.meilleureStreak) {
            this.meilleureStreak = this.streak;
        }
    }

    /**
     * L'utilisateur a craqué.
     * Streak → 0, compteur de rechutes +1.
     */
    public void echoue() {
        this.streak = 0;
        this.compteur++;
    }

    /** Réinitialise le compteur de rechutes */
    public void reinitialiserCompteur() {
        this.compteur = 0;
    }

    // ── Progression vers l'objectif ───────────────────────────

    /** Progression en % pour la ProgressBar (0–100) */
    public int getProgressionPourcentage() {
        if (objectif <= 0) return 0;
        return Math.min((streak * 100) / objectif, 100);
    }

    /** Vrai si l'objectif est atteint ou dépassé */
    public boolean objectifAtteint() {
        return streak >= objectif;
    }

    @Override
    public String toString() {
        return nom;
    }
}
