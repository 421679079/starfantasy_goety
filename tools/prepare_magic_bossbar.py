"""Prepare the approved artwork; stage and compress before copying into resources.

The frame keeps the native 182x8 fill, with two pixels of side padding. Cutouts retain
their aspect ratios; four texture pixels per GUI pixel preserve small details.
All generated checkerboard pixels are removed, never included in the atlas.
"""
from pathlib import Path
import argparse
import colorsys
import json
import numpy as np
from PIL import Image, ImageDraw, ImageFont

parser = argparse.ArgumentParser()
parser.add_argument('drafts', type=Path)
parser.add_argument('liquid', type=Path)
parser.add_argument('--output', required=True, type=Path)
parser.add_argument('--preview', required=True, type=Path)
args = parser.parse_args()
args.output.mkdir(parents=True, exist_ok=True)
SCALE = 4
SIDE_PADDING = 2*SCALE
FRAME_WIDTH = 200*SCALE+2*SIDE_PADDING


def clean_frame(path, second_phase):
    rgb = np.array(Image.open(path).convert('RGB'))
    hi, lo = rgb.max(axis=2), rgb.min(axis=2)
    # The backdrop is neutral gray; retain colored ornament and its dark outline.
    keep = ((hi.astype(int) - lo.astype(int)) > 30) | (hi < 85)
    keep[:278] = False
    keep[505:] = False
    rgba = np.dstack((rgb, keep.astype('uint8') * 255))
    rgba[~keep] = 0
    image = Image.fromarray(rgba)
    # Stretch only the straight rails. Scaling the entire picture would flatten
    # the eye/masks and squeeze the two end gems into very narrow slivers.
    # Padding accommodates masks doubled around their unchanged centers.
    fitted = Image.new('RGBA', (FRAME_WIDTH, 24*SCALE))

    def place(box, size, at, canvas=None):
        if canvas is None:
            canvas = fitted
        tile = image.crop(box).resize(size, Image.Resampling.LANCZOS)
        x, y = at[0]+SIDE_PADDING, at[1]+4*SCALE
        if int(x) != x or int(y) != y:
            translated = tile.transform(fitted.size, Image.Transform.AFFINE,
                                        (1, 0, -x, 0, 1, -y), Image.Resampling.BICUBIC)
            canvas.alpha_composite(translated)
        else:
            canvas.alpha_composite(tile, (int(x), int(y)))

    # A tight crop gives about one visible GUI pixel of gold. Grow inward while
    # holding the old rails' outer bounds at 2.5 and 13.5, not a taller gold band.
    top = (352, 366) if second_phase else (354, 365)
    bottom = (443, 457) if second_phase else (447, 458)
    rails = Image.new('RGBA', fitted.size)
    ornaments = Image.new('RGBA', fitted.size)
    place((400, top[0], 690, top[1]), (728, 6), (36, 10), rails)
    place((400, bottom[0], 690, bottom[1]), (728, 6), (36, 48), rails)
    # Move the end ornaments outward by two GUI pixels, with enough transparent
    # atlas padding to preserve their tips. Stop each rail at the inward-facing
    # silhouette instead of drawing a line through the ornament's open curls.
    place((0, 304, 255, 498), (78, 60), (-8, 4), ornaments)
    place((1730, 304, 1983, 498), (78, 60), (730, 4), ornaments)
    rail_pixels = np.array(rails)
    ornament_alpha = np.array(ornaments)[:, :, 3]
    midpoint = FRAME_WIDTH//2
    for row in np.flatnonzero(rail_pixels[:, :, 3].max(axis=1)):
        left_edge = np.flatnonzero(ornament_alpha[row, :midpoint] >= 128)
        right_edge = np.flatnonzero(ornament_alpha[row, midpoint:] >= 128)+midpoint
        assert len(left_edge) and len(right_edge), 'Rail must meet both ornaments'
        rail_pixels[row, :left_edge[-1]] = 0
        rail_pixels[row, right_edge[0]+1:] = 0
    fitted.alpha_composite(Image.fromarray(rail_pixels))
    fitted.alpha_composite(ornaments)
    # Keep its approved doubled size; move to the health channel's center (100,8).
    place((898, 282, 1085, 385), (80, 44), (360, 10))
    if second_phase:
        for source_x, target_x in ((333, 28), (788, 78), (1188, 122), (1648, 172)):
            place((source_x-25, 306, source_x+26, 369), (28, 34), (target_x*SCALE-14, -8.5))
            place((source_x-25, 442, source_x+26, 499), (28, 32), (target_x*SCALE-14, 40))
        place((924, 442, 1056, 500), (36, 16), (382, 48))
    return fitted


