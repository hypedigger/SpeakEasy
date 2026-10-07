# -*- coding: utf-8 -*-
"""Assemble les JSON de data/ vers app/src/main/assets/ (contenu embarque)."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "data"
ASSETS = ROOT / "app" / "src" / "main" / "assets"
ASSETS.mkdir(parents=True, exist_ok=True)


def write(name, obj):
    path = ASSETS / name
    path.write_text(json.dumps(obj, ensure_ascii=False, separators=(",", ":")),
                    encoding="utf-8")
    kb = path.stat().st_size / 1024
    print(f"  {name}: {len(obj)} items, {kb:.0f} Ko")


def main():
    print("assets ->", ASSETS)

    pivot = json.loads((DATA / "pivot.json").read_text(encoding="utf-8"))
    write("pivot.json", pivot)

    build_verbs("ru-verbs", "ru_verbes.json")
    build_verbs("it-verbs", "it_verbes.json")

    # le frioulan est produit en un seul fichier, pas en lots
    fur = DATA / "fur-verbes.json"
    if fur.exists():
        write("fur_verbes.json", json.loads(fur.read_text(encoding="utf-8")))
    else:
        print("  fur_verbes.json : pas encore genere")

    for lang, src in (("ru", "ru-grammaire.json"), ("it", "it-grammaire.json"),
                      ("fur", "fur-grammaire.json")):
        gram_file = DATA / src
        if gram_file.exists():
            write(f"{lang}_grammaire.json",
                  json.loads(gram_file.read_text(encoding="utf-8")))
        else:
            print(f"  {lang}_grammaire.json : pas encore genere")


def build_verbs(folder, out_name):
    src = DATA / folder
    if not src.exists():
        print(f"  {out_name} : pas encore genere")
        return
    verbs, seen = [], set()
    for f in sorted(src.glob("output_verbs_*.json")):
        for v in json.loads(f.read_text(encoding="utf-8")):
            # les agents ont signale quelques non-verbes : on les ecarte
            tables = ("present", "passe", "imparfait", "futur",
                      "conditionnel", "subjonctif")
            if not any(v.get(t) for t in tables):
                continue
            if v["id"] in seen:
                continue
            seen.add(v["id"])
            verbs.append(v)
    verbs.sort(key=lambda v: v["id"])
    write(out_name, verbs)


if __name__ == "__main__":
    main()
