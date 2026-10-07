# -*- coding: utf-8 -*-
"""Génère data/review/CORRECTIONS-A-VALIDER.md à partir des fichiers de
corrections JSON, pour validation par l'utilisateur."""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
REVIEW = DATA / "review"

SECTIONS = [
    ("corrections_ru_vocab_1.json", "Vocabulaire russe (1/2)", "ru"),
    ("corrections_ru_vocab_2.json", "Vocabulaire russe (2/2)", "ru"),
    ("corrections_ru_phrases.json", "Phrases, chiffres et divers russes", "ru"),
    ("corrections_it_1.json", "Vocabulaire italien (1/2)", "it"),
    ("corrections_it_2.json", "Vocabulaire italien (2/2)", "it"),
]


def esc(s):
    return "" if s is None else str(s).replace("|", "\\|").replace("\n", " ")


def main():
    lines = [
        "# Corrections proposées — à valider",
        "",
        "Chaque ligne : texte original → correction proposée. Une case vide = pas de changement.",
        "Pour valider : dites « applique tout » ou listez les numéros à NE PAS appliquer.",
        "",
    ]
    total = 0
    num = 0
    for fname, title, lang in SECTIONS:
        f = REVIEW / fname
        if not f.exists():
            lines += [f"## {title}", "", "_(fichier manquant)_", ""]
            continue
        data = json.loads(f.read_text(encoding="utf-8"))
        total += len(data)
        lines += [f"## {title} — {len(data)} corrections", ""]
        lines.append(f"| n° | {lang} original | {lang} corrigé | fr original | fr corrigé | raison |")
        lines.append("|---|---|---|---|---|---|")
        for x in data:
            num += 1
            orig = x.get(lang) or x.get("ru") or x.get("it")
            corr = x.get(f"{lang}_corrige")
            lines.append(
                f"| {num} | {esc(orig)} | {esc(corr)} | {esc(x.get('fr'))} "
                f"| {esc(x.get('fr_corrige'))} | {esc(x.get('raison'))} |")
        lines.append("")
    out = REVIEW / "CORRECTIONS-A-VALIDER.md"
    out.write_text("\n".join(lines), encoding="utf-8")
    print(f"{out} : {total} corrections")


if __name__ == "__main__":
    main()
