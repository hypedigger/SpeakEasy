package com.teacher.app.srs

import java.util.concurrent.TimeUnit

/**
 * Repetition espacee, systeme de Leitner.
 *
 * Chaque carte vit dans une boite. Une reussite la fait monter d'une boite et
 * repousse la prochaine revision ; un echec la renvoie en boite 0, ou elle
 * revient tres vite. Plus la boite est haute, plus l'intervalle est long.
 *
 * L'interface est volontairement minimale (reussite / echec, sans saisie de
 * texte) pour pouvoir remplacer l'algorithme par SM-2 ou FSRS plus tard sans
 * toucher au reste de l'application.
 */
object Leitner {

    const val MAX_BOX = 5

    /** A partir de cette boite, on considere la carte comme maitrisee. */
    const val MASTERED_BOX = 4

    /** Intervalle de revision, en jours, pour chaque boite. */
    private val INTERVAL_DAYS = intArrayOf(0, 1, 3, 7, 16, 40)

    /** Une carte ratee revient dans la meme session, apres ce delai. */
    private val RELEARN_DELAY_MS = TimeUnit.MINUTES.toMillis(10)

    data class Outcome(val box: Int, val dueAt: Long)

    fun onSuccess(currentBox: Int, now: Long): Outcome {
        val box = (currentBox + 1).coerceAtMost(MAX_BOX)
        return Outcome(box, now + TimeUnit.DAYS.toMillis(INTERVAL_DAYS[box].toLong()))
    }

    fun onFailure(now: Long): Outcome = Outcome(0, now + RELEARN_DELAY_MS)

    fun apply(success: Boolean, currentBox: Int, now: Long): Outcome =
        if (success) onSuccess(currentBox, now) else onFailure(now)

    /** Libelle lisible de l'echeance, pour l'affichage. */
    fun intervalLabel(box: Int): String = when (INTERVAL_DAYS[box.coerceIn(0, MAX_BOX)]) {
        0 -> "à revoir maintenant"
        1 -> "demain"
        else -> "dans ${INTERVAL_DAYS[box.coerceIn(0, MAX_BOX)]} jours"
    }
}
