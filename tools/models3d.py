"""Volumetric 3D models for plants and items (replaces the flat cross planes)."""
import math, random
from px import *
import tex_plants as P
from gen_assets import model, box, face, leaf_plane, diag_leaf, stem, cross, wtex, wjson, NS

# ---------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------
OUT = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}

def rot_for(direction, tilt):
    """Rotation (axis, angle) that tilts an element pointing to 'direction' up (+) / down (-) by tilt."""
    if direction == 'north': return 'x', -tilt
    if direction == 'south': return 'x', tilt
    if direction == 'east': return 'z', -tilt
    return 'z', tilt

def branch(tex, direction, h, length, tilt=22.5, thick=0.7, cx=8, cz=8):
    """Side branch as a thin cuboid, angled upwards."""
    dx, dz = OUT[direction]
    t = thick / 2
    if dx:
        frm = [cx if dx > 0 else cx - length, h - t, cz - t]; to = [cx + length if dx > 0 else cx, h + t, cz + t]
    else:
        frm = [cx - t, h - t, cz if dz > 0 else cz - length]; to = [cx + t, h + t, cz + length if dz > 0 else cz]
    axis, ang = rot_for(direction, tilt)
    e = box(frm, to, tex, faces=['north', 'south', 'east', 'west', 'up', 'down'])
    if ang:
        e['rotation'] = {'origin': [cx, h, cz], 'axis': axis, 'angle': ang}
    tip = (cx + dx * length * math.cos(math.radians(abs(tilt))), h + length * math.sin(math.radians(tilt)), cz + dz * length * math.cos(math.radians(abs(tilt))))
    return e, tip

def opposite(d):
    return {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}[d]

def node(tex_leaf, tex_stem, h, axis, size, tilt, blen=1.2, width=None, btilt=22.5):
    """Node: two opposite branches with fan leaves."""
    dirs = ('north', 'south') if axis == 'ns' else ('east', 'west')
    els = []
    for d in dirs:
        if blen > 0:
            e, tip = branch(tex_stem, d, h, blen, btilt)
            els.append(e)
            cx, ty, cz = tip
        else:
            cx, ty, cz = 8, h, 8
        els.append(leaf_plane(tex_leaf, d, round(ty * 4) / 4, size, tilt, cx=round(cx * 4) / 4, cz=round(cz * 4) / 4, width=width))
    return els

def tuft(tex, h, size):
    """Small leaf rosette at the top (two crossed, flat diagonal leaves + upright leaves)."""
    return [diag_leaf(tex, h, size, 45), diag_leaf(tex, h, size, -45),
            leaf_plane(tex, 'north', h, size * 0.8, 22.5), leaf_plane(tex, 'south', h, size * 0.8, 22.5)]

