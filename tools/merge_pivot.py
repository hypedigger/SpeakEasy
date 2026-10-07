# -*- coding: utf-8 -*-
"""Fusionne les listes existantes (russe, italien) et les chunks de mots
fréquents en une liste pivot française unique : data/pivot.json.

Dédoublonnage :
  - clé « lâche » = texte français en minuscules, sans la parenthèse de
    désambiguïsation, espaces normalisés, ponctuation finale retirée.
  - Les items existants (russe/italien) gagnent sur les chunks générés :
    ils portent déjà une traduction.
  - Entre chunks, premier arrivé gagne (ordre des fichiers).
"""
import json
import re
import unicodedata
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
# Lit les sources corrigées (data/clean) si présentes, sinon l'extraction brute
SRC = DATA / "clean" if (DATA / "clean" / "ru_vocab.json").exists() else DATA / "source"
CHUNKS = DATA / "pivot-chunks"


def loose_key(fr: str) -> str:
    s = fr.split("(")[0]
    s = s.lower().strip()
    s = re.sub(r"\s+", " ", s)
    s = s.strip(" ?!.…,;:'’")
    s = unicodedata.normalize("NFC", s)
    return s


def is_phrase(*texts) -> bool:
    for t in texts:
        if not t:
            continue
        if re.search(r"[?!]", t) or len(t.split()) >= 4:
            return True
    return False


def main():
    pivot = []
    seen = {}

    def add(fr, pos, typ, tags, origine, translations=None):
        key = loose_key(fr)
        if not key:
            return
        if key in seen:
            entry = seen[key]
            if origine not in entry["origine"]:
                entry["origine"].append(origine)
            if translations:
                entry["traductions"].update(
                    {k: v for k, v in translations.items()
                     if k not in entry["traductions"]})
            return
        entry = {
            "id": len(pivot) + 1,
            "fr": fr.strip(),
            "pos": pos,
            "type": typ,
            "tags": tags or [],
            "origine": [origine],
            "traductions": translations or {},
        }
        pivot.append(entry)
        seen[key] = entry

    # 1. Vocabulaire russe existant (priorité : il porte des traductions)
    for it in json.loads((SRC / "ru_vocab.json").read_text(encoding="utf-8")):
        add(it["fr"], None, "mot", [], "russe",
            {"ru": {"texte": it["ru"], "pron": it["pron"]}})
    for it in json.loads((SRC / "ru_autre_voc.json").read_text(encoding="utf-8")):
        add(it["fr"], None, "mot", [], "russe",
            {"ru": {"texte": it["ru"], "pron": it["pron"]}})
    for it in json.loads((SRC / "ru_chiffres.json").read_text(encoding="utf-8")):
        add(it["fr"], "num", "mot", ["nombres"], "russe",
            {"ru": {"texte": it["ru"], "pron": None}})
        if it.get("ordinal_fr") and it.get("ordinal_ru"):
            add(it["ordinal_fr"], "num", "mot", ["nombres"], "russe",
                {"ru": {"texte": it["ordinal_ru"], "pron": None}})
    for it in json.loads((SRC / "ru_phrases.json").read_text(encoding="utf-8")):
        add(it["fr"], None, "phrase", [], "russe",
            {"ru": {"texte": it["ru"], "pron": it["pron"]}})

    # 2. Vocabulaire italien existant
    for it in json.loads((SRC / "it_vocab.json").read_text(encoding="utf-8")):
        typ = "phrase" if is_phrase(it["fr"], it["it"]) else "mot"
        add(it["fr"], None, typ, [], "italien", {"it": {"texte": it["it"]}})

    n_existants = len(pivot)

    # 3. Chunks de mots fréquents générés
    for f in sorted(CHUNKS.glob("chunk_*.json")):
        try:
            items = json.loads(f.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            print(f"!! {f.name}: JSON invalide ({e})")
            continue
        for it in items:
            typ = "phrase" if it.get("pos") == "expr" else "mot"
            add(it["fr"], it.get("pos"), typ, it.get("tags"), "freq2000")

    out = DATA / "pivot.json"
    out.write_text(json.dumps(pivot, ensure_ascii=False, indent=1),
                   encoding="utf-8")
    n_freq = len(pivot) - n_existants
    print(f"pivot.json : {len(pivot)} concepts "
          f"({n_existants} issus des listes existantes, {n_freq} nouveaux freq2000)")
    mots = sum(1 for p in pivot if p["type"] == "mot")
    print(f"  mots: {mots}, phrases: {len(pivot) - mots}")
    manq_ru = sum(1 for p in pivot if "ru" not in p["traductions"])
    manq_it = sum(1 for p in pivot if "it" not in p["traductions"])
    print(f"  traductions manquantes -> russe: {manq_ru}, italien: {manq_it}")


if __name__ == "__main__":
    main()
