"""Validate all ritual variants and their links against the packaged release, without launching Minecraft."""
from pathlib import Path
import json
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
jar = Path(sys.argv[1]) if len(sys.argv) > 1 else root / 'build/releases/starfantasy_goety-0.7.4-clean.jar'
titles = ['risen', 'abhorrent', 'defiler', 'dark', 'great_shadow', 'witch_king',
          'pyre_lord', 'profane', 'cruel', 'terrible', 'glorious', 'atrocious']
with zipfile.ZipFile(jar) as z:
    def read(name):
        return json.loads(z.read(name))
    names = set(z.namelist())
    found = {n for n in names if n.startswith('data/starfantasy_goety/recipes/summon_apostle_servant_')}
    assert len(found) == 12
    for title in titles:
        r = read(f'data/starfantasy_goety/recipes/summon_apostle_servant_{title}.json')
        assert r['type'] == 'goety:ritual'
        assert r['ritual_type'] == 'starfantasy_goety:summon_apostle_servant'
        assert r['entity_to_summon'] == 'starfantasy_goety:apostle_servant'
        assert r['activation_item'] == {'item': f'starfantasy_goety:halo_of_the_{title}'}
        assert r['craftType'] == 'sabbath' and r['soulCost'] == 1 and r['duration'] == 10
        assert r['summonLife'] == -1 and 'entity_to_sacrifice' not in r
        assert sorted(i['item'] for i in r['ingredients']) == ['goety:unholy_hat', 'goety:unholy_robe', 'minecraft:bow']
    for lang, label in [('zh_cn', '\u4f7f\u5f92\u4ec6\u4ece'), ('en_us', 'Apostle Servant')]:
        d = read(f'assets/starfantasy_goety/lang/{lang}.json')
        assert d['entity.starfantasy_goety.apostle_servant'] == label
        for title in titles:
            assert d[f'entity.starfantasy_goety.apostle.title.{title}'].strip()
        book = read(f'assets/goety/patchouli_books/black_book/{lang}/entries/starfantasy/apostle_servants.json')
        for page in book['pages']:
            if 'recipe' in page:
                ns, name = page['recipe'].split(':')
                assert f'data/{ns}/recipes/{name}.json' in names
    assert 'ApostleServantBehaviorMixin' not in read('starfantasy_goety.mixins.json')['mixins']
    assert 'com/starfantasy/goety/entity/ApostleServantEntity.class' in names
    for title in titles:
        assert f'assets/starfantasy_goety/geo/entity/apostle/apostle_the_{title}.geo.json' in names
        assert f'assets/starfantasy_goety/textures/entity/apostle/apostle_the_{title}.png' in names
print('PASS: 12 packaged permanent Sabbath rituals, ingredients, timing, names, book links and independent servant resources')