# ---------------------------------------------------------------------
# Cannabis
# ---------------------------------------------------------------------
def gen_cannabis_3d():
    tex = {'particle': f'{NS}:block/cannabis_stage4', 'stem': f'{NS}:block/cannabis_stem', 'fan': f'{NS}:block/cannabis_fan',
           'fan_young': f'{NS}:block/cannabis_fan_young', 'cola': f'{NS}:block/cannabis_cola'}
    lower = {
        0: dict(h=2, nodes=[], tuft=(1.5, 3.5, True)),
        1: dict(h=4.5, nodes=[(2.5, 'ns', 5, 22.5, 0)], tuft=(4.5, 3.5, True)),
        2: dict(h=7, nodes=[(2.5, 'ew', 6.5, 0, 0.8), (5, 'ns', 5.5, 22.5, 0.6)], tuft=(7, 4, True)),
        3: dict(h=11, nodes=[(2.5, 'ew', 8.5, -22.5, 1.2), (5.5, 'ns', 8, 0, 1.0), (8.5, 'ew', 7, 22.5, 0.8)], tuft=(11, 5, False)),
    }
    full = dict(h=16, nodes=[(2.5, 'ew', 10, -22.5, 1.6), (5.5, 'ns', 10, -22.5, 1.5), (8.5, 'ew', 9.5, 0, 1.3), (11.5, 'ns', 9, 0, 1.1), (14.5, 'ew', 8.5, 22.5, 0.9)], tuft=None)
    for s in range(4, 8): lower[s] = full
    upper = {
        4: dict(h=5, nodes=[(1.5, 'ns', 8, 0, 0.8)], tuft=(5, 5, False), colas=[]),
        5: dict(h=9, nodes=[(1.5, 'ns', 8.5, 0, 1.0), (4.5, 'ew', 7.5, 22.5, 0.8)], tuft=(9, 5, False), colas=[]),
        6: dict(h=13, nodes=[(1.5, 'ns', 9, -22.5, 1.2), (4.5, 'ew', 8, 0, 1.0), (7.5, 'ns', 7, 22.5, 0.8)], tuft=None,
                colas=[(4.5, 'ew', 1.0), (7.5, 'ns', 0.8)]),
        7: dict(h=14, nodes=[(1.5, 'ns', 9.5, -22.5, 1.3), (4.5, 'ew', 8.5, -22.5, 1.2), (7.5, 'ns', 7.5, 0, 1.0), (10.5, 'ew', 6, 22.5, 0.8)], tuft=None,
                colas=[(4.5, 'ew', 1.2), (7.5, 'ns', 1.0), (10.5, 'ew', 0.8)]),
    }

    def build(spec, young, top=None):
        els = []
        h = spec['h']
        els.append(stem('stem', h, 1.0))
        leaf = 'fan_young' if young else 'fan'
        for i, (nh, axis, size, tilt, blen) in enumerate(spec['nodes']):
            els += node(leaf, 'stem', nh, axis, size, tilt, blen)
            # second leaf ring: diagonal, smaller, flat - fills the gaps
            els.append(diag_leaf(leaf, nh + 0.75, size * 0.7, 45))
            dl = diag_leaf(leaf, nh + 0.75, size * 0.7, 45)
            dl['from'] = [8 - size * 0.35, nh + 0.75, 8]; dl['to'] = [8 + size * 0.35, nh + 0.75, 8 + size * 0.7]
            dl['faces'] = {'up': face(leaf, [0, 0, 16, 16], 180), 'down': face(leaf, [0, 16, 16, 0], 180)}
            els.append(dl)
            # steep young leaves at the node (45° upwards) - visible from the side
            other = ('east', 'west') if axis == 'ns' else ('north', 'south')
            for d in other:
                els.append(leaf_plane(leaf, d, nh + 1.5, size * 0.55, 45))
        if spec.get('tuft'):
            th, ts, _ = spec['tuft']
            els += tuft(leaf, th, ts)
        if top:
            # main cola in three steps + side colas at the branch tips
            r = 1.35 if top == 7 else 1.1
            els.append(box([8 - r, h - 7, 8 - r], [8 + r, h - 2.5, 8 + r], 'cola'))
            els.append(box([8 - r + 0.35, h - 2.5, 8 - r + 0.35], [8 + r - 0.35, h, 8 + r - 0.35], 'cola'))
            els.append(box([8 - 0.5, h, 8 - 0.5], [8 + 0.5, h + 1, 8 + 0.5], 'cola'))
            # sugar leaves sticking out of the cola
            for j, d in enumerate(('north', 'east', 'south', 'west')):
                els.append(leaf_plane('fan_young', d, h - 5 + j * 1.2, 3, 45))
            for (ch, axis, blen) in spec['colas']:
                for d in (('north', 'south') if axis == 'ns' else ('east', 'west')):
                    dx, dz = OUT[d]
                    cx, cz = 8 + dx * (blen + 0.6), 8 + dz * (blen + 0.6)
                    els.append(box([cx - 0.7, ch + 0.4, cz - 0.7], [cx + 0.7, ch + 2.8, cz + 0.7], 'cola'))
                    els.append(box([cx - 0.4, ch + 2.8, cz - 0.4], [cx + 0.4, ch + 3.5, cz + 0.4], 'cola'))
        return els

    for s in range(8):
        els = build(lower[s], s <= 2)
        model(f'block/cannabis_stage{s}', tex | {'particle': f'{NS}:block/cannabis_stage{s}'}, els, render_type='minecraft:cutout', ao=False)
    for s in range(4, 8):
        els = build(upper[s], False, top=s if s >= 6 else None)
        model(f'block/cannabis_top_stage{s}', tex | {'particle': f'{NS}:block/cannabis_top_stage{s}'}, els, render_type='minecraft:cutout', ao=False)

