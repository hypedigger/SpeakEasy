# -*- coding: utf-8 -*-
"""Applique les corrections validées (data/review/*.json) aux extractions
data/source/*.json et écrit le résultat dans data/clean/*.json."""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
SRC, REVIEW, CLEAN = DATA / "source", DATA / "review", DATA / "clean"
CLEAN.mkdir(exist_ok=True)


def load(p):
    return json.loads(p.read_text(encoding="utf-8"))


def save(name, obj):
    (CLEAN / name).write_text(json.dumps(obj, ensure_ascii=False, indent=1),
                              encoding="utf-8")


def main():
    applied = 0

    # --- Vocabulaire russe ---
    voc = load(SRC / "ru_vocab.json")
    for f in ["corrections_ru_vocab_1.json", "corrections_ru_vocab_2.json"]:
        for c in load(REVIEW / f):
            row = voc[c["index"]]
            if row["ru"] != c["ru"]:
                print(f"!! décalage {f} index {c['index']}: {row['ru']!r} != {c['ru']!r}")
                continue
            if c.get("ru_corrige"):
                row["ru"] = c["ru_corrige"]
            if c.get("fr_corrige"):
                row["fr"] = c["fr_corrige"]
            applied += 1
    save("ru_vocab.json", voc)

    # --- Phrases / chiffres / autre voc russes ---
    files = {"phrases": load(SRC / "ru_phrases.json"),
             "chiffres": load(SRC / "ru_chiffres.json"),
             "autre_voc": load(SRC / "ru_autre_voc.json")}
    for c in load(REVIEW / "corrections_ru_phrases.json"):
        rows = files[c["fichier"]]
        row = rows[c["index"]]
        if c["fichier"] == "chiffres" and row.get("ru") != c["ru"] \
                and row.get("ordinal_ru") == c["ru"]:
            if c.get("ru_corrige"):
                row["ordinal_ru"] = c["ru_corrige"]
            if c.get("fr_corrige"):
                row["ordinal_fr"] = c["fr_corrige"]
            applied += 1
            continue
        if row.get("ru") not in (c["ru"], None):
            print(f"!! décalage {c['fichier']} index {c['index']}: "
                  f"{row.get('ru')!r} != {c['ru']!r}")
            continue
        if c.get("ru_corrige"):
            row["ru"] = c["ru_corrige"]
        if c.get("fr_corrige"):
            row["fr"] = c["fr_corrige"]
        applied += 1
    save("ru_phrases.json", files["phrases"])
    save("ru_chiffres.json", files["chiffres"])
    save("ru_autre_voc.json", files["autre_voc"])

    # --- Vocabulaire italien ---
    it = load(SRC / "it_vocab.json")
    for f in ["corrections_it_1.json", "corrections_it_2.json"]:
        for c in load(REVIEW / f):
            row = it[c["index"]]
            if row["it"] != c["it"]:
                print(f"!! décalage {f} index {c['index']}: {row['it']!r} != {c['it']!r}")
                continue
            if c.get("it_corrige"):
                row["it"] = c["it_corrige"]
            if c.get("fr_corrige"):
                row["fr"] = c["fr_corrige"]
            applied += 1
    save("it_vocab.json", it)

    print(f"{applied} corrections appliquées -> data/clean/")


if __name__ == "__main__":
    main()
