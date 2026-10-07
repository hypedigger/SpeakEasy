# -*- coding: utf-8 -*-
"""Integre le frioulan (Pordenone) dans data/pivot.json.

Seules les entrees marquees "sur" par les agents sont retenues : la consigne
est de ne proposer que du contenu fiable a une locutrice native.
"""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
GEN = DATA / "fur-gen"


def main():
    pivot = json.loads((DATA / "pivot.json").read_text(encoding="utf-8"))
    by_id = {p["id"]: p for p in pivot}

    retenus = ecartes = 0
    for f in sorted(GEN.glob("output_batch_*.json")):
        try:
            items = json.loads(f.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            print(f"!! {f.name}: JSON invalide ({e})")
            continue
        for it in items:
            if it.get("confiance") != "sur":
                ecartes += 1
                continue
            p = by_id.get(it["id"])
            texte = (it.get("fur") or "").strip()
            if not p or not texte:
                continue
            p["traductions"]["fur"] = {
                "texte": texte,
                "pron": None,          # pas de translitteration : alphabet latin
                "exemple": it.get("ex_fur"),
                "exemple_fr": it.get("ex_fr"),
                "note": it.get("note_variante"),
            }
            retenus += 1

    (DATA / "pivot.json").write_text(
        json.dumps(pivot, ensure_ascii=False, indent=1), encoding="utf-8")

    ex = sum(1 for p in pivot if (p["traductions"].get("fur") or {}).get("exemple"))
    notes = sum(1 for p in pivot if (p["traductions"].get("fur") or {}).get("note"))
    print(f"frioulan : {retenus} entrees retenues, {ecartes} ecartees (confiance insuffisante)")
    print(f"  dont {ex} avec phrase d'exemple, {notes} avec note de variante")


if __name__ == "__main__":
    main()