# ---------------------------------------------------------------------
# Tobacco
# ---------------------------------------------------------------------
def gen_tobacco_3d():
    bloom = Canvas()
    for y in range(16):
        for x in range(16):
            bloom.set(x, y, hexc('#f2a3c8') if (x + y) % 3 else hexc('#d0679a'))
    wtex('block/tobacco_bloom', bloom)
    tube = Canvas()
    for y in range(16):
        for x in range(16):
            tube.set(x, y, hexc('#e8eccf') if x % 4 else hexc('#c8d0a0'))
    wtex('block/tobacco_tube', tube)
    st = Canvas()
    for y in range(16):
        for x in range(16):
            st.set(x, y, hexc('#7aa846') if x % 3 else hexc('#5f8a34'))
    wtex('block/tobacco_stem', st)
    H = [2.5, 4.5, 6.5, 9, 11, 13, 14, 15]
    tex = {'stem': f'{NS}:block/tobacco_stem', 'leaf': f'{NS}:block/tobacco_leaf', 'bloom': f'{NS}:block/tobacco_bloom', 'tube': f'{NS}:block/tobacco_tube'}
    for s_ in range(8):
        h = H[s_]
        els = [stem('stem', h, 1.0 if s_ < 4 else 1.3)]
        n = min(6, 1 + s_)
        for i in range(n):
            nh = 0.75 + i * max(1.6, (h - 2.5) / max(1, n))
            if nh > h - 1: break
            frac = i / max(1, n - 1)
            size = (6 + s_ * 0.9) * (1 - 0.45 * frac)
            size = max(3, min(size, 12))
            tilt = -22.5 if frac < 0.34 else (22.5 if frac < 0.75 else 45)
            axis = ('east', 'west') if i % 2 == 0 else ('north', 'south')
            for d in axis:
                els.append(leaf_plane('leaf', d, round(nh * 4) / 4, size, tilt, width=size * 0.95))
        els.append(leaf_plane('leaf', 'north', h - 0.5, 3.5, 45, width=3.2))
        els.append(leaf_plane('leaf', 'south', h - 0.5, 3.5, 45, width=3.2))
        if s_ >= 6:
            rnd = random.Random(s_)
            pts = [(8, 8), (6.5, 7), (9.5, 7.5), (7.5, 9.5), (9, 9.5)] if s_ == 7 else [(8, 8), (6.8, 7.5), (9.2, 8.8)]
            for (x, z) in pts:
                b = h - 0.5 + rnd.random() * 1.2
                els.append(box([x - 0.35, b, z - 0.35], [x + 0.35, b + 1.6, z + 0.35], 'tube'))
                els.append(box([x - 0.9, b + 1.6, z - 0.9], [x + 0.9, b + 2.1, z + 0.9], 'bloom'))
        model(f'block/tobacco_stage{s_}', tex | {'particle': f'{NS}:block/tobacco_stage{s_}'}, els, render_type='minecraft:cutout', ao=False)

# ---------------------------------------------------------------------
# Opium poppy
# ---------------------------------------------------------------------
def poppy_leaf_tex():
    cv = Canvas()
    L = P.POP_LEAF
    for y in range(16):
        for x in range(16):
            u = (15.5 - y) / 15.0                      # 0 = base, 1 = tip
            if u < 0 or u > 1: continue
            w = 6.8 * math.sin(math.pi * min(1, u * 0.95 + 0.05)) ** 0.8
            w *= 0.82 + 0.18 * math.cos(u * 6 * math.pi)   # lobed edge
            d = abs(x + 0.5 - 8)
            if d <= w:
                c = L['l'] if x >= 8 else L['m']
                if d > w - 1: c = L['d']
                if d < 0.6: c = L['v']
                cv.set(x, y, c)
    return cv

