# -*- coding: utf-8 -*-
"""Integre les sorties de generation italienne dans data/pivot.json."""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
GEN = DATA / "it-gen"


def main():
    pivot = json.loads((DATA / "pivot.json").read_text(encoding="utf-8"))
    by_id = {p["id"]: p for p in pivot}

    seen = set()
    for f in sorted(GEN.glob("output_batch_*.json")):
        try:
            items = json.loads(f.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            print(f"!! {f.name}: JSON invalide ({e})")
            continue
        for it in items:
            cid = it["id"]
            if cid not in by_id:
                print(f"!! {f.name}: id inconnu {cid}")
                continue
            seen.add(cid)
            p = by_id[cid]
            existing = p["traductions"].get("it") or {}
            p["traductions"]["it"] = {
                # la liste personnelle de l'utilisateur reste prioritaire
                "texte": existing.get("texte") or it.get("it"),
                "pron": it.get("pron"),
                "exemple": it.get("ex_it"),
                "exemple_fr": it.get("ex_fr"),
            }

    (DATA / "pivot.json").write_text(
        json.dumps(pivot, ensure_ascii=False, indent=1), encoding="utf-8")

    total = len(pivot)
    with_it = sum(1 for p in pivot if (p["traductions"].get("it") or {}).get("texte"))
    with_pron = sum(1 for p in pivot if (p["traductions"].get("it") or {}).get("pron"))
    with_ex = sum(1 for p in pivot if (p["traductions"].get("it") or {}).get("exemple"))
    mots = sum(1 for p in pivot if p["type"] == "mot")
    print(f"pivot.json : {total} concepts")
    print(f"  italien : {with_it} traductions, {with_pron} prononciations, "
          f"{with_ex} exemples (attendu ~{mots})")
    missing = [p["id"] for p in pivot if p["id"] not in seen]
    if missing:
        print(f"  !! {len(missing)} concepts sans sortie : {missing[:20]}")


if __name__ == "__main__":
    main()
