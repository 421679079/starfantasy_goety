"""Check spawn-egg registration, title data, and client resources."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/com/starfantasy/goety"
resources = root / "src/main/resources/assets/starfantasy_goety"
registry = (java / "registry/ServantSpawnEggRegistry.java").read_text("utf-8")
egg = (java / "item/StarFantasyServantSpawnEggItem.java").read_text("utf-8")
apostle = (java / "entity/ApostleServantEntity.java").read_text("utf-8")
titles = [
    "risen", "abhorrent", "defiler", "dark", "great_shadow", "witch_king",
    "pyre_lord", "profane", "cruel", "terrible", "glorious", "atrocious",
]
assert "ApostleServantEntity.TITLES[index]" in registry
assert "TITLE_COLORS[title]" in registry
assert "canOwnAnother(servantType, player)" in egg
assert "if (!canSpawnFor(player)) return InteractionResult.FAIL;" in egg
assert "if (!canSpawnFor(player)) return InteractionResultHolder.m_19100_(stack);" in egg
assert "root.m_128469_(\"EntityTag\").m_128405_(\"TitleNumber\", title)" in egg
assert "initializeTitle(tag.m_128451_(\"TitleNumber\"))" in apostle

ids = ["hades_servant", "apollyon_servant"] + [f"{title}_apostle_servant" for title in titles]
for language in ("zh_cn", "en_us"):
    names = json.loads((resources / f"lang/{language}.json").read_text("utf-8"))
    for id_ in ids:
        assert names[f"item.starfantasy_goety.{id_}_spawn_egg"]
for id_ in ids:
    model = json.loads((resources / f"models/item/{id_}_spawn_egg.json").read_text("utf-8"))
    assert model["parent"] == "minecraft:item/template_spawn_egg"
print("PASS: 14 servant eggs have names, models, title assignment, and limit guards.")
