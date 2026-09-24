"""Exercise the production tooltip splitter and verify the six-page book layout."""
from pathlib import Path
import json
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
resources = ROOT / 'src/main/resources'
source = (ROOT / 'src/mojang/java/com/starfantasy/goety/client/FocusTooltip.java').read_text('utf-8')
source = re.sub(r'^(package|import) .*;\s*$', '', source, flags=re.M)
fixture = r'''
import java.util.*;
class Item { String getDescriptionId(){return "focus";} }
class BattleFocusItem extends Item {}
class GuardFocusItem extends Item {}
enum ChatFormatting { GRAY }
class Component {
    static String translation; String text; ChatFormatting color;
    Component(String text){this.text=text;}
    static Component translatable(String key){return new Component(translation);}
    static Component literal(String text){return new Component(text);}
    String getString(){return text;}
    Component withStyle(ChatFormatting color){this.color=color;return this;}
}
class TooltipRegression {
    public static void main(String[] args) {
        for(Item item:List.of(new BattleFocusItem(),new GuardFocusItem())) {
            for(String separator:List.of("\n","\r\n")) {
                Component.translation="描述。"+separator+"附魔强效可以增加伤害。";
                List<Component> lines=new ArrayList<>();
                lines.add(Component.literal("previous"));
                if(!FocusTooltip.append(item,lines) || lines.size()!=3
                        || !lines.get(1).text.equals("描述。")
                        || !lines.get(2).text.equals("附魔强效可以增加伤害。")
                        || lines.get(1).color!=ChatFormatting.GRAY
                        || lines.get(2).color!=ChatFormatting.GRAY) throw new AssertionError();
            }
        }
        List<Component> other=new ArrayList<>();
        if(FocusTooltip.append(new Item(),other) || !other.isEmpty()) throw new AssertionError();
        System.out.println("PASS: production splitter appends separate gray lines; CRLF/localized text; other items untouched");
    }
}
'''
with tempfile.TemporaryDirectory() as directory:
    d = Path(directory)
    (d / 'FocusTooltip.java').write_text('import java.util.List;\n' + source, 'utf-8')
    (d / 'TooltipRegression.java').write_text(fixture, 'utf-8')
    subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', str(d / 'FocusTooltip.java'), str(d / 'TooltipRegression.java')], check=True)
    subprocess.run(['java', '-cp', directory, 'TooltipRegression'], check=True)

mixins = json.loads((resources / 'starfantasy_goety.mixins.json').read_text('utf-8'))
for name in ['FocusTooltipMixin', 'WandFocusTooltipMixin']:
    assert name in mixins['client'] and name not in mixins['mixins']
    hook = (ROOT / 'src/mojang/java/com/starfantasy/goety/mixin' / (name + '.java')).read_text('utf-8')
    assert 'FocusTooltip.append(' in hook and 'callback.cancel()' in hook