def gen_poppy_3d():
    wtex('block/poppy_leaf', poppy_leaf_tex())
    pet = Canvas()
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 7.5, (y + 0.5 - 9) / 7
            if dx * dx + dy * dy <= 1:
                c = P.RED if dy > -0.5 else P.RED_L
                if dy > 0.55: c = P.BLACK if abs(dx) < 0.45 else P.RED_D
                pet.set(x, y, c)
    wtex('block/poppy_petal_single', pet)
    tex = {'leaf': f'{NS}:block/poppy_leaf', 'stem': f'{NS}:block/poppy_stem', 'petal': f'{NS}:block/poppy_petal',
           'single': f'{NS}:block/poppy_petal_single', 'pod': f'{NS}:block/poppy_pod', 'crown': f'{NS}:block/poppy_crown'}
    stalks = [(8, 8, 0), (5.5, 6.5, -2), (10.5, 9.5, -3), (6, 10.5, -4)]
    Hs = [2, 4, 7, 9, 11, 13, 14, 14]
    for s in range(8):
        els = []
        # basal rosette
        n_leaf = min(4, 1 + s // 2)
        for i, d in enumerate(['east', 'west', 'north', 'south'][:max(2, n_leaf)]):
            size = min(9, 4 + s * 0.9)
            els.append(leaf_plane('leaf', d, 0.5 + i * 0.4, size, -22.5 if s >= 3 else 22.5, width=size * 0.9))
            if s >= 2:
                size2 = min(6.5, 3 + s * 0.6)
                els.append(leaf_plane('leaf', d, 1.2 + i * 0.4, size2, 45, width=size2 * 0.9))
        if s >= 2:
            count = 1 if s < 4 else (3 if s < 6 else 4)
            for (x, z, off) in stalks[:count]:
                h = max(3, Hs[s] + off)
                els.append(stem('stem', h, 0.7, x, z))
                # stem leaf
                els.append(leaf_plane('leaf', 'east' if x > 8 else 'west', round(h * 0.4 * 4) / 4, 4.5, 45, cx=x, cz=z, width=4))
                els.append(leaf_plane('leaf', 'north' if z > 8 else 'south', round(h * 0.6 * 4) / 4, 3.5, 45, cx=x, cz=z, width=3.2))
                if s == 4:   # nodding bud
                    els.append(box([x - 0.8, h - 1.8, z - 0.8], [x + 0.8, h, z + 0.8], 'pod'))
                elif s == 5:   # open flower: calyx + 4 petals
                    els.append(box([x - 1, h, z - 1], [x + 1, h + 1.2, z + 1], {'north': 'single', 'south': 'single', 'east': 'single', 'west': 'single', 'up': 'petal', 'down': 'pod'}))
                    for d in ('north', 'south', 'east', 'west'):
                        dx, dz = OUT[d]
                        els.append(leaf_plane('single', d, h + 1, 3.2, 22.5, cx=x + dx * 0.8, cz=z + dz * 0.8, width=3.2))
                elif s >= 6:
                    r = 1.2 if s == 6 else 1.6
                    els.append(box([x - r, h, z - r], [x + r, h + 2 * r + 0.4, z + r], {'north': 'pod', 'south': 'pod', 'east': 'pod', 'west': 'pod', 'up': 'crown', 'down': 'pod'}))
                    els.append(box([x - r - 0.3, h + 2 * r + 0.4, z - r - 0.3], [x + r + 0.3, h + 2 * r + 0.8, z + r + 0.3], 'crown'))
        model(f'block/poppy_stage{s}', tex | {'particle': f'{NS}:block/poppy_stage{s}'}, els, render_type='minecraft:cutout', ao=False)

# ---------------------------------------------------------------------
# Coca bush as a voxel shrub
# ---------------------------------------------------------------------
def gen_coca_3d():
    lv = Canvas()
    rnd = random.Random(21)
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.82:
                lv.set(x, y, rnd.choice([P.COCA['d'], P.COCA['m'], P.COCA['m'], P.COCA['l'], P.COCA['l'], P.COCA['h']]))
    lv.edge_darken(0.85, hl=1.05)
    wtex('block/coca_foliage', lv)
    tex = {'leaves': f'{NS}:block/coca_foliage', 'wood': 'minecraft:block/stripped_jungle_log', 'berry': f'{NS}:block/coca_berry',
           'leaf': f'{NS}:block/coca_stage1'}
    clusters = {
        0: [],
        1: [(8, 4.5, 8, 2.5)],
        2: [(8, 6.5, 8, 3), (5.5, 4.5, 6.5, 2.2), (10.5, 5, 9.5, 2.2)],
        3: [(8, 9.5, 8, 3.5), (5, 6, 6, 3), (11, 6.5, 10, 3), (6, 7, 11, 2.6), (10.5, 8, 5, 2.6), (8, 12.5, 8, 2.2)],
        4: [(8, 10, 8, 4), (4.8, 6.5, 6, 3.2), (11.2, 7, 10, 3.2), (6, 7.5, 11.5, 2.8), (10.5, 8.5, 4.5, 2.8), (8, 13.5, 8, 2.5), (11, 11, 8.5, 2.2)],
    }
    berries = [(4, 9, 5), (11.5, 10.5, 6), (6.5, 12.5, 11), (12, 6.5, 12), (9.5, 14.5, 9.5), (3.5, 6, 9), (8.5, 7, 13.5), (13, 9, 7.5)]
    for s in range(5):
        els = []
        h = [3, 4, 6, 9, 10][s]
        els.append(box([7.4, 0, 7.4], [8.6, h, 8.6], 'wood', faces=['north', 'south', 'east', 'west', 'up']))
        if s >= 2:
            for d in ('north', 'east', 'south', 'west'):
                e, _ = branch('wood', d, 2 + (1 if d in ('east', 'west') else 0), 3.5 if s >= 3 else 2.5, 45 if s >= 3 else 22.5, thick=0.8)
                els.append(e)
        if s == 0:
            els.append(leaf_plane('leaf', 'east', 2.5, 3, 22.5)); els.append(leaf_plane('leaf', 'west', 2.5, 3, 22.5))
            els.append(leaf_plane('leaf', 'north', 3, 2.5, 22.5))
        for (x, y, z, r) in clusters[s]:
            els.append(box([x - r, y - r * 0.8, z - r], [x + r, y + r * 0.8, z + r], 'leaves'))
        if s == 4:
            for (x, y, z) in berries:
                els.append(box([x - 0.5, y - 0.5, z - 0.5], [x + 0.5, y + 0.5, z + 0.5], 'berry'))
        model(f'block/coca_stage{s}', tex | {'particle': f'{NS}:block/coca_stage{s}'}, els, render_type='minecraft:cutout', ao=True)

# ---------------------------------------------------------------------
# 3D items (hand/ground/frame) - the inventory keeps the 2D icon
# ---------------------------------------------------------------------
HANDHELD = {
    'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 13, 7], 'scale': [1, 1, 1]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [1.3, 1.3, 1.3]},
}
OBJECT = {
    'thirdperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2.5, 1.5], 'scale': [0.55, 0.55, 0.55]},
    'thirdperson_lefthand': {'rotation': [0, 45, 0], 'translation': [0, 2.5, 1.5], 'scale': [0.55, 0.55, 0.55]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [1, 2, 0], 'scale': [0.55, 0.55, 0.55]},
    'firstperson_lefthand': {'rotation': [0, 45, 0], 'translation': [1, 2, 0], 'scale': [0.55, 0.55, 0.55]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 10, 0], 'scale': [0.8, 0.8, 0.8]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -2, 0], 'scale': [1.25, 1.25, 1.25]},
}
FLAT = {k: dict(v) for k, v in OBJECT.items()}
FLAT['fixed'] = {'rotation': [90, 0, 0], 'translation': [0, 0, -0.5], 'scale': [1.1, 1.1, 1.1]}
FLAT['ground'] = {'rotation': [0, 0, 0], 'translation': [0, 1, 0], 'scale': [0.6, 0.6, 0.6]}

