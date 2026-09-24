"""Extract the four marked sprites without redrawing the supplied pixel art."""
from pathlib import Path
import argparse
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
parser.add_argument('--preview', type=Path)
args = parser.parse_args()
source = Image.open(args.source).convert('RGBA')
if source.size != (256, 256):
    raise SystemExit('Expected the supplied 256x256 boss_bar.png atlas.')

output = ROOT / 'src/main/resources/assets/starfantasy_goety/textures/gui'
output.mkdir(parents=True, exist_ok=True)
# The two 200x16 frames already match Goety's on-screen geometry exactly.
frames = source.crop((0, 0, 200, 32))
frames.save(output / 'apollyon_boss_bar.png', optimize=True)
# Keep both complete, marked 208x12 fill sprites, fitted to the native 182x8
# interior with nearest-neighbor sampling. Repeat each strip for native UV scrolling.
fills = Image.new('RGBA', (364, 16))
for phase, top in enumerate((99, 119)):
    strip = source.crop((12, top, 220, top + 12)).resize((182, 8), Image.Resampling.NEAREST)
    fills.paste(strip, (0, phase * 8))
    fills.paste(strip, (182, phase * 8))
fills.save(output / 'apollyon_boss_bar_fill.png', optimize=True)

if args.preview:
    preview = Image.new('RGBA', (220, 140), '#202020')
    draw = ImageDraw.Draw(preview)
    for phase in range(2):
        draw.text((10, phase * 70 + 2), f'Phase {phase + 1}: full / half', fill='white')
        for index, width in enumerate((182, 91)):
            x, y = 10, phase * 70 + 19 + index * 22
            preview.alpha_composite(fills.crop((0, phase * 8, width, phase * 8 + 8)), (x + 9, y + 4))
            preview.alpha_composite(frames.crop((0, phase * 16, 200, phase * 16 + 16)), (x, y))
    args.preview.parent.mkdir(parents=True, exist_ok=True)
    preview.resize((880, 560), Image.Resampling.NEAREST).save(args.preview)
print('Saved custom frames 200x32 and scrolling fills 364x16; source image unchanged.')
