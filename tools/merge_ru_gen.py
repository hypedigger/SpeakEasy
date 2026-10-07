# -*- coding: utf-8 -*-
"""Intègre les sorties de génération russe (data/ru-gen/output_batch_*.json)
dans data/pivot.json : traductions.ru = {texte, pron, exemple, exemple_fr}."""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parent.parent / "data"
GEN = DATA / "ru-gen"


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
            existing = p["traductions"].get("ru") or {}
            p["traductions"]["ru"] = {
                # le texte existant (liste utilisateur) reste prioritaire
                "texte": existing.get("texte") or it.get("ru"),
                "pron": it.get("pron"),
                "exemple": it.get("ex_ru"),
                "exemple_fr": it.get("ex_fr"),
            }

    missing = [p["id"] for p in pivot if p["id"] not in seen]
    (DATA / "pivot.json").write_text(
        json.dumps(pivot, ensure_ascii=False, indent=1), encoding="utf-8")

    total = len(pivot)
    with_ru = sum(1 for p in pivot if (p["traductions"].get("ru") or {}).get("texte"))
    with_pron = sum(1 for p in pivot if (p["traductions"].get("ru") or {}).get("pron"))
    with_ex = sum(1 for p in pivot
                  if (p["traductions"].get("ru") or {}).get("exemple"))
    mots = sum(1 for p in pivot if p["type"] == "mot")
    print(f"pivot.json mis à jour : {total} concepts")
    print(f"  russe: {with_ru} traductions, {with_pron} prononciations, "
          f"{with_ex} exemples (attendu ~{mots} exemples pour les mots)")
    if missing:
        print(f"  !! {len(missing)} concepts sans sortie de génération : "
              f"{missing[:20]}{'...' if len(missing) > 20 else ''}")


if __name__ == "__main__":
    main()