def diag_rot(e):
    e['rotation'] = {'origin': [8, 8, 8], 'axis': 'z', 'angle': 45}
    return e

def item3d(name, textures, elements, display, render_type='minecraft:cutout'):
    """Generates <name>_3d (geometry) and <name> (separate_transforms: GUI = 2D icon)."""
    model(f'item/3d/{name}', textures | {'particle': f'{NS}:item/{name}'}, elements, render_type=render_type, display=display)
    wjson(f'models/item/{name}.json', {
        'loader': 'neoforge:separate_transforms',
        'gui_light': 'front',
        'base': {'parent': f'{NS}:item/3d/{name}'},
        'perspectives': {'gui': {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{name}'}}},
    })

def stick_item(name, segments, textures, radius=0.75):
    """Round smokable along the diagonal. segments: [(length, tex, radius_factor)] from bottom to top."""
    x = 1.5
    els = []
    for length, tex, rf in segments:
        r = radius * rf
        e = box([x, 8 - r, 8 - r], [x + length, 8 + r, 8 + r], tex)
        els.append(diag_rot(e))
        x += length
    item3d(name, textures, els, HANDHELD)

def tex3d():
    rnd = random.Random(31)
    def fill(name, cols, weights=None, stripes=None):
        cv = Canvas()
        for y in range(16):
            for x in range(16):
                cv.set(x, y, rnd.choices(cols, weights=weights)[0])
        if stripes:
            for x in range(0, 16, stripes[0]):
                for y in range(16):
                    cv.set(x, y, stripes[1])
        wtex(f'item/3d/{name}', cv)
    fill('paper', [hexc('#f6f3ec'), hexc('#efeae0'), hexc('#fbfaf6')], stripes=(8, hexc('#e2ddd2')))
    fill('paper_tobacco', [hexc('#f6f3ec'), hexc('#efeae0'), hexc('#d8c4a0')], weights=[5, 4, 1])
    fill('filter', [hexc('#d98a3a'), hexc('#e8a55a'), hexc('#c47a30'), hexc('#f2c48a')])
    fill('card', [hexc('#e2dccb'), hexc('#d6cfbb')])
    fill('ember', [hexc('#ff7a1a'), hexc('#ffb040'), hexc('#c43c0c'), hexc('#8a8580')], weights=[4, 2, 2, 2])
    fill('ash', [hexc('#8a8580'), hexc('#a5a09a'), hexc('#6e6a65')])
    fill('tip', [hexc('#f6f3ec'), hexc('#e2ddd2'), hexc('#5f9a3a')], weights=[5, 3, 1])
    fill('cigar', [hexc('#8a5a32'), hexc('#6b4224'), hexc('#7a4e2a'), hexc('#4e2f18')], weights=[4, 3, 3, 1])
    fill('band', [hexc('#e8c040')], stripes=(3, hexc('#b0202a')))
    fill('plastic_red', [hexc('#d03a2a'), hexc('#c0302a'), hexc('#e04a3a')])
    fill('steel', [hexc('#c0c6cc'), hexc('#a8aeb6'), hexc('#d8dde2')])
    fill('dark', [hexc('#3a3e46'), hexc('#2e3238'), hexc('#4a4e56')])
    fill('wood', [hexc('#8a5a32'), hexc('#7a4a24'), hexc('#a8703c')], stripes=(5, hexc('#5a3820')))
    fill('hash', [hexc('#6e4a26'), hexc('#7e5630'), hexc('#5a3a1c'), hexc('#8a6234')])
    fill('ceramic', [hexc('#f0f0ea'), hexc('#e6e6de'), hexc('#fafaf6')])
    fill('tea_green', [hexc('#9ab04a'), hexc('#8aa040')])
    fill('tea_yellow', [hexc('#d8c070'), hexc('#c8b060')])
    fill('liquid_amber', [hexc('#f0d878', 220), hexc('#e8c860', 220)])
    fill('canister', [hexc('#3f6fb0'), hexc('#4a7ac0'), hexc('#36609c')])
    wtex('block/cannabis_cola_dried', dried_cola())

def dried_cola():
    cv = Canvas()
    rnd = random.Random(6)
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choice([hexc('#6b6e2e'), hexc('#868a3c'), hexc('#a2a24e'), hexc('#5a5c26')]))
    for _ in range(30):
        cv.set(rnd.randrange(16), rnd.randrange(16), hexc('#a8582a') if rnd.random() < 0.7 else hexc('#e8e8d0'))
    return cv

def gen_items_3d():
    tex3d()
    T = lambda n: f'{NS}:item/3d/{n}'
    # Smokables
    stick_item('joint', [(2.2, 'card', 0.8), (6.5, 'paper', 1.0), (2.5, 'tip', 1.25), (0.6, 'ember', 1.15)],
               {'card': T('card'), 'paper': T('paper'), 'tip': T('tip'), 'ember': T('ember')})
    stick_item('spliff', [(2.2, 'card', 0.8), (6.5, 'paper', 1.0), (2.5, 'tip', 1.25), (0.6, 'ember', 1.15)],
               {'card': T('card'), 'paper': T('paper_tobacco'), 'tip': T('tip'), 'ember': T('ember')})
    stick_item('cigarette', [(3.2, 'filter', 0.8), (7.5, 'paper', 0.8), (0.6, 'ash', 0.8), (0.4, 'ember', 0.8)],
               {'filter': T('filter'), 'paper': T('paper'), 'ash': T('ash'), 'ember': T('ember')}, radius=0.7)
    stick_item('cigar', [(1.5, 'cigar', 0.85), (1.2, 'band', 1.05), (8.0, 'cigar', 1.0), (0.7, 'ash', 1.0), (0.4, 'ember', 1.0)],
               {'cigar': T('cigar'), 'band': T('band'), 'ash': T('ash'), 'ember': T('ember')}, radius=1.2)
    # Syringes
    for name, liq in (('empty_syringe', None), ('morphine_syringe', T('liquid_amber'))):
        els = [
            diag_rot(box([1, 6.75, 6.75], [1.6, 9.25, 9.25], 'dark')),               # thumb rest
            diag_rot(box([1.6, 7.6, 7.6], [4.2, 8.4, 8.4], 'steel')),                # plunger rod
            diag_rot(box([4.2, 6.5, 7.6], [4.8, 9.5, 8.4], 'dark')),                 # finger flange
            diag_rot(box([4.8, 7, 7], [11, 9, 9], 'glass')),                         # barrel
            diag_rot(box([11, 7.5, 7.5], [11.8, 8.5, 8.5], 'steel')),                # hub
            diag_rot(box([11.8, 7.85, 7.85], [15.2, 8.15, 8.15], 'steel')),          # needle
        ]
        texs = {'dark': T('dark'), 'steel': T('steel'), 'glass': f'{NS}:block/lab_glass'}
        if liq:
            els.insert(3, diag_rot(box([5.2, 7.25, 7.25], [10.6, 8.75, 8.75], 'liq')))
            texs['liq'] = liq
        item3d(name, texs, els, HANDHELD, render_type='minecraft:translucent')
    # Pipe
    els = [
        box([2, 6.5, 7.4], [10, 7.7, 8.6], 'dark'),                 # mouthpiece
        box([9, 6.2, 7.1], [11.5, 8, 8.9], 'wood'),
        box([10.5, 6, 6.5], [14, 11.5, 9.5], 'wood'),               # bowl
        box([11, 10.6, 7], [13.5, 11.6, 9], 'ash'),                 # embers/contents
    ]
    item3d('smoking_pipe', {'dark': T('dark'), 'wood': T('wood'), 'ash': T('ember')}, els, HANDHELD)
    # Lighter
    els = [
        box([6, 2, 7], [10, 10, 9.5], 'red'),
        box([6, 10, 7], [10, 11, 9.5], 'steel'),
        box([6.5, 11, 7.5], [9.5, 13, 9], 'steel'),
        box([8.5, 12, 7.8], [9.5, 13.5, 8.7], 'dark'),
    ]
    item3d('lighter', {'red': T('plastic_red'), 'steel': T('steel'), 'dark': T('dark')}, els, OBJECT)
    # Buds
    for name, cola in (('cannabis_bud', f'{NS}:block/cannabis_cola'), ('dried_cannabis_bud', f'{NS}:block/cannabis_cola_dried')):
        els = [
            box([5.5, 2, 5.5], [10.5, 8, 10.5], 'c'), box([6.2, 8, 6.2], [9.8, 11.5, 9.8], 'c'), box([7, 11.5, 7], [9, 13, 9], 'c'),
            box([4.5, 4, 7], [5.5, 7, 9], 'c'), box([10.5, 5, 6.5], [11.5, 8, 8.5], 'c'),
            box([7.5, 0, 7.5], [8.5, 2, 8.5], 'stem'),
            leaf_plane('leaf', 'east', 6, 4, 22.5, cx=10.5, cz=8), leaf_plane('leaf', 'west', 8, 3.5, 22.5, cx=5.5, cz=8),
        ]
        item3d(name, {'c': cola, 'stem': f'{NS}:block/cannabis_stem', 'leaf': f'{NS}:block/cannabis_fan'}, els, OBJECT)
    # Magic mushrooms
    for name, dried in (('magic_mushroom', False), ('dried_magic_mushroom', True)):
        k = 0.8 if dried else 1.0
        els = [
            box([7, 0, 7], [9, 7 * k, 9], 'stem', faces=['north', 'south', 'east', 'west', 'down']),
            box([4, 7 * k, 4], [12, 9.5 * k, 12], {'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'cap', 'down': 'gills'}),
            box([5, 9.5 * k, 5], [11, 11 * k, 11], 'cap'), box([6.5, 11 * k, 6.5], [9.5, 11.6 * k, 9.5], 'cap'),
            box([10, 0, 4], [11, 3, 5], 'stem'), box([9, 3, 3], [12, 4, 6], {'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'cap', 'down': 'gills'}),
        ]
        item3d(name, {'stem': f'{NS}:block/magic_mushroom_stem', 'side': f'{NS}:block/magic_mushroom_cap_side', 'cap': f'{NS}:block/magic_mushroom_cap',
                      'gills': f'{NS}:block/magic_mushroom_gills'}, els, OBJECT)
    # Poppy pod
    els = [box([7.5, 0, 7.5], [8.5, 5, 8.5], 'stem'), box([5, 5, 5], [11, 11, 11], {'north': 'pod', 'south': 'pod', 'east': 'pod', 'west': 'pod', 'up': 'crown', 'down': 'pod'}),
           box([4.5, 11, 4.5], [11.5, 12, 11.5], 'crown'), box([7, 12, 7], [9, 12.6, 9], 'crown')]
    item3d('poppy_pod', {'stem': f'{NS}:block/poppy_stem', 'pod': f'{NS}:block/poppy_pod', 'crown': f'{NS}:block/poppy_crown'}, els, OBJECT)
    # Hashish slab
    item3d('hashish', {'h': T('hash')}, [box([3, 0, 4.5], [13, 2.5, 11.5], 'h'), box([4, 2.5, 5.5], [12, 3, 10.5], 'h')], OBJECT)
    # LSD blotter (thin sheet)
    item3d('lsd_blotter', {'top': T('blotter_top'), 'edge': T('paper')},
           [box([2, 0, 2], [14, 0.4, 14], {'up': 'top', 'down': 'edge', 'north': 'edge', 'south': 'edge', 'east': 'edge', 'west': 'edge'},
                uvs={'up': [0, 0, 16, 16], 'down': [2, 2, 14, 14]})], FLAT)
    # Canister
    item3d('solvent', {'can': T('canister'), 'dark': T('dark'), 'label': T('solvent_label')},
           [box([4, 0, 6], [12, 10, 10], {'north': 'label', 'south': 'can', 'east': 'can', 'west': 'can', 'up': 'can', 'down': 'can'},
                uvs={'north': [1, 0, 15, 16]}),
            box([9, 10, 7.25], [11, 11.5, 8.75], 'dark'), box([4.5, 10, 7.5], [7.5, 11, 8.5], 'can')], OBJECT)
    # Cups
    for name, tea in (('coca_tea', T('tea_green')), ('detox_tea', T('tea_yellow'))):
        els = [box([5, 0, 5], [11, 6, 11], 'cer', faces=['north', 'south', 'east', 'west', 'down']),
               box([5.5, 5.2, 5.5], [10.5, 5.5, 10.5], 'tea', faces=['up']),
               box([11, 1.5, 7.5], [12.5, 2.3, 8.5], 'cer'), box([12, 1.5, 7.5], [12.8, 5, 8.5], 'cer'), box([11, 4.2, 7.5], [12.5, 5, 8.5], 'cer')]
        item3d(name, {'cer': T('ceramic'), 'tea': tea}, els, OBJECT)

def run():
    gen_cannabis_3d(); gen_tobacco_3d(); gen_poppy_3d(); gen_coca_3d(); gen_items_3d()
