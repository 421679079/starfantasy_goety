"""Check the three focus rituals, duplicate ingredients and bilingual book links."""
from collections import Counter
from pathlib import Path
import json
import sys
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
EXPECTED = {
    "final_art_focus": ("end", "goety:rupture_focus", [
        "goety:void_echo", "goety:void_echo", "goety:void_eye", "starfantasy_goety:halo_of_the_dark"]),
    "blooms_and_plumes_focus": ("magic", "goety:sonic_boom_focus", [
        "starfantasy_goety:halo_of_hades", "goety:arrow_rain_focus", "goety:mystic_core", "goety:corruption_focus"]),
    "evernight_focus": ("necroturgy", "goety:ghost_fire_focus", [
        "starfantasy_goety:halo_of_the_risen", "minecraft:wither_rose", "minecraft:wither_rose", "goety:cryptic_eye"]),
}


def verify(read):
    for name, (school, center, ingredients) in EXPECTED.items():
        recipe = json.loads(read(f"data/starfantasy_goety/recipes/{name}.json"))
        assert recipe["type"] == "goety:ritual" and recipe["ritual_type"] == "goety:craft"
        assert recipe["craftType"] == school and recipe["activation_item"] == {"item": center}
        assert recipe["soulCost"] == 1 and recipe["duration"] == 10
        assert Counter(i["item"] for i in recipe["ingredients"]) == Counter(ingredients)
        assert recipe["result"] == {"item": f"starfantasy_goety:{name}", "count": 1}
        model = json.loads(read(f"assets/starfantasy_goety/models/item/{name}.json"))
        assert model["textures"]["layer0"] == f"starfantasy_goety:item/{name}"
        assert read(f"assets/starfantasy_goety/textures/item/{name}.png").startswith(b"\x89PNG")
        for locale in ["zh_cn", "en_us"]:
            book = json.loads(read(f"assets/goety/patchouli_books/black_book/{locale}/entries/starfantasy/{name}.json"))
            lang = json.loads(read(f"assets/starfantasy_goety/lang/{locale}.json"))
            assert book["icon"] == f"starfantasy_goety:{name}"
            assert book["name"] == lang[f"item.starfantasy_goety.{name}"]
            assert book["pages"][0]["text"] == lang[f"item.starfantasy_goety.{name}.info"].replace("\n", "$(br2)")
            assert book["pages"][1] == {"type": "goety:ritual", "recipe": f"starfantasy_goety:{name}"}
            assert book["extra_recipe_mappings"][f"starfantasy_goety:{name}"] == 1


verify(lambda path: (RESOURCES / path).read_bytes())
registry = (ROOT / "src/mojang/java/com/starfantasy/goety/magic/focus/BattleFocusContent.java").read_text("utf-8")
for name in EXPECTED:
    assert f'ITEMS.register("{name}"' in registry
with ZipFile(ROOT.parent / "private-deps/goety/goety-2.5.56.5.jar") as goety:
    for _, center, ingredients in EXPECTED.values():
        for item in [center, *ingredients]:
            namespace, name = item.split(":")
            if namespace == "goety":
                assert f"assets/goety/models/item/{name}.json" in goety.namelist(), item
            elif namespace == "starfantasy_goety":
                assert (RESOURCES / f"assets/starfantasy_goety/models/item/{name}.json").is_file(), item
            else:
                assert item == "minecraft:wither_rose", item
if len(sys.argv) > 1:
    with ZipFile(sys.argv[1]) as release:
        verify(release.read)
        for old in ["flower_arrow_dance_focus", "final_art"]:
            assert f'ITEMS.register("{old}"' not in registry
            assert not any(path.endswith((f"/{old}.json", f"/{old}.png")) for path in release.namelist())
    print("PASS: release jar contains all three rituals and matching bilingual book pages")
print("PASS: end/magic/necroturgy rituals, 1 soul/second, 10 seconds, centers and all duplicate ingredients")
