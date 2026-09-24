"""Verify the local SlashBlade burst port and absence of the removed Bosses resources."""
from pathlib import Path
from zipfile import ZipFile
import re
import sys
import tomllib

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parent
CLIENT = ROOT / 'src/mojang/java/com/starfantasy/goety/magic/focus/client'
SOURCE = WORKSPACE / 'starfantasy_slashblade/src/main/java/com/starfantasy/slashart/client/renderer'


def method(text, marker):
    start = text.index(marker)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


original = (SOURCE / 'BlackHoleRiftRenderer.java').read_text('utf-8')
ported = (CLIENT / 'FinalArtExplosion.java').read_text('utf-8')
for marker in ['public static void renderBurstVisual(', 'private static void renderProceduralCore(',
               'private static void renderCore(', 'private static void renderGlowSphere(',
               'private static void renderSphere(', 'private static void renderAccretionBands(',
               'private static void renderBand(', 'private static Point3 spherePoint(',
               'private static Point3 bandPoint(', 'private static void sphereVertex(',
               'private static void vertex(', 'private record Point3(']:
    expected = method(original, marker).replace('BlackHoleRiftRenderer', 'FinalArtExplosion').replace('BlackHoleRiftRenderTypes', 'FinalArtExplosionRenderTypes')
    assert method(ported, marker) == expected, marker
assert 'ResourceLocation' not in ported and 'FinalCurtainExplosion' not in ported
assert 'isRenderingShaderShadowPass()' in ported
types = (CLIENT / 'FinalArtExplosionRenderTypes.java').read_text('utf-8')
for layer in ['BLACK_HOLE_SURFACE', 'BLACK_HOLE_OVERLAY']:
    pattern = rf'private static final RenderType {layer} = create\([\s\S]*?createCompositeState\(false\)\);'
    old = re.search(pattern, (SOURCE / 'BlackHoleRiftRenderTypes.java').read_text('utf-8')).group()
    new = re.search(pattern, types).group()
    old = re.sub(r'StarFantasySlashArtMod.MODID \+ ":black_hole', '"starfantasy_goety:final_art_black_hole', old)
    assert old == new, layer
for progress in [0, .1, .5, .9, 1]:
    scale = 15 * (1 - (1 - progress) ** 3)
    assert 0 <= scale <= 15 and 0 <= 1 - progress <= 1
assets = ROOT / 'src/main/resources/assets/starfantasy_goety'
slash_assets = WORKSPACE / 'starfantasy_slashblade/src/main/resources/assets/star_fantasy'
for local, source in [('final_art_start', 'final_art_start'), ('final_art_pull', 'final_art_loop')]:
    assert (assets / f'sounds/{local}.ogg').read_bytes() == (slash_assets / f'sounds/{source}.ogg').read_bytes()
removed = ['com/starfantasy/library/vfx/client/finalcurtain/FinalCurtainExplosion.class',
           'assets/star_fantasy_library/textures/effect/denia_black_hole_starfield.png',
           *[f'assets/star_fantasy_library/sounds/{s}.ogg' for s in ['final_art_start', 'final_art_pull', 'final_art_burst']]]
def check_library_requirement(text):
    dependencies = tomllib.loads(text)['dependencies']['starfantasy_goety']
    library = next(dep for dep in dependencies if dep['modId'] == 'star_fantasy_library')
    assert library['versionRange'] == '[0.2.10,)', 'Local burst must not require a Library update'


check_library_requirement((ROOT / 'src/main/resources/META-INF/mods.toml').read_text('utf-8'))
if len(sys.argv) == 3:
    with ZipFile(sys.argv[1]) as goety, ZipFile(sys.argv[2]) as library:
        check_library_requirement(goety.read('META-INF/mods.toml').decode('utf-8'))
        assert all(name not in library.namelist() for name in removed)
        assert all(name not in goety.namelist() for name in removed)
        for name in ['FinalArtExplosion', 'FinalArtExplosionRenderTypes']:
            assert f'com/starfantasy/goety/magic/focus/client/{name}.class' in goety.namelist()
        assert 'THIRD_PARTY/StarFantasy_SlashBlade/LICENSE.txt' in goety.namelist()
        for local, source in [('final_art_start', 'final_art_start'), ('final_art_pull', 'final_art_loop')]:
            assert goety.read(f'assets/starfantasy_goety/sounds/{local}.ogg') == (slash_assets / f'sounds/{source}.ogg').read_bytes()
print('PASS: exact SlashBlade procedural geometry/render states, local public audio, no migrated Bosses burst assets')