for locale in ['zh_cn', 'en_us']:
    book = resources / f'assets/goety/patchouli_books/black_book/{locale}'
    entries = {p.stem: json.loads(p.read_text('utf-8')) for p in (book / 'entries/starfantasy').glob('*.json')}
    lang = json.loads((resources / f'assets/starfantasy_goety/lang/{locale}.json').read_text('utf-8'))
    for category, count in [('foci', 4), ('servants', 3), ('staves', 11), ('exploration', 2), ('directed_apostles', 12)]:
        data = json.loads((book / f'categories/starfantasy/{category}.json').read_text('utf-8'))
        assert data['parent'] == 'goety:starfantasy'
        assert sum(e['category'] == f'goety:starfantasy/{category}' for e in entries.values()) == count
    # GuiBookCategory.addSubcategoryButtons uses the right page when getEntries().isEmpty().
    assert not any(e['category'] == 'goety:starfantasy' for e in entries.values())
    assert 'directed_apostles' not in entries
    assert '§' not in entries['nameless_staff']['name']
    assert entries['final_staff']['name'] == re.sub(r'§[0-9a-fk-or]', '', lang['item.starfantasy_goety.final_staff'], flags=re.I)
    for name, entry in entries.items():
        if entry['category'] == 'goety:starfantasy/directed_apostles':
            title = name.removeprefix('directed_apostle_')
            assert entry['name'] == lang['entity.starfantasy_goety.apostle.title.' + title]
            assert entry['icon'] == 'starfantasy_goety:halo_of_the_' + title
            assert len(entry['pages']) == 2
            assert entry['pages'][1] == {'type': 'goety:ritual', 'recipe': 'starfantasy_goety:' + name}
            if locale == 'zh_cn':
                assert entry['pages'][0]['text'] == '在召唤仪式中额外添加一个特殊材料，即可召唤出指定头衔的使徒。'
    if locale == 'zh_cn':
        for category, noun in [('foci', '聚晶'), ('servants', '仆从'), ('exploration', '结构'), ('staves', '魔杖')]:
            data = json.loads((book / f'categories/starfantasy/{category}.json').read_text('utf-8'))
            assert data['description'] == f'这里记录了星翼幻想新增的{noun}。'
    for index, name in enumerate(['guard_focus', 'blooms_and_plumes_focus', 'evernight_focus', 'final_art_focus']):
        entry = entries[name]
        assert entry['sortnum'] == index
        assert entry['name'] == lang[f'item.starfantasy_goety.{name}']
        assert entry['category'] == 'goety:starfantasy/foci' and len(entry['pages']) == 2
        assert entry['pages'][0]['type'] == 'patchouli:text'
        assert entry['pages'][1] == {'type': 'goety:ritual', 'recipe': f'starfantasy_goety:{name}'}
        assert entry['extra_recipe_mappings'][f'starfantasy_goety:{name}'] == 1
        if name != 'guard_focus':
            assert entry['pages'][0]['text'] == lang[f'item.starfantasy_goety.{name}.info'].replace('\n', '$(br2)')
    for name in ['flower_arrow', 'flower_burst_ribbon', 'evernight_cage']:
        assert lang[f'entity.starfantasy_goety.{name}']
    if locale == 'zh_cn':
        assert lang['item.starfantasy_goety.final_art_focus.info'].splitlines() == [
            '在前方张开虚质空间，持续吸引周围的敌方生物并造成伤害。', '可附魔强效、持久、范围、半径。']
        assert lang['item.starfantasy_goety.blooms_and_plumes_focus'] == '花与箭的聚晶'
        assert lang['item.starfantasy_goety.blooms_and_plumes_focus.info'].splitlines() == [
            '召唤流星雨对前方区域发动毁灭性打击，造成音爆伤害。', '附魔强效可以增加伤害。']
        assert [lang[f'entity.starfantasy_goety.{n}'] for n in ['flower_arrow', 'flower_burst_ribbon', 'evernight_cage']] == ['流星', '星之环', '长夜']
    staves = [e for e in entries.values() if e['category'] == 'goety:starfantasy/staves']
    for entry in staves:
        assert '§' not in entry['name']
        assert len(entry['pages']) == 2 and entry['pages'][1]['type'] == 'goety:ritual'
        if locale == 'zh_cn' and entry['icon'] != 'starfantasy_goety:final_staff':
            assert entry['pages'][0]['text'] == '使徒的神环可用于强化对应学派的魔杖。'
    assert 'tier_two_staves' not in entries
    assert len(entries['church']['pages']) == 4 and 'extra_recipe_mappings' not in entries['church']
    assert len(entries['underworld_eye']['pages']) == 2
    assert entries['underworld_eye']['extra_recipe_mappings']['starfantasy_goety:underworld_eye'] == 1
    for entry in entries.values():
        for page in entry.get('extra_recipe_mappings', {}).values():
            assert 0 <= page < len(entry['pages'])
print('PASS: client tooltip hooks; five right-page categories with 4/3/11/2/12 entries; titles, descriptions, icons, recipe references and entity translations')
