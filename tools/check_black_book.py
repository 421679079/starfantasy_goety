"""Check editable Black Book pages against the actual packaged ritual recipes."""
import argparse
import json
import struct
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
BOOK = "assets/goety/patchouli_books/black_book"


def check(read, names):
    def document(path):
        return json.loads(read(path).decode("utf-8-sig"))

    recipe_files = {
        f"{parts[1]}:{'/'.join(parts[3:])[:-5]}": path
        for path in names
        if path.startswith("data/") and path.endswith(".json")
        and len(parts := path.split("/")) >= 4 and parts[2] == "recipes"
    }
    language_entries = {}
    for language in ("zh_cn", "en_us"):
        category = document(f"{BOOK}/{language}/categories/starfantasy.json")
        assert category["name"] and category["description"]
        assert category["icon"] == "starfantasy_goety:guard_focus"
        prefix = f"{BOOK}/{language}/entries/starfantasy/"
        entries = {path[len(prefix):]: document(path)
                   for path in names if path.startswith(prefix) and path.endswith(".json")}
        assert entries, f"No {language} entries"
        referenced = []
        mappings = set()
        for filename, entry in entries.items():
            context = f"{language}/{filename}"
            assert entry["name"] and entry["icon"], context
            assert entry["category"] == "goety:starfantasy", context
            for page in entry["pages"]:
                if page["type"] == "patchouli:text":
                    assert page["text"].strip(), context
                elif page["type"] == "patchouli:image":
                    assert page["images"], context
                    for image in page["images"]:
                        namespace, path = image.split(":", 1)
                        png = read(f"assets/{namespace}/{path}")
                        assert png[:8] == b"\x89PNG\r\n\x1a\n", (context, image)
                        assert struct.unpack(">II", png[16:24]) == (256, 256), (context, image)
                else:
                    assert page["type"] == "goety:ritual", context
                    recipe_id = page["recipe"]
                    assert recipe_id in recipe_files, (context, "Missing recipe", recipe_id)
                    recipe = document(recipe_files[recipe_id])
                    assert recipe["type"] == "goety:ritual", recipe_id
                    assert len(recipe["ingredients"]) <= 12, (recipe_id, "Template has 12 pedestals")
                    referenced.append(recipe_id)
            for item, page in entry.get("extra_recipe_mappings", {}).items():
                assert item not in mappings, (context, "Duplicate item shortcut", item)
                assert isinstance(page, int) and 0 <= page < len(entry["pages"]), context
                assert entry["pages"][page]["type"] == "goety:ritual", context
                mappings.add(item)
        assert set(referenced) == set(recipe_files), (
            language, "Missing or unexpected recipes", set(recipe_files) ^ set(referenced))
        assert len(referenced) == len(set(referenced)), (language, "Duplicate recipe pages")
        language_entries[language] = entries
        print(f"{language}: {len(entries)} entries, {len(referenced)} valid ritual recipes")

    chinese, english = language_entries["zh_cn"], language_entries["en_us"]
    assert chinese.keys() == english.keys(), "Language entry files differ"
    for filename, zh in chinese.items():
        en = english[filename]
        for field in ("icon", "category", "sortnum", "extra_recipe_mappings"):
            assert zh.get(field) == en.get(field), (filename, field)
        structure = lambda entry: [(p["type"], p.get("recipe"), p.get("images"), p.get("border"))
                                   for p in entry["pages"]]
        assert structure(zh) == structure(en), (filename, "Language page order differs")
    print("Category references, page links and bilingual page order are valid.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, help="Validate a built JAR instead of source resources")
    args = parser.parse_args()
    if args.jar:
        with ZipFile(args.jar) as jar:
            check(jar.read, jar.namelist())
    else:
        resources = ROOT / "src/main/resources"
        names = [path.relative_to(resources).as_posix() for path in resources.rglob("*.json")]
        check(lambda path: (resources / path).read_bytes(), names)


if __name__ == "__main__":
    main()
