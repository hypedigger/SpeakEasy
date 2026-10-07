# -*- coding: utf-8 -*-
"""Retablit les accents dans les textes affiches par l'application.

Les sources ont ete ecrites sans accents ; cette passe corrige les libelles
visibles par l'utilisateur (l'app est en francais).
"""
import pathlib

SRC = pathlib.Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "java"

# Remplacements ordonnes : les chaines longues d'abord pour eviter les
# substitutions partielles.
REPLACEMENTS = [
    # Boutons de reponse
    ('"  Non maitrise"', '"  Non maîtrisé"'),
    ('"  Rate"', '"  Raté"'),
    ('"  Su"', '"  Su"'),

    # Ecran de revision
    ('"Session terminee"', '"Session terminée"'),
    ('"Rien a reviser."', '"Rien à réviser."'),
    ('"Rien a reviser pour le moment"', '"Rien à réviser pour le moment"'),
    ('"$ok reussites sur $total"', '"$ok réussites sur $total"'),
    ('"${(100f * ok / total).toInt()} % de reussite"',
     '"${(100f * ok / total).toInt()} % de réussite"'),
    ('"Continuer a reviser"', '"Continuer à réviser"'),
    ('"Ecouter"', '"Écouter"'),
    ('"Nouveau mot"', '"Nouveau mot"'),
    ('"Boite $box · ${Leitner.intervalLabel(box)}"',
     '"Boîte $box · ${Leitner.intervalLabel(box)}"'),
    ('"Toutes les cartes dues sont faites et le quota de nouveaux mots "',
     '"Toutes les cartes dues sont faites et le quota de nouveaux mots "'),
    ('"du jour est atteint. Revenez plus tard, ou augmentez le quota dans le profil."',
     '"du jour est atteint. Revenez plus tard, ou augmentez le quota dans le profil."'),

    # Accueil
    ('"Vous voyez le mot francais, vous le retrouvez"',
     '"Vous voyez le mot français, vous le retrouvez"'),
    ('"Progression suivie separement de l\'autre sens"',
     '"Progression suivie séparément de l\'autre sens"'),
    ('"Les phrases se retiennent mieux que les mots isoles"',
     '"Les phrases se retiennent mieux que les mots isolés"'),
    ('"Comprehension a l\'ecoute et a la lecture"',
     '"Compréhension à l\'écoute et à la lecture"'),
    ('"Choisissez un temps et revisez les verbes"',
     '"Choisissez un temps et révisez les verbes"'),
    ('"Regles de grammaire"', '"Règles de grammaire"'),
    ('"Cas, genres, pluriels, pronoms, adjectifs"',
     '"Cas, genres, pluriels, pronoms, adjectifs"'),
    ('"Avancement, points faibles et reglages"',
     '"Avancement, points faibles et réglages"'),
    ('"Francais → ${language.label}"', '"Français → ${language.label}"'),
    ('"${language.label} → Francais"', '"${language.label} → Français"'),
    ('"Mots · Francais → ${language.label}"', '"Mots · Français → ${language.label}"'),
    ('"Mots · ${language.label} → Francais"', '"Mots · ${language.label} → Français"'),
    ('"Phrases · Francais → ${language.label}"', '"Phrases · Français → ${language.label}"'),
    ('"Phrases · ${language.label} → Francais"', '"Phrases · ${language.label} → Français"'),

    # Choix de langue
    ('"Quelle langue voulez-vous travailler ?"', '"Quelle langue voulez-vous travailler ?"'),
    ('"Contenu a venir"', '"Contenu à venir"'),

    # Conjugaison
    ('"Choisissez le temps a reviser. On vous montre un verbe, "',
     '"Choisissez le temps à réviser. On vous montre un verbe, "'),
    ('"vous recitez mentalement, puis vous verifiez le tableau complet."',
     '"vous récitez mentalement, puis vous vérifiez le tableau complet."'),
    ('"Aucun verbe a reviser pour ce temps."', '"Aucun verbe à réviser pour ce temps."'),
    ('"Voir tous les temps de ce verbe"', '"Voir tous les temps de ce verbe"'),
    ('"feminin"', '"féminin"'),

    # Profil
    ('"Serie"', '"Série"'),
    ('"Aujourd\'hui"', '"Aujourd\'hui"'),
    ('"Reglages"', '"Réglages"'),
    ('"Repartition par boite"', '"Répartition par boîte"'),
    ('"Remettre la progression a zero"', '"Remettre la progression à zéro"'),
    ('"Tout remettre a zero ?"', '"Tout remettre à zéro ?"'),
    ('"Synthese vocale"', '"Synthèse vocale"'),
    ('"Lire automatiquement la reponse"', '"Lire automatiquement la réponse"'),
    ('"$mastered maitrises · $started commences · $total au total"',
     '"$mastered maîtrisés · $started commencés · $total au total"'),
    ('"Un plafond bas evite l\'accumulation de revisions : c\'est "',
     '"Un plafond bas évite l\'accumulation de révisions : c\'est "'),
    ('"la cle d\'un systeme de repetition espacee qui tient dans la duree."',
     '"la clé d\'un système de répétition espacée qui tient dans la durée."'),
    ('"Toute votre progression en ${language.label} sera effacee : " +',
     '"Toute votre progression en ${language.label} sera effacée : " +'),
    ('"boites, historique et statistiques. Le contenu, lui, est conserve."',
     '"boîtes, historique et statistiques. Le contenu, lui, est conservé."'),

    # Categories de grammaire
    ('"Alphabet et prononciation"', '"Alphabet et prononciation"'),

    # Leitner
    ('"a revoir maintenant"', '"à revoir maintenant"'),
    ('"dans ${INTERVAL_DAYS[box.coerceIn(0, MAX_BOX)]} jours"',
     '"dans ${INTERVAL_DAYS[box.coerceIn(0, MAX_BOX)]} jours"'),
]


def main():
    changed = 0
    for path in SRC.rglob("*.kt"):
        text = path.read_text(encoding="utf-8")
        original = text
        for old, new in REPLACEMENTS:
            text = text.replace(old, new)
        if text != original:
            path.write_text(text, encoding="utf-8")
            changed += 1
            print(f"  {path.name}")
    print(f"{changed} fichiers mis a jour")


if __name__ == "__main__":
    main()