frames = [clean_frame(args.drafts / f'phase{phase}_frame.png', phase == 2) for phase in (1, 2)]
frame_atlas = Image.new('RGBA', (FRAME_WIDTH, 48*SCALE))
for phase, frame in enumerate(frames):
    frame_atlas.paste(frame, (0, phase*24*SCALE))
frame_atlas.save(args.output / 'apollyon_boss_bar.png')

# The generated strip is isolated by its exact rectangular bounds, so its white
# sparkles survive (a chroma key would accidentally delete them).
liquid = Image.open(args.liquid).convert('RGB').crop((210, 373, 1773, 437))
liquid = liquid.resize((182*SCALE, 8*SCALE), Image.Resampling.LANCZOS)
data = np.array(liquid).astype('float32') / 255.0
light = data.min(axis=2)
brightness = data.max(axis=2)
# Real alpha: liquid 60-78%, brightest white star cores up to 95%.
alpha = np.clip(.59 + .19*brightness + .22*np.maximum(0, (light-.60)/.40), .60, .95)
rgba = np.dstack((data, alpha))
# Match both ends without blurring stars elsewhere, for seamless UV wrapping.
edge = (rgba[:, 0, :] + rgba[:, -1, :]) * .5
for distance in range(12):
    weight = distance / 12
    for x in (distance, rgba.shape[1]-1-distance):
        rgba[:, x] = edge*(1-weight) + rgba[:, x]*weight
purple = Image.fromarray(np.uint8(np.clip(rgba*255, 0, 255)))
red_data = np.array(purple)
for y in range(red_data.shape[0]):
    for x in range(red_data.shape[1]):
        r, g, b = red_data[y, x, :3] / 255.0
        hue, sat, val = colorsys.rgb_to_hsv(r, g, b)
        # White glints retain their near-white appearance in both phases.
        red_data[y, x, :3] = np.uint8(np.array(colorsys.hsv_to_rgb(.985, sat, val))*255)
red = Image.fromarray(red_data)
fills = [red, purple]
fill_atlas = Image.new('RGBA', (364*SCALE, 16*SCALE))
for phase, fill in enumerate(fills):
    for copy in range(2):
        fill_atlas.paste(fill, (copy*182*SCALE, phase*8*SCALE))
fill_atlas.save(args.output / 'apollyon_boss_bar_fill.png')

# Keep user-facing, separated PNGs outside the mod's two packed atlases.
separated = args.output.parent / 'separated'
separated.mkdir(exist_ok=True)
for phase in range(2):
    frames[phase].save(separated / f'phase{phase+1}_frame.png')
    fills[phase].save(separated / f'phase{phase+1}_fill.png')

# A world-colored, textured backdrop makes alpha (including the empty half)
# visible; this is an asset preview, not an in-game capture.
preview = Image.new('RGBA', (880, 430), '#25202f')
draw = ImageDraw.Draw(preview)
for y in range(0, preview.height, 16):
    for x in range(0, preview.width, 16):
        if (x//16+y//16) % 2:
            draw.rectangle((x, y, x+15, y+15), fill='#302938')
for phase in range(2):
    draw.text((40, 15+phase*210), f'Phase {phase+1}: full / half (4x GUI size)', fill='white')
    for row, width in enumerate((182*SCALE, 91*SCALE)):
        x, y = 40, 47+phase*210+row*78
        preview.alpha_composite(fills[phase].crop((0, 0, width, 8*SCALE)), (x+9*SCALE, y+4*SCALE))
        preview.alpha_composite(frames[phase], (x-SIDE_PADDING, y-4*SCALE))
args.preview.parent.mkdir(parents=True, exist_ok=True)
preview.save(args.preview)
metadata = {
    'tool': 'built-in imagegen, followed by user-authorized Pillow processing',
    'liquid_prompt': 'Translucent violet magical liquid with sparse white star specks; remove solid bevels, add subtle flowing highlights; preserve rectangular meter.',
    'source_liquid': str(args.liquid), 'source_frames': str(args.drafts),
    'gui_frame': [204, 24], 'frame_x_offset': -2, 'frame_y_offset': -4, 'gui_fill': [182, 8], 'texture_scale': SCALE,
    'eye_center': [100, 8], 'eye_size': [20, 11], 'mask_scale': 2,
    'end_ornament_scale': 1.5, 'end_ornament_outward_offset': 2,
    'gem_centers': [[10, 8.5], [190, 8.5]], 'rail_joins': 'inner ornament silhouette',
    'rail_outer_y': [2.5, 13.5], 'rail_thickness_direction': 'inward',
    'rail_tile_height': 1.5,
    'fill_alpha': [int(np.array(purple)[:,:,3].min()), int(np.array(purple)[:,:,3].max())]
}
(args.output.parent / 'artwork.json').write_text(json.dumps(metadata, indent=2), 'utf-8')
print(json.dumps(metadata, indent=2))
