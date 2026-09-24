"""Recolor Goety's unholy hat and composite the existing faded halo, as requested."""
from pathlib import Path
from io import BytesIO
from zipfile import ZipFile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
textures = ROOT/'src/main/resources/assets/starfantasy_goety/textures/item'
with ZipFile(ROOT.parent/'private-deps/goety/goety-2.5.56.5.jar') as jar:
    hat = Image.open(BytesIO(jar.read('assets/goety/textures/item/unholy_hat.png'))).convert('RGBA')

purple = {
    13: (26, 24, 35), 14: (30, 28, 41), 22: (41, 39, 58),
    29: (46, 44, 64), 35: (54, 52, 72), 44: (63, 60, 84),
    70: (77, 72, 97), 90: (95, 89, 113),
}
for y in range(hat.height):
    for x in range(hat.width):
        r, g, b, a = hat.getpixel((x, y))
        hat.putpixel((x, y), (*purple[r], a) if a else (0, 0, 0, 0))
# Follow the original hat's curved trim, keeping its existing silhouette and pixel shading.
for x, y in [(3, 8), (4, 8), (11, 8), (12, 8)] + [(x, 9) for x in range(5, 11)]:
    gold = (218, 183, 66) if x < 8 else (183, 146, 43)
    hat.putpixel((x, y), (*gold, 255))

halo = Image.open(textures/'faded_halo.png').convert('RGBA')
halo = halo.crop(halo.getbbox())
hat = hat.crop(hat.getbbox()).resize((30, 25), Image.Resampling.NEAREST)
icon = Image.new('RGBA', (64, 64))
icon.alpha_composite(halo, ((64 - halo.width)//2, 10))
icon.alpha_composite(hat, ((64 - hat.width)//2, 27))
icon.save(textures/'faded_crown.png', optimize=True)
print(textures/'faded_crown.png')
