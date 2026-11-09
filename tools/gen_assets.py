"""Generates all client assets: textures, block/item models, blockstates, particles, shader JSONs."""
import json, os, shutil
from px import *
import tex_plants as P
import tex_items_hd as I
import tex_blocks as B
import tex_lab as LT

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'assets', 'highcraft')
NS = 'highcraft'

def wjson(rel, obj):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)

def wtex(rel, cv, mcmeta=None):
    path = os.path.join(ROOT, 'textures', rel + '.png')
    cv.save(path)
    if mcmeta:
        with open(path + '.mcmeta', 'w') as f:
            json.dump(mcmeta, f, indent=2)

# =====================================================================
# Model helpers
# =====================================================================
def face(tex, uv=None, rot=0, cull=None, tint=None):
    f = {'texture': '#' + tex}
    if uv: f['uv'] = uv
    if rot: f['rotation'] = rot
    if cull: f['cullface'] = cull
    return f

def box(frm, to, tex, faces=None, rot=None, shade=True, uvs=None):
    """Cuboid; tex = str (all faces) or dict dir->tex."""
    dirs = faces or ['north', 'south', 'east', 'west', 'up', 'down']
    f = {}
    for d in dirs:
        t = tex.get(d) if isinstance(tex, dict) else tex
        if t is None:
            continue
        f[d] = face(t, uv=(uvs or {}).get(d))
    e = {'from': frm, 'to': to, 'faces': f}
    if rot: e['rotation'] = rot
    if not shade: e['shade'] = False
    return e

def default_uv(e, d):
    """Minecraft's automatic face UV (BlockElement#uvsByFace) for element e and face d."""
    (x1, y1, z1), (x2, y2, z2) = e['from'], e['to']
    return {'down': [x1, 16 - z2, x2, 16 - z1], 'up': [x1, z1, x2, z2], 'north': [16 - x2, 16 - y2, 16 - x1, 16 - y1],
            'south': [x1, 16 - y2, x2, 16 - y1], 'west': [z1, 16 - y2, z2, 16 - y1], 'east': [16 - z2, 16 - y2, 16 - z1, 16 - y1]}[d]

def shift_y(e, dy):
    """Copy of element e moved up by dy pixels; UVs are pinned first so the texture mapping doesn't change."""
    faces = {d: {**f, 'uv': f.get('uv', default_uv(e, d))} for d, f in e['faces'].items()}
    out = {**e, 'from': [e['from'][0], e['from'][1] + dy, e['from'][2]], 'to': [e['to'][0], e['to'][1] + dy, e['to'][2]], 'faces': faces}
    if 'rotation' in e:
        o = e['rotation']['origin']
        out['rotation'] = {**e['rotation'], 'origin': [o[0], o[1] + dy, o[2]]}
    return out

def cross(tex, h=16, size=16, shade=False):
    """Two diagonal planes like vanilla 'cross'."""
    m = (16 - size) / 2
    lo, hi = 0.8 + m * 0.9, 15.2 - m * 0.9
    r = {'origin': [8, 8, 8], 'axis': 'y', 'angle': 45, 'rescale': True}
    uv = [0, 16 - h, 16, 16]
    e1 = {'from': [lo, 0, 8], 'to': [hi, h, 8], 'rotation': r, 'shade': shade,
          'faces': {'north': face(tex, uv), 'south': face(tex, uv)}}
    e2 = {'from': [8, 0, lo], 'to': [8, h, hi], 'rotation': r, 'shade': shade,
          'faces': {'west': face(tex, uv), 'east': face(tex, uv)}}
    return [e1, e2]

def crop_planes(tex, h=16):
    """Four planes in a # shape like vanilla 'crop'."""
    uv = [0, 16 - h, 16, 16]
    es = []
    for x in (4, 12):
        es.append({'from': [x, -1, 0], 'to': [x, h - 1, 16], 'shade': False,
                   'faces': {'east': face(tex, uv), 'west': face(tex, uv)}})
    for z in (4, 12):
        es.append({'from': [0, -1, z], 'to': [16, h - 1, z], 'shade': False,
                   'faces': {'north': face(tex, uv), 'south': face(tex, uv)}})
    return es

def leaf_plane(tex, direction, h, size, tilt=-22.5, cx=8, cz=8, width=None):
    """Horizontal leaf plane, tip points to 'direction', tilted down/up by tilt."""
    s = size; w = width or size
    if direction == 'north':
        frm, to, axis, ang, rot = [cx - w / 2, h, cz - s], [cx + w / 2, h, cz], 'x', -tilt, 0
    elif direction == 'south':
        frm, to, axis, ang, rot = [cx - w / 2, h, cz], [cx + w / 2, h, cz + s], 'x', tilt, 180
    elif direction == 'east':
        frm, to, axis, ang, rot = [cx, h, cz - w / 2], [cx + s, h, cz + w / 2], 'z', -tilt, 90
    else:
        frm, to, axis, ang, rot = [cx - s, h, cz - w / 2], [cx, h, cz + w / 2], 'z', tilt, 270
    e = {'from': frm, 'to': to,
         'faces': {'up': face(tex, [0, 0, 16, 16], rot), 'down': face(tex, [0, 16, 16, 0], (360 - rot) % 360 if rot in (90, 270) else rot)}}
    if ang:
        e['rotation'] = {'origin': [cx, h, cz], 'axis': axis, 'angle': ang}
    return e

def diag_leaf(tex, h, size, angle=45, cx=8, cz=8):
    """Flat leaf plane rotated ±45° around the Y axis (tip towards NE/SW ...)."""
    e = {'from': [cx - size / 2, h, cz - size], 'to': [cx + size / 2, h, cz],
         'rotation': {'origin': [cx, h, cz], 'axis': 'y', 'angle': angle},
         'faces': {'up': face(tex, [0, 0, 16, 16]), 'down': face(tex, [0, 16, 16, 0])}}
    return e

def model(rel, textures, elements=None, parent=None, render_type=None, ao=None, display=None):
    m = {}
    if parent: m['parent'] = parent
    if render_type: m['render_type'] = render_type
    if ao is not None: m['ambientocclusion'] = ao
    m['textures'] = textures
    if elements is not None: m['elements'] = elements
    if display: m['display'] = display
    wjson(f'models/{rel}.json', m)

def stem(tex, h, w=1.0, cx=8, cz=8):
    return box([cx - w / 2, 0, cz - w / 2], [cx + w / 2, h, cz + w / 2], tex, faces=['north', 'south', 'east', 'west', 'up'])

# =====================================================================
# Plants
# =====================================================================
def gen_cannabis():
    for s in range(8):
        wtex(f'block/cannabis_stage{s}', P.hemp_side(s))
    for s in range(4, 8):
        wtex(f'block/cannabis_top_stage{s}', P.hemp_side(s, True))
    wtex('block/cannabis_fan', P.fan_leaf())
    wtex('block/cannabis_fan_young', P.fan_leaf(P.HEMP_YOUNG))
    wtex('block/cannabis_stem', P.hemp_stem())
    wtex('block/cannabis_cola', P.hemp_cola())

    # Leaf planes per stage: (height, direction|diag, size, tilt)
    lower = {
        0: [(1.5, 'east', 3.5, 22.5), (1.5, 'west', 3.5, 22.5)],
        1: [(2.5, 'north', 4.5, 22.5), (2.5, 'south', 4.5, 22.5), (4, 'east', 3.5, 22.5)],
        2: [(3, 'east', 6, 0), (3, 'west', 6, 0), (6, 'north', 5, 22.5), (6, 'south', 5, 22.5)],
        3: [(3, 'east', 8, -22.5), (3, 'west', 8, -22.5), (7, 'north', 7, 0), (7, 'south', 7, 0), (10.5, 'd45', 5, 0), (10.5, 'd225', 5, 0)],
    }
    full_lower = [(4, 'east', 10, -22.5), (4, 'west', 10, -22.5), (8, 'north', 9, -22.5), (8, 'south', 9, -22.5),
                  (11.5, 'd45', 8, 0), (11.5, 'd225', 8, 0), (15, 'east', 8, 0), (15, 'west', 8, 0)]
    for s in range(4, 8):
        lower[s] = full_lower
    upper = {
        4: [(2, 'north', 7, 22.5), (2, 'south', 7, 22.5), (4, 'd45', 4, 0)],
        5: [(2, 'north', 8, 0), (2, 'south', 8, 0), (6, 'east', 6, 22.5), (6, 'west', 6, 22.5), (8.5, 'd45', 4, 0)],
        6: [(2, 'north', 8, -22.5), (2, 'south', 8, -22.5), (5.5, 'east', 7, 0), (5.5, 'west', 7, 0), (9, 'd45', 5, 22.5), (9, 'd225', 5, 22.5)],
        7: [(2, 'north', 8, -22.5), (2, 'south', 8, -22.5), (5.5, 'east', 7, -22.5), (5.5, 'west', 7, -22.5), (9, 'd45', 5.5, 0), (9, 'd225', 5.5, 0), (12, 'north', 4, 22.5), (12, 'south', 4, 22.5)],
    }
    heights_lower = [2, 4.5, 7, 11, 16, 16, 16, 16]
    heights_upper = {4: 5, 5: 9, 6: 13, 7: 15}

    def leaves(spec, young):
        tex = 'fan_young' if young else 'fan'
        out = []
        for h, d, size, tilt in spec:
            if d.startswith('d'):
                out.append(diag_leaf(tex, h, size, 45 if d == 'd45' else -45))
                out.append(diag_leaf(tex, h, size, 45 if d == 'd45' else -45, cx=8, cz=8) | {'from': [8 - size / 2, h, 8], 'to': [8 + size / 2, h, 8 + size],
                           'faces': {'up': face(tex, [0, 0, 16, 16], 180), 'down': face(tex, [0, 16, 16, 0], 180)}})
            else:
                out.append(leaf_plane(tex, d, h, size, tilt))
        return out

    for s in range(8):
        els = cross('side', 16) + leaves(lower[s], s <= 2)
        if s >= 1:
            els.insert(0, stem('stem', heights_lower[s], 1.0))
        model(f'block/cannabis_stage{s}', {'particle': f'{NS}:block/cannabis_stage{s}', 'side': f'{NS}:block/cannabis_stage{s}',
                                           'stem': f'{NS}:block/cannabis_stem', 'fan': f'{NS}:block/cannabis_fan',
                                           'fan_young': f'{NS}:block/cannabis_fan_young'}, els, render_type='minecraft:cutout', ao=False)
    for s in range(4, 8):
        els = [stem('stem', heights_upper[s], 0.8)] + cross('side', 16) + leaves(upper[s], False)
        if s >= 6:
            # Main cola: tapering, made of two cuboids
            top = heights_upper[s]
            r1 = 1.25 if s == 6 else 1.5
            els.append(box([8 - r1, top - 6, 8 - r1], [8 + r1, top - 1.5, 8 + r1], 'cola'))
            els.append(box([8 - r1 + 0.5, top - 1.5, 8 - r1 + 0.5], [8 + r1 - 0.5, top + 0.5, 8 + r1 - 0.5], 'cola'))
            # Side colas on the branches
            for (dx, dz, hh) in ((-3.5, 0, 5), (3.5, 0.5, 7.5), (0, -3.5, 4), (0.5, 3.5, 6.5)):
                if s == 6 and hh > 6:
                    continue
                els.append(box([8 + dx - 0.75, hh - 2, 8 + dz - 0.75], [8 + dx + 0.75, hh + 0.5, 8 + dz + 0.75], 'cola'))
        model(f'block/cannabis_top_stage{s}', {'particle': f'{NS}:block/cannabis_top_stage{s}', 'side': f'{NS}:block/cannabis_top_stage{s}',
                                               'stem': f'{NS}:block/cannabis_stem', 'fan': f'{NS}:block/cannabis_fan',
                                               'cola': f'{NS}:block/cannabis_cola'}, els, render_type='minecraft:cutout', ao=False)
    variants = {}
    for s in range(8):
        variants[f'age={s},half=lower'] = {'model': f'{NS}:block/cannabis_stage{s}'}
        variants[f'age={s},half=upper'] = {'model': f'{NS}:block/cannabis_top_stage{s}' if s >= 4 else f'{NS}:block/empty'}
    wjson('blockstates/cannabis_crop.json', {'variants': variants})

def gen_tobacco():
    for s in range(8):
        wtex(f'block/tobacco_stage{s}', P.tobacco_side(s))
    wtex('block/tobacco_leaf', P.tobacco_leaf_top())
    wtex('block/tobacco_flower', P.tobacco_flower())
    H = [3, 5, 7, 9, 11, 13, 14, 15]
    spec = {
        0: [(1, 'east', 3, 22.5), (1, 'west', 3, 22.5)],
        1: [(1.5, 'north', 5, 0), (1.5, 'south', 5, 0)],
        2: [(1.5, 'east', 7, -22.5), (1.5, 'west', 7, -22.5), (4, 'north', 5, 22.5)],
        3: [(1.5, 'east', 8, -22.5), (1.5, 'west', 8, -22.5), (4.5, 'north', 7, 0), (4.5, 'south', 7, 0), (7.5, 'east', 5, 22.5)],
    }
    big = [(1.5, 'east', 9, -22.5), (1.5, 'west', 9, -22.5), (5, 'north', 8, -22.5), (5, 'south', 8, -22.5),
           (8.5, 'east', 7, 0), (8.5, 'west', 7, 0), (11.5, 'north', 5, 22.5)]
    for s in range(4, 8):
        spec[s] = big
    for s in range(8):
        els = [stem('stem', H[s], 1.2)] + cross('side', 16)
        for h, d, size, tilt in spec[s]:
            els.append(leaf_plane('leaf', d, h, size, tilt, width=size * 0.75))
        if s >= 6:
            fl = cross('flower', 6)
            for e in fl:
                e['from'][1] += H[s] - 3; e['to'][1] += H[s] - 3
            els += fl
        model(f'block/tobacco_stage{s}', {'particle': f'{NS}:block/tobacco_stage{s}', 'side': f'{NS}:block/tobacco_stage{s}',
                                          'stem': 'minecraft:block/kelp_plant', 'leaf': f'{NS}:block/tobacco_leaf',
                                          'flower': f'{NS}:block/tobacco_flower'}, els, render_type='minecraft:cutout', ao=False)
    wjson('blockstates/tobacco_crop.json', {'variants': {f'age={s}': {'model': f'{NS}:block/tobacco_stage{s}'} for s in range(8)}})

def gen_poppy():
    for s in range(8):
        wtex(f'block/poppy_stage{s}', P.poppy_side(s))
    wtex('block/poppy_petal', P.poppy_petal())
    wtex('block/poppy_pod', P.poppy_pod_tex())
    wtex('block/poppy_crown', P.poppy_crown_tex())
    leaf = Canvas(); leaflet(leaf, 8, 15.5, 0, 15, 6, P.POP_LEAF['d'], P.POP_LEAF['l'], P.POP_LEAF['h'], serrate=True, vein=P.POP_LEAF['v'])
    leaf.edge_darken(0.8); wtex('block/poppy_leaf', leaf)
    stem_tex = Canvas()
    for y in range(16):
        for x in range(16):
            stem_tex.set(x, y, P.POP_STEM if x % 3 else P.POP_STEM_D)
    wtex('block/poppy_stem', stem_tex)
    stalks = [(8, 8, 0), (5.5, 6.5, -2), (10.5, 9.5, -3)]   # x, z, height offset
    Hs = [3, 5, 8, 10, 12, 14, 15, 15]
    for s in range(8):
        els = cross('side', 16)
        n_leaf = min(4, 1 + s // 2)
        dirs = ['east', 'west', 'north', 'south']
        for i in range(n_leaf):
            els.append(leaf_plane('leaf', dirs[i], 1 + i * 0.5, 5 + min(s, 4), -22.5, width=4))
        if s >= 4:
            for (x, z, off) in stalks:
                h = Hs[s] + off
                els.append(stem('stem', h, 0.6, x, z))
                if s == 4:
                    els.append(box([x - 0.75, h - 1.5, z - 0.75], [x + 0.75, h, z + 0.75], 'pod'))
                elif s == 5:
                    els.append(box([x - 2, h, z - 2], [x + 2, h + 2, z + 2], {'north': 'petal', 'south': 'petal', 'east': 'petal', 'west': 'petal', 'up': 'petal', 'down': 'pod'},
                                   uvs={d: [4, 0, 12, 4] for d in ('north', 'south', 'east', 'west')}))
                else:
                    r = 1.25 if s == 6 else 1.6
                    els.append(box([x - r, h, z - r], [x + r, h + 2 * r + 0.5, z + r], {'north': 'pod', 'south': 'pod', 'east': 'pod', 'west': 'pod', 'up': 'crown', 'down': 'pod'}))
                    els.append(box([x - r - 0.3, h + 2 * r + 0.5, z - r - 0.3], [x + r + 0.3, h + 2 * r + 0.9, z + r + 0.3], 'crown'))
        model(f'block/poppy_stage{s}', {'particle': f'{NS}:block/poppy_stage{s}', 'side': f'{NS}:block/poppy_stage{s}',
                                        'leaf': f'{NS}:block/poppy_leaf', 'stem': f'{NS}:block/poppy_stem', 'petal': f'{NS}:block/poppy_petal',
                                        'pod': f'{NS}:block/poppy_pod', 'crown': f'{NS}:block/poppy_crown'}, els, render_type='minecraft:cutout', ao=False)
    wjson('blockstates/poppy_crop.json', {'variants': {f'age={s}': {'model': f'{NS}:block/poppy_stage{s}'} for s in range(8)}})

def gen_coca():
    for s in range(5):
        wtex(f'block/coca_stage{s}', P.coca_side(s))
    berry = Canvas()
    berry.rect(0, 0, 15, 15, hexc('#c62828')); berry.rect(0, 0, 7, 7, hexc('#ef5350'))
    wtex('block/coca_berry', berry)
    H = [5, 9, 12, 15, 16]
    for s in range(5):
        els = cross('side', 16)
        if s >= 2:
            els += crop_planes('side', 16)
        els.insert(0, stem('wood', min(5, H[s] - 2), 1.2))
        if s == 4:
            for (x, y, z) in ((4, 9, 6), (11, 11, 5), (6, 13, 11), (10, 7, 11), (12, 14, 9), (5, 12, 4)):
                els.append(box([x, y, z], [x + 1, y + 1, z + 1], 'berry'))
        model(f'block/coca_stage{s}', {'particle': f'{NS}:block/coca_stage{s}', 'side': f'{NS}:block/coca_stage{s}',
                                       'wood': 'minecraft:block/stripped_jungle_log', 'berry': f'{NS}:block/coca_berry'},
              els, render_type='minecraft:cutout', ao=False)
    wjson('blockstates/coca_bush.json', {'variants': {f'age={s}': {'model': f'{NS}:block/coca_stage{s}'} for s in range(5)}})

def mushroom_els(x, z, stem_h, cap_w, cap_h):
    els = [box([x - 0.5, 0, z - 0.5], [x + 0.5, stem_h, z + 0.5], 'stem', faces=['north', 'south', 'east', 'west'])]
    c = cap_w / 2
    els.append(box([x - c, stem_h, z - c], [x + c, stem_h + cap_h, z + c],
                   {'north': 'capside', 'south': 'capside', 'east': 'capside', 'west': 'capside', 'up': 'cap', 'down': 'gills'}))
    if cap_w >= 3:
        els.append(box([x - c + 0.5, stem_h + cap_h, z - c + 0.5], [x + c - 0.5, stem_h + cap_h + 0.75, z + c - 0.5], 'cap'))
    return els

def gen_mushroom():
    wtex('block/magic_mushroom_cap', P.mushroom_cap_tex())
    wtex('block/magic_mushroom_cap_side', P.mushroom_side_tex())
    wtex('block/magic_mushroom_stem', P.mushroom_stem_tex())
    wtex('block/magic_mushroom_gills', P.mushroom_gills_tex())
    stages = {
        0: [(8, 8, 2, 2, 1), (6, 10, 1.5, 1.5, 1)],
        1: [(8, 8, 3.5, 3, 1.5), (5.5, 10, 2.5, 2, 1), (10.5, 6, 2, 2, 1)],
        2: [(8, 8, 5.5, 4, 2), (5, 10.5, 4, 3, 1.5), (11, 6, 3.5, 3, 1.5), (10.5, 11, 2.5, 2, 1), (5, 5, 2.5, 2, 1)],
    }
    tex = {'particle': f'{NS}:block/magic_mushroom_cap', 'cap': f'{NS}:block/magic_mushroom_cap', 'capside': f'{NS}:block/magic_mushroom_cap_side',
           'stem': f'{NS}:block/magic_mushroom_stem', 'gills': f'{NS}:block/magic_mushroom_gills'}
    for s, ms in stages.items():
        els = []
        for m in ms:
            els += mushroom_els(*m)
        model(f'block/magic_mushroom_stage{s}', tex, els, render_type='minecraft:cutout')
    wjson('blockstates/magic_mushroom.json', {'variants': {f'age={s}': {'model': f'{NS}:block/magic_mushroom_stage{s}'} for s in range(3)}})

def gen_wild():
    pairs = {'wild_cannabis': ('cannabis_stage3', 'cannabis_stage3'), 'wild_tobacco': ('tobacco_stage5', 'tobacco_stage5'),
             'wild_coca': ('coca_stage3', 'coca_stage3'), 'wild_poppy': ('poppy_stage5', 'poppy_stage5')}
    for name, (mdl, tex) in pairs.items():
        wjson(f'blockstates/{name}.json', {'variants': {'': [{'model': f'{NS}:block/{mdl}'}, {'model': f'{NS}:block/{mdl}', 'y': 90},
                                                              {'model': f'{NS}:block/{mdl}', 'y': 180}, {'model': f'{NS}:block/{mdl}', 'y': 270}]}})
        item_tex = {'wild_cannabis': 'item/cannabis_leaf', 'wild_tobacco': 'item/tobacco_leaf', 'wild_coca': 'item/coca_leaves',
                    'wild_poppy': 'block/poppy_petal'}[name]
        model(f'item/{name}', {'layer0': f'{NS}:{item_tex}'}, parent='minecraft:item/generated')

# =====================================================================
# Machines & equipment
# =====================================================================
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

def facing_states(name, models_by_lit=None, model_name=None):
    variants = {}
    for f, y in FACING_Y.items():
        if models_by_lit:
            for lit, m in models_by_lit.items():
                v = {'model': f'{NS}:block/{m}'}
                if y: v['y'] = y
                variants[f'facing={f},lit={lit}'] = v
        else:
            v = {'model': f'{NS}:block/{model_name or name}'}
            if y: v['y'] = y
            variants[f'facing={f}'] = v
    wjson(f'blockstates/{name}.json', {'variants': variants})

def block_item(name, model_name=None):
    model(f'item/{name}', {}, parent=f'{NS}:block/{model_name or name}')

def gen_machines():
    # ---- Textures ----
    wtex('block/grinder_side', B.grinder_side()); wtex('block/grinder_front', B.grinder_front()); wtex('block/grinder_front_on', B.grinder_front(True))
    wtex('block/millstone', B.millstone()); wtex('block/millstone_top', B.millstone_top())
    wtex('block/extractor_base', B.extractor_base()); wtex('block/extractor_front', B.extractor_base(True)); wtex('block/extractor_front_on', B.extractor_base(True, True))
    wtex('block/extractor_cap', B.extractor_cap()); wtex('block/lab_glass', B.glass_tex())
    wtex('block/extractor_liquid', B.liquid_frames('#3a9a3a', '#7ad86a'), {'animation': {'frametime': 3, 'interpolate': True}})
    idle = Canvas(); [idle.set(x, y, hexc('#3a8a3a', 200) if y > 1 else hexc('#6ac85a', 200)) for y in range(16) for x in range(16)]
    wtex('block/extractor_liquid_idle', idle)
    wtex('block/lab_side', B.lab_side()); wtex('block/lab_front', B.lab_front()); wtex('block/lab_top', B.lab_top()); wtex('block/burner', B.burner())
    purple = Canvas(); [purple.set(x, y, hexc('#a040c0', 210) if y > 1 else hexc('#d080f0', 210)) for y in range(16) for x in range(16)]
    wtex('block/lab_liquid', purple)
    blue = Canvas(); [blue.set(x, y, hexc('#2fa8e0', 210)) for y in range(16) for x in range(16)]
    wtex('block/lab_liquid_blue', blue)
    wtex('block/press_base', B.press_base()); wtex('block/press_column', B.press_column()); wtex('block/press_plate', B.press_plate()); wtex('block/press_top', B.press_top())
    wtex('block/tent_fabric', B.tent_fabric()); wtex('block/tent_zipper', B.tent_fabric(True)); wtex('block/tent_mylar', B.tent_mylar())
    wtex('block/tent_frame', B.tent_frame()); wtex('block/tent_lamp', B.tent_lamp(True)); wtex('block/tent_lamp_off', B.tent_lamp(False)); wtex('block/tent_soil', B.soil())
    wtex('block/grow_lamp_bottom', B.lamp_bottom()); wtex('block/grow_lamp_top', B.lamp_top()); wtex('block/grow_lamp_side', B.lamp_side())
    wtex('block/bong_glass', B.bong_glass()); wtex('block/bong_water', B.bong_water()); wtex('block/bong_metal', B.bong_metal())
    wtex('block/scale_top', B.scale_top()); wtex('block/scale_side', B.scale_side()); wtex('block/rope', B.rope())

    # ---- Grinder ----
    for lit in (False, True):
        front = 'front_on' if lit else 'front'
        els = [
            box([0, 0, 0], [16, 9, 16], {'north': front, 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'stone_top', 'down': 'bottom'}),
            box([1, 9, 1], [15, 12, 15], {'north': 'mill', 'south': 'mill', 'east': 'mill', 'west': 'mill', 'up': 'mill_top', 'down': 'mill'}),
            box([2, 12, 2], [14, 14.5, 14], {'north': 'mill', 'south': 'mill', 'east': 'mill', 'west': 'mill', 'up': 'mill_top', 'down': 'mill'}),
            box([5, 14.5, 5], [11, 16, 11], {'north': 'wood', 'south': 'wood', 'east': 'wood', 'west': 'wood', 'up': 'hopper', 'down': 'wood'}),
            box([7.5, 14.5, 7.5], [13, 15.5, 8.5], 'wood'),            # crank arm
            box([12, 15.5, 7.5], [13, 18, 8.5], 'handle'),             # handle
        ]
        model(f'block/grinder{"_on" if lit else ""}', {'particle': f'{NS}:block/grinder_side', 'side': f'{NS}:block/grinder_side',
                                                       'front': f'{NS}:block/grinder_front', 'front_on': f'{NS}:block/grinder_front_on',
                                                       'stone_top': 'minecraft:block/smooth_stone', 'bottom': 'minecraft:block/smooth_stone',
                                                       'mill': f'{NS}:block/millstone', 'mill_top': f'{NS}:block/millstone_top',
                                                       'wood': 'minecraft:block/stripped_dark_oak_log', 'hopper': 'minecraft:block/hopper_inside',
                                                       'handle': 'minecraft:block/spruce_planks'}, els, parent='minecraft:block/block')
    facing_states('grinder', {'false': 'grinder', 'true': 'grinder_on'}); block_item('grinder')

    # ---- Extractor ----
    for lit in (False, True):
        els = [
            box([1, 0, 1], [15, 4, 15], {'north': 'front_on' if lit else 'front', 'south': 'base', 'east': 'base', 'west': 'base', 'up': 'base', 'down': 'base'}),
            box([3.5, 4, 3.5], [12.5, 12, 12.5], 'liquid', faces=['north', 'south', 'east', 'west', 'up']),
            box([3, 4, 3], [13, 14, 13], 'glass', faces=['north', 'south', 'east', 'west']),
            box([2, 14, 2], [14, 16, 14], 'cap'),
            box([7, 16, 7], [9, 18, 9], 'pipe'),
            box([9, 17, 7.25], [14, 18, 8.75], 'pipe'),
            box([13, 13, 7.25], [14.5, 17, 8.75], 'pipe'),
        ]
        model(f'block/extractor{"_on" if lit else ""}', {'particle': f'{NS}:block/extractor_base', 'base': f'{NS}:block/extractor_base',
                                                         'front': f'{NS}:block/extractor_front', 'front_on': f'{NS}:block/extractor_front_on',
                                                         'glass': f'{NS}:block/lab_glass', 'cap': f'{NS}:block/extractor_cap', 'pipe': 'minecraft:block/copper_block',
                                                         'liquid': f'{NS}:block/extractor_liquid' if lit else f'{NS}:block/extractor_liquid_idle'},
              els, parent='minecraft:block/block', render_type='minecraft:translucent')
    facing_states('extractor', {'false': 'extractor', 'true': 'extractor_on'}); block_item('extractor')

    # ---- Chemistry lab ----
    for lit in (False, True):
        els = [
            box([0, 0, 0], [16, 11, 16], {'north': 'front', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'top', 'down': 'side'}),
            box([-0.5, 11, -0.5], [16.5, 12, 16.5], 'top'),
            # Erlenmeyer flask
            box([3.5, 12, 4.5], [7.5, 14, 8.5], 'liquid', faces=['north', 'south', 'east', 'west', 'up']),
            box([3, 12, 4], [8, 15, 9], 'glass', faces=['north', 'south', 'east', 'west', 'up']),
            box([4.5, 15, 5.5], [6.5, 17, 7.5], 'glass', faces=['north', 'south', 'east', 'west']),
            # test tube rack
            box([9.5, 12, 3], [14.5, 13, 6], 'rack'),
            box([10, 13, 4], [11, 16, 5], 'glass'), box([10.15, 13, 4.15], [10.85, 14.5, 4.85], 'liquid2'),
            box([12, 13, 4], [13, 16, 5], 'glass'), box([12.15, 13, 4.15], [12.85, 15, 4.85], 'liquid'),
            # burner
            box([10, 12, 9.5], [13, 13, 12.5], 'burner'),
            box([11, 13, 10.5], [12, 14.5, 11.5], 'burner'),
        ]
        if lit:
            fl = cross('fire', 6)
            for e in fl:
                e['from'] = [e['from'][0] * 0.25 + 9.5, 14.5, e['from'][2] * 0.25 + 9 if e['from'][2] != 8 else 11]
                e['to'] = [e['to'][0] * 0.25 + 9.5, 17.5, e['to'][2] * 0.25 + 9 if e['to'][2] != 8 else 11]
                e['rotation'] = {'origin': [11.5, 15, 11], 'axis': 'y', 'angle': 45, 'rescale': True}
            els += [
                {'from': [10.5, 14.5, 11], 'to': [12.5, 17.5, 11], 'shade': False, 'rotation': {'origin': [11.5, 15, 11], 'axis': 'y', 'angle': 45},
                 'faces': {'north': face('fire', [0, 0, 16, 16]), 'south': face('fire', [0, 0, 16, 16])}},
                {'from': [11.5, 14.5, 10], 'to': [11.5, 17.5, 12], 'shade': False, 'rotation': {'origin': [11.5, 15, 11], 'axis': 'y', 'angle': 45},
                 'faces': {'east': face('fire', [0, 0, 16, 16]), 'west': face('fire', [0, 0, 16, 16])}},
            ]
        model(f'block/lab_station{"_on" if lit else ""}', {'particle': f'{NS}:block/lab_side', 'side': f'{NS}:block/lab_side', 'front': f'{NS}:block/lab_front',
                                                           'top': f'{NS}:block/lab_top', 'glass': f'{NS}:block/lab_glass', 'liquid': f'{NS}:block/lab_liquid',
                                                           'liquid2': f'{NS}:block/lab_liquid_blue', 'rack': 'minecraft:block/spruce_planks',
                                                           'burner': f'{NS}:block/burner', 'fire': 'minecraft:block/fire_0'},
              els, parent='minecraft:block/block', render_type='minecraft:translucent')
    facing_states('lab_station', {'false': 'lab_station', 'true': 'lab_station_on'}); block_item('lab_station')

    # ---- Press ----
    for lit in (False, True):
        py = 5 if lit else 8
        els = [
            box([0, 0, 0], [16, 5, 16], {'north': 'base', 'south': 'base', 'east': 'base', 'west': 'base', 'up': 'plate', 'down': 'bottom'}),
            box([1, 5, 1], [3, 14, 3], 'col'), box([13, 5, 1], [15, 14, 3], 'col'),
            box([1, 5, 13], [3, 14, 15], 'col'), box([13, 5, 13], [15, 14, 15], 'col'),
            box([0.5, 14, 0.5], [15.5, 16, 15.5], {'north': 'beam', 'south': 'beam', 'east': 'beam', 'west': 'beam', 'up': 'top', 'down': 'beam'}),
            box([6, py + 2, 6], [10, 14, 10], 'col'),
            box([3, py, 3], [13, py + 2, 13], 'plate'),
            box([4, 5, 4], [12, 6, 12], 'cake'),
        ]
        model(f'block/press{"_on" if lit else ""}', {'particle': f'{NS}:block/press_base', 'base': f'{NS}:block/press_base', 'col': f'{NS}:block/press_column',
                                                     'plate': f'{NS}:block/press_plate', 'top': f'{NS}:block/press_top', 'beam': 'minecraft:block/iron_block',
                                                     'bottom': 'minecraft:block/smooth_stone', 'cake': f'{NS}:item/hashish'},
              els, parent='minecraft:block/block')
    facing_states('press', {'false': 'press', 'true': 'press_on'}); block_item('press')

    # ---- Grow tent: two blocks tall, front open to the north ----
    # Lower half: floor + walls (open top). Upper half: walls, roof, rolled-up door and the lamp.
    # The walls meet at the seam without inner faces; the lamp (bottom at 1.875 blocks) sits above a mature cannabis plant.
    tent_tex = {'particle': f'{NS}:block/tent_fabric', 'fabric': f'{NS}:block/tent_fabric', 'zipper': f'{NS}:block/tent_zipper',
                'mylar': f'{NS}:block/tent_mylar', 'frame': f'{NS}:block/tent_frame', 'soil': f'{NS}:block/tent_soil'}
    lower = [
        box([0.5, 0, 0.5], [15.5, 1, 15.5], {'north': 'frame', 'south': 'frame', 'east': 'frame', 'west': 'frame', 'up': 'soil', 'down': 'frame'}),
        box([0, 0, 15], [16, 16, 16], {'south': 'fabric', 'north': 'mylar', 'east': 'fabric', 'west': 'fabric', 'down': 'fabric'}),
        box([0, 0, 0], [1, 16, 15], {'west': 'fabric', 'east': 'mylar', 'north': 'fabric', 'south': 'fabric', 'down': 'fabric'}),
        box([15, 0, 0], [16, 16, 15], {'east': 'fabric', 'west': 'mylar', 'north': 'fabric', 'south': 'fabric', 'down': 'fabric'}),
        box([1, 0, 0], [2.5, 16, 1], {'north': 'fabric', 'south': 'fabric', 'east': 'fabric', 'west': 'fabric', 'down': 'fabric'}),   # door flaps
        box([13.5, 0, 0], [15, 16, 1], {'north': 'fabric', 'south': 'fabric', 'east': 'fabric', 'west': 'fabric', 'down': 'fabric'}),
    ]
    def tent_upper():
        return [
            box([0, 0, 15], [16, 16, 16], {'south': 'fabric', 'north': 'mylar', 'east': 'fabric', 'west': 'fabric', 'up': 'fabric'}),
            box([0, 0, 0], [1, 16, 15], {'west': 'fabric', 'east': 'mylar', 'north': 'fabric', 'south': 'fabric', 'up': 'fabric'}),
            box([15, 0, 0], [16, 16, 15], {'east': 'fabric', 'west': 'mylar', 'north': 'fabric', 'south': 'fabric', 'up': 'fabric'}),
            box([1, 15, 0], [15, 16, 15], {'up': 'fabric', 'down': 'mylar', 'north': 'fabric', 'south': 'fabric'}),
            box([1, 12.5, 0], [15, 15, 1.5], {'north': 'zipper', 'south': 'fabric', 'down': 'fabric', 'east': 'fabric', 'west': 'fabric'}),  # rolled-up door
            box([1, 0, 0], [2.5, 12.5, 1], {'north': 'fabric', 'south': 'fabric', 'east': 'fabric', 'west': 'fabric'}),
            box([13.5, 0, 0], [15, 12.5, 1], {'north': 'fabric', 'south': 'fabric', 'east': 'fabric', 'west': 'fabric'}),
            box([3, 14, 3], [13, 14.75, 13], {'down': 'lamp', 'north': 'frame', 'south': 'frame', 'east': 'frame', 'west': 'frame'}),
            box([2, 14.75, 7.5], [14, 15, 8.5], 'frame'),
        ]
    model('block/grow_tent_lower', tent_tex, lower, parent='minecraft:block/block', render_type='minecraft:cutout')
    for lit in (False, True):
        model(f'block/grow_tent_upper{"_on" if lit else ""}', {**tent_tex, 'lamp': f'{NS}:block/tent_lamp' if lit else f'{NS}:block/tent_lamp_off'},
              tent_upper(), parent='minecraft:block/block', render_type='minecraft:cutout')
    variants = {}
    for f, y in FACING_Y.items():
        for half in ('lower', 'upper'):
            for lit in ('false', 'true'):
                m = 'grow_tent_lower' if half == 'lower' else ('grow_tent_upper_on' if lit == 'true' else 'grow_tent_upper')
                v = {'model': f'{NS}:block/{m}'}
                if y: v['y'] = y
                variants[f'facing={f},half={half},lit={lit}'] = v
    wjson('blockstates/grow_tent.json', {'variants': variants})
    # Item: both halves in one model (y 0..32), scaled down so the tall tent fits into a slot
    item_els = lower + [shift_y(e, 16) for e in tent_upper()]
    model('item/grow_tent', {**tent_tex, 'lamp': f'{NS}:block/tent_lamp'}, item_els, render_type='minecraft:cutout', display={
        'gui': {'rotation': [30, 225, 0], 'translation': [0, -3.25, 0], 'scale': [0.4, 0.4, 0.4]},
        'ground': {'rotation': [0, 0, 0], 'translation': [0, 1.5, 0], 'scale': [0.18, 0.18, 0.18]},
        'fixed': {'rotation': [0, 0, 0], 'translation': [0, -3.5, 0], 'scale': [0.4, 0.4, 0.4]},
        'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 1], 'scale': [0.25, 0.25, 0.25]},
        'thirdperson_lefthand': {'rotation': [75, 225, 0], 'translation': [0, 1.5, 1], 'scale': [0.25, 0.25, 0.25]},
        'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.22, 0.22, 0.22]},
        'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2.5, 0], 'scale': [0.22, 0.22, 0.22]},
        'head': {'rotation': [0, 0, 0], 'translation': [0, 6, 0], 'scale': [0.5, 0.5, 0.5]},
    })

    # ---- Grow lamp ----
    els = [
        box([1, 12, 1], [15, 14, 15], {'down': 'bottom', 'up': 'top', 'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side'}),
        box([7, 14, 7], [9, 16, 9], 'top'),
        box([1.5, 11.5, 1.5], [14.5, 12, 14.5], {'down': 'bottom'}),
    ]
    model('block/grow_lamp', {'particle': f'{NS}:block/grow_lamp_side', 'bottom': f'{NS}:block/grow_lamp_bottom', 'top': f'{NS}:block/grow_lamp_top',
                              'side': f'{NS}:block/grow_lamp_side'}, els, parent='minecraft:block/block')
    wjson('blockstates/grow_lamp.json', {'variants': {'': {'model': f'{NS}:block/grow_lamp'}}}); block_item('grow_lamp')

    # ---- Drying rack ----
    els = [
        box([0, 0, 7], [2, 16, 9], 'post'), box([14, 0, 7], [16, 16, 9], 'post'),
        box([0, 0, 4.5], [2, 1.5, 11.5], 'post'), box([14, 0, 4.5], [16, 1.5, 11.5], 'post'),
        box([0, 13, 7.25], [16, 14.5, 8.75], 'bar'),
    ]
    for off in (-0.29, -0.097, 0.097, 0.29):
        x = 8 + off * 16
        els.append(box([x - 0.3, 11.2, 7.7], [x + 0.3, 13, 8.3], 'rope'))
    model('block/drying_rack', {'particle': 'minecraft:block/stripped_spruce_log', 'post': 'minecraft:block/stripped_spruce_log',
                                'bar': 'minecraft:block/spruce_log', 'rope': f'{NS}:block/rope'}, els, parent='minecraft:block/block')
    facing_states('drying_rack'); block_item('drying_rack')

    # ---- Bong (Multipart) ----
    base = [
        box([4, 0, 4], [12, 5, 12], 'glass'),
        box([6, 5, 6], [10, 14, 10], 'glass', faces=['north', 'south', 'east', 'west']),
        box([5.5, 14, 5.5], [10.5, 15, 10.5], 'glass'),
        box([10, 2.5, 7.5], [14, 3.5, 8.5], 'metal', rot={'origin': [10, 3, 8], 'axis': 'z', 'angle': 45}),
        box([12, 5, 7], [14.5, 6.5, 9], 'metal'),
    ]
    model('block/bong', {'particle': f'{NS}:block/bong_glass', 'glass': f'{NS}:block/bong_glass', 'metal': f'{NS}:block/bong_metal'}, base,
          parent='minecraft:block/block', render_type='minecraft:translucent')
    model('block/bong_water', {'particle': f'{NS}:block/bong_water', 'water': f'{NS}:block/bong_water'},
          [box([4.5, 0.5, 4.5], [11.5, 3.5, 11.5], 'water'), box([6.5, 3.5, 6.5], [9.5, 6, 9.5], 'water', faces=['north', 'south', 'east', 'west', 'up'])],
          render_type='minecraft:translucent')
    for load, tex in (('cannabis', 'item/ground_cannabis'), ('tobacco', 'item/cut_tobacco'), ('opium', 'item/opium')):
        model(f'block/bong_load_{load}', {'particle': f'{NS}:{tex}', 'fill': f'{NS}:{tex}'},
              [box([12.3, 6.5, 7.3], [14.2, 6.9, 8.7], 'fill', faces=['up', 'north', 'south', 'east', 'west'])], render_type='minecraft:cutout')
    mp = []
    for f, y in FACING_Y.items():
        base_part = {'model': f'{NS}:block/bong'}
        if y: base_part['y'] = y
        mp.append({'when': {'facing': f}, 'apply': base_part})
        for load in ('cannabis', 'tobacco', 'opium'):
            p = {'model': f'{NS}:block/bong_load_{load}'}
            if y: p['y'] = y
            mp.append({'when': {'facing': f, 'load': load}, 'apply': p})
    mp.append({'when': {'water': 'true'}, 'apply': {'model': f'{NS}:block/bong_water'}})
    wjson('blockstates/bong.json', {'multipart': mp}); block_item('bong')

    # ---- Precision scale ----
    els = [
        box([3, 0, 3], [13, 1.5, 13], {'up': 'top', 'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'down': 'side'}),
        box([3.5, 1.5, 3.5], [12.5, 1.8, 10], {'up': 'plate', 'north': 'plate', 'south': 'plate', 'east': 'plate', 'west': 'plate'}),
    ]
    model('block/precision_scale', {'particle': f'{NS}:block/scale_side', 'top': f'{NS}:block/scale_top', 'side': f'{NS}:block/scale_side',
                                    'plate': 'minecraft:block/iron_block'}, els, parent='minecraft:block/block')
    facing_states('precision_scale'); block_item('precision_scale')

    # ---- Empty model ----
    model('block/empty', {'particle': f'{NS}:block/cannabis_stem'}, [])

# =====================================================================
# Items, GUI, effects, particles, villagers, shaders
# =====================================================================
def gen_items():
    handheld = {'joint', 'spliff', 'cigarette', 'cigar', 'scoring_knife', 'empty_syringe', 'morphine_syringe', 'smoking_pipe'}
    for name, cv in I.all_items().items():
        wtex(f'item/{name}', cv)
        model(f'item/{name}', {'layer0': f'{NS}:item/{name}'}, parent='minecraft:item/handheld' if name in handheld else 'minecraft:item/generated')
    for name, cv in I.textures_3d().items():
        wtex(f'item/3d/{name}', cv)

def gen_misc():
    for m in B.MACHINE_LAYOUT:
        wtex(f'gui/{m}', B.gui_texture(m))
    for name, cv in B.effect_icons().items():
        wtex(f'mob_effect/{name}', cv)
    for i, cv in enumerate(B.smoke_frames()):
        wtex(f'particle/smoke_puff_{i}', cv)
    for i, cv in enumerate(B.sparkle_frames()):
        wtex(f'particle/trip_sparkle_{i}', cv)
    wjson('particles/smoke_puff.json', {'textures': [f'{NS}:smoke_puff_{i}' for i in range(4)]})
    wjson('particles/trip_sparkle.json', {'textures': [f'{NS}:trip_sparkle_{i}' for i in range(4)]})
    wtex('misc/drug_vignette', B.vignette())
    dealer = B.dealer_villager()
    grower = B.grower_villager()
    wtex('entity/villager/profession/dealer', dealer, {'villager': {'hat': 'partial'}})
    wtex('entity/villager/profession/grower', grower, {'villager': {'hat': 'full'}})
    wtex('entity/zombie_villager/profession/dealer', dealer, {'villager': {'hat': 'partial'}})
    wtex('entity/zombie_villager/profession/grower', grower, {'villager': {'hat': 'full'}})

def gen_shaders():
    wjson('shaders/post/drugs.json', {
        'targets': ['swap'],
        'passes': [
            {'name': f'{NS}:drugs', 'intarget': 'minecraft:main', 'outtarget': 'swap'},
            {'name': 'blit', 'intarget': 'swap', 'outtarget': 'minecraft:main'},
        ]})
    uni = lambda n, v=0.0: {'name': n, 'type': 'float', 'count': 1, 'values': [v]}
    wjson('shaders/program/drugs.json', {
        'blend': {'func': 'add', 'srcrgb': 'one', 'dstrgb': 'zero'},
        'vertex': 'sobel',
        'fragment': f'{NS}:drugs',
        'attributes': ['Position'],
        'samplers': [{'name': 'DiffuseSampler'}],
        'uniforms': [
            {'name': 'ProjMat', 'type': 'matrix4x4', 'count': 16, 'values': [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]},
            {'name': 'InSize', 'type': 'float', 'count': 2, 'values': [1, 1]},
            {'name': 'OutSize', 'type': 'float', 'count': 2, 'values': [1, 1]},
            uni('HcTime'), uni('Stoned'), uni('Trip'), uni('Rush'), uni('Sedated'), uni('Withdrawal'), uni('Overdose'), uni('Acid'),
        ]})

# =====================================================================
# Drug lab: machines, furniture, armor
# =====================================================================
def stairs_states(name, base):
    variants = {}
    for facing, y0 in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
        for half in ('bottom', 'top'):
            for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                m = {'straight': base, 'inner_left': base + '_inner', 'inner_right': base + '_inner',
                     'outer_left': base + '_outer', 'outer_right': base + '_outer'}[shape]
                y = y0
                if shape in ('inner_left', 'outer_left'): y = (y + 270) % 360
                if half == 'top':
                    if shape in ('inner_left', 'outer_left'): y = (y + 90) % 360
                    elif shape in ('inner_right', 'outer_right'): y = (y + 90) % 360
                v = {'model': f'{NS}:block/{m}'}
                if half == 'top': v['x'] = 180
                if y: v['y'] = y
                if half == 'top' or y: v['uvlock'] = True
                variants[f'facing={facing},half={half},shape={shape}'] = v
    wjson(f'blockstates/{name}.json', {'variants': variants})

def gen_lab():
    T = lambda n: f'{NS}:block/{n}'
    tex = {
        'lab_tiles': LT.lab_tiles(), 'hazard_stripes': LT.hazard_stripes(),
        'reactor_base': LT.reactor_base(), 'reactor_base_on': LT.reactor_base(True), 'reactor_steel': LT.steel(4),
        'reactor_vessel': LT.reactor_vessel(), 'reactor_top': LT.reactor_top(),
        'reactor_liquid': LT.reactor_liquid(), 'reactor_liquid_on': LT.reactor_liquid(True),
        'crystallizer_side': LT.crystallizer_side(), 'crystallizer_front': LT.crystallizer_front(),
        'crystallizer_front_on': LT.crystallizer_front(True), 'crystallizer_top': LT.crystallizer_top(),
        'crystallizer_top_on': LT.crystallizer_top(True), 'lab_steel': LT.steel(30), 'brushed_steel': LT.brushed(31),
        'pill_press_paint': LT.press_paint(), 'pill_press_front': LT.pill_press_front(), 'pill_press_front_on': LT.pill_press_front(True),
        'pill_press_hopper': LT.pill_press_hopper(),
        'enamel': LT.enamel(), 'hood_inside': LT.hood_inside(), 'hood_glass': LT.hood_glass(), 'fan_front': LT.fan_front(), 'duct': LT.duct(),
        'counter_front': LT.counter_front(), 'counter_side': LT.counter_side(), 'counter_top': LT.counter_top(),
        'chemical_barrel_side': LT.barrel_side('#2f68b8', '#f0c020'), 'chemical_barrel_top': LT.barrel_top('#2f68b8'),
        'hazard_barrel_side': LT.barrel_side('#f0c020', '#f0c020', True), 'hazard_barrel_top': LT.barrel_top('#f0c020'),
        'gas_cylinder_side': LT.cylinder_side(), 'gas_cylinder_top': LT.cylinder_top(), 'lab_bottles': LT.bottles(),
        'plastic_sheeting': LT.plastic(), 'plastic_sheeting_top': LT.plastic_top(), 'hazard_sign': LT.hazard_sign(),
        'sign_back': LT.sign_back(), 'lab_light': LT.light_panel(), 'frost': LT.frost(),
    }
    for k, v in tex.items():
        wtex(f'block/{k}', v)
    wtex('models/armor/gas_mask_layer_1', LT.gas_mask_layer())
    wtex('models/armor/hazmat_layer_1', LT.hazmat_layer1())
    wtex('models/armor/hazmat_layer_2', LT.hazmat_layer2())

    # ---- Chemical reactor ----
    for lit in (False, True):
        els = [
            box([0, 0, 0], [16, 6, 16], {'north': 'base', 'south': 'steel', 'east': 'steel', 'west': 'steel', 'up': 'steel', 'down': 'steel'}),
            box([2, 6, 2], [14, 13, 14], 'vessel'),
            box([3, 13, 3], [13, 15, 13], {'north': 'vessel', 'south': 'vessel', 'east': 'vessel', 'west': 'vessel', 'up': 'top', 'down': 'vessel'}),
            box([5.5, 7.5, 1.4], [10.5, 12, 2], {'north': 'liquid', 'east': 'steel', 'west': 'steel', 'up': 'steel', 'down': 'steel'}),
            box([7, 15, 7], [9, 17.5, 9], 'pipe'), box([9, 16.5, 7.25], [15, 17.5, 8.75], 'pipe'), box([14, 6, 7.25], [15, 17.5, 8.75], 'pipe'),
            box([14, 9, 9.5], [15.5, 11, 11.5], 'valve'),
        ]
        model(f'block/chemical_reactor{"_on" if lit else ""}', {
            'particle': T('reactor_steel'), 'base': T('reactor_base_on' if lit else 'reactor_base'), 'steel': T('reactor_steel'),
            'vessel': T('reactor_vessel'), 'top': T('reactor_top'), 'liquid': T('reactor_liquid_on' if lit else 'reactor_liquid'),
            'pipe': 'minecraft:block/copper_block', 'valve': 'minecraft:block/red_concrete'}, els, parent='minecraft:block/block')
    facing_states('chemical_reactor', {'false': 'chemical_reactor', 'true': 'chemical_reactor_on'}); block_item('chemical_reactor')

    # ---- Crystallizer ----
    for lit in (False, True):
        els = [
            box([0, 0, 0], [16, 9, 16], {'north': 'front', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'tray', 'down': 'steel'}),
            box([0, 9, 0], [16, 11, 1], 'steel'), box([0, 9, 15], [16, 11, 16], 'steel'),
            box([0, 9, 1], [1, 11, 15], 'steel'), box([15, 9, 1], [16, 11, 15], 'steel'),
            box([1, 9.05, 1], [15, 9.1, 15], 'frost', faces=['up']),
        ]
        if lit:
            for (x, z, h, w_) in ((3, 4, 2.5, 2), (6.5, 9, 3.2, 1.6), (10, 4.5, 2.2, 2.2), (11.5, 10.5, 2.8, 1.5), (4, 11.5, 1.8, 1.8), (8, 6.5, 1.5, 1.5)):
                els.append({'from': [x, 9, z], 'to': [x + w_, 9 + h, z + w_],
                            'rotation': {'origin': [x + w_ / 2, 9, z + w_ / 2], 'axis': 'y', 'angle': 22.5},
                            'faces': {d: face('crystal') for d in ('north', 'south', 'east', 'west', 'up')}})
        model(f'block/crystallizer{"_on" if lit else ""}', {
            'particle': T('crystallizer_side'), 'front': T('crystallizer_front_on' if lit else 'crystallizer_front'),
            'side': T('crystallizer_side'), 'tray': T('crystallizer_top_on' if lit else 'crystallizer_top'), 'steel': T('lab_steel'),
            'frost': T('frost'), 'crystal': 'minecraft:block/calcite'}, els, parent='minecraft:block/block', render_type='minecraft:cutout')
    facing_states('crystallizer', {'false': 'crystallizer', 'true': 'crystallizer_on'}); block_item('crystallizer')

    # ---- Pill press ----
    for lit in (False, True):
        py = 8 if lit else 10
        els = [
            box([1, 0, 1], [15, 6, 15], {'north': 'front', 'south': 'paint', 'east': 'paint', 'west': 'paint', 'up': 'paint', 'down': 'paint'}),
            box([3, 6, 3], [13, 12, 13], 'paint'),
            box([4, 12, 4], [12, 16, 12], {'north': 'steel', 'south': 'steel', 'east': 'steel', 'west': 'steel', 'up': 'hopper', 'down': 'steel'}),
            box([6.5, py - 2, 1.5], [9.5, py, 3], 'steel'),
            box([7.25, py, 1.9], [8.75, 12, 2.6], 'steel'),
            box([13, 8, 7], [16, 9, 9], 'steel'), box([15.5, 7, 6], [16.5, 10, 10], 'lever'),
            box([6, 1, 0], [10, 3, 1], 'steel'),
        ]
        model(f'block/pill_press{"_on" if lit else ""}', {
            'particle': T('pill_press_paint'), 'front': T('pill_press_front_on' if lit else 'pill_press_front'), 'paint': T('pill_press_paint'),
            'steel': T('brushed_steel'), 'hopper': T('pill_press_hopper'), 'lever': 'minecraft:block/red_concrete'}, els, parent='minecraft:block/block')
    facing_states('pill_press', {'false': 'pill_press', 'true': 'pill_press_on'}); block_item('pill_press')

    # ---- Fume hood ----
    els = [
        box([0, 0, 0], [16, 7, 16], {'north': 'cab', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'side', 'down': 'side'}),
        box([0, 7, 0], [16, 8, 16], 'top'),
        box([0, 8, 0], [1, 16, 16], 'enamel'), box([15, 8, 0], [16, 16, 16], 'enamel'),
        box([1, 8, 15], [15, 16, 16], {'north': 'inside', 'south': 'enamel', 'up': 'enamel', 'down': 'enamel'}),
        box([1, 13.5, 0], [15, 16, 15], {'north': 'enamel', 'south': 'inside', 'up': 'enamel', 'down': 'inside'}),
        box([1, 8, 1], [15, 13.5, 1.5], 'glass', faces=['north', 'south']),
        box([6, 16, 7], [10, 19, 11], 'duct'),
    ]
    model('block/fume_hood', {'particle': T('enamel'), 'cab': T('counter_front'), 'side': T('counter_side'), 'top': T('counter_top'),
                              'enamel': T('enamel'), 'inside': T('hood_inside'), 'glass': T('hood_glass'), 'duct': T('duct')},
          els, parent='minecraft:block/block', render_type='minecraft:translucent')
    facing_states('fume_hood'); block_item('fume_hood')

    # ---- Exhaust fan (wall) ----
    model('block/exhaust_fan', {'particle': T('lab_steel'), 'front': T('fan_front'), 'steel': T('lab_steel')},
          [box([0, 0, 12], [16, 16, 16], {'north': 'front', 'south': 'steel', 'east': 'steel', 'west': 'steel', 'up': 'steel', 'down': 'steel'})],
          parent='minecraft:block/block')
    facing_states('exhaust_fan'); block_item('exhaust_fan')

    # ---- Tiles, slab, stairs, stripes ----
    for name in ('lab_tiles', 'hazard_stripes'):
        model(f'block/{name}', {'all': T(name)}, parent='minecraft:block/cube_all')
        wjson(f'blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/{name}'}}}); block_item(name)
    tt = {'bottom': T('lab_tiles'), 'top': T('lab_tiles'), 'side': T('lab_tiles')}
    model('block/lab_tile_slab', tt, parent='minecraft:block/slab')
    model('block/lab_tile_slab_top', tt, parent='minecraft:block/slab_top')
    wjson('blockstates/lab_tile_slab.json', {'variants': {'type=bottom': {'model': f'{NS}:block/lab_tile_slab'},
                                                          'type=top': {'model': f'{NS}:block/lab_tile_slab_top'},
                                                          'type=double': {'model': f'{NS}:block/lab_tiles'}}})
    block_item('lab_tile_slab')
    model('block/lab_tile_stairs', tt, parent='minecraft:block/stairs')
    model('block/lab_tile_stairs_inner', tt, parent='minecraft:block/inner_stairs')
    model('block/lab_tile_stairs_outer', tt, parent='minecraft:block/outer_stairs')
    stairs_states('lab_tile_stairs', 'lab_tile_stairs'); block_item('lab_tile_stairs')

    # ---- Counter ----
    model('block/lab_counter', {'particle': T('counter_side'), 'front': T('counter_front'), 'side': T('counter_side'), 'top': T('counter_top')},
          [box([0, 0, 0], [16, 15, 16], {'north': 'front', 'south': 'side', 'east': 'side', 'west': 'side', 'down': 'side'}),
           box([-0.5, 15, -0.5], [16.5, 16, 16.5], 'top')], parent='minecraft:block/block')
    facing_states('lab_counter'); block_item('lab_counter')

    # ---- Barrels, gas cylinder ----
    for name in ('chemical_barrel', 'hazard_barrel'):
        model(f'block/{name}', {'particle': T(f'{name}_side'), 'side': T(f'{name}_side'), 'top': T(f'{name}_top')},
              [box([1.5, 0, 1.5], [14.5, 16, 14.5], {'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'top', 'down': 'top'})],
              parent='minecraft:block/block')
        wjson(f'blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/{name}'}}}); block_item(name)
    model('block/gas_cylinder', {'particle': T('gas_cylinder_side'), 'side': T('gas_cylinder_side'), 'top': T('gas_cylinder_top'), 'steel': T('lab_steel')},
          [box([4.5, 0, 4.5], [11.5, 14, 11.5], {'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'top', 'down': 'top'}),
           box([5.25, 14, 5.25], [10.75, 15, 10.75], 'top'), box([6.75, 15, 6.75], [9.25, 16.5, 9.25], 'steel'),
           box([5.5, 16, 7.5], [10.5, 17, 8.5], 'steel')], parent='minecraft:block/block')
    wjson('blockstates/gas_cylinder.json', {'variants': {'': {'model': f'{NS}:block/gas_cylinder'}}}); block_item('gas_cylinder')

    # ---- Glassware shelf (wall) ----
    def bottle(x0, y0, z0, w_, h, col):
        return box([x0, y0, z0], [x0 + w_, y0 + h, z0 + w_], 'b', uvs={d: [col * 4, 16 - h, col * 4 + w_, 16] for d in ('north', 'south', 'east', 'west')}
                   | {'up': [col * 4, 0, col * 4 + w_, w_]})
    els = [box([0, 2, 8], [16, 3, 16], 'wood'), box([0, 9, 8], [16, 10, 16], 'wood'),
           box([0.5, 0, 15], [1.5, 15, 16], 'wood'), box([14.5, 0, 15], [15.5, 15, 16], 'wood'),
           bottle(1.5, 3, 10.5, 2.5, 5, 0), bottle(5, 3, 11, 2, 4, 1), bottle(8.5, 3, 10.5, 3, 3.5, 2), bottle(12.5, 3, 11, 2, 5.5, 0),
           bottle(2, 10, 10.5, 3, 4.5, 1), bottle(6.5, 10, 11, 2, 3, 3), bottle(10.5, 10, 10.5, 3, 4, 2)]
    model('block/glassware_shelf', {'particle': 'minecraft:block/spruce_planks', 'wood': 'minecraft:block/spruce_planks', 'b': T('lab_bottles')},
          els, parent='minecraft:block/block', render_type='minecraft:translucent')
    facing_states('glassware_shelf'); block_item('glassware_shelf')

    # ---- Plastic sheeting (pane) ----
    pt = {'pane': T('plastic_sheeting'), 'edge': T('plastic_sheeting_top')}
    for suffix, parent in (('post', 'template_glass_pane_post'), ('side', 'template_glass_pane_side'), ('side_alt', 'template_glass_pane_side_alt'),
                           ('noside', 'template_glass_pane_noside'), ('noside_alt', 'template_glass_pane_noside_alt')):
        model(f'block/plastic_sheeting_{suffix}', pt, parent=f'minecraft:block/{parent}', render_type='minecraft:translucent')
    P = lambda s_: f'{NS}:block/plastic_sheeting_{s_}'
    wjson('blockstates/plastic_sheeting.json', {'multipart': [
        {'apply': {'model': P('post')}},
        {'when': {'north': 'true'}, 'apply': {'model': P('side')}},
        {'when': {'east': 'true'}, 'apply': {'model': P('side'), 'y': 90}},
        {'when': {'south': 'true'}, 'apply': {'model': P('side_alt')}},
        {'when': {'west': 'true'}, 'apply': {'model': P('side_alt'), 'y': 90}},
        {'when': {'north': 'false'}, 'apply': {'model': P('noside')}},
        {'when': {'east': 'false'}, 'apply': {'model': P('noside_alt')}},
        {'when': {'south': 'false'}, 'apply': {'model': P('noside_alt'), 'y': 90}},
        {'when': {'west': 'false'}, 'apply': {'model': P('noside'), 'y': 270}}]})
    model('item/plastic_sheeting', {'layer0': T('plastic_sheeting')}, parent='minecraft:item/generated')

    # ---- Hazard sign (wall) ----
    model('block/hazard_sign', {'particle': T('hazard_sign'), 'front': T('hazard_sign'), 'back': T('sign_back')},
          [box([1, 1, 15], [15, 15, 16], {'north': 'front', 'south': 'back', 'east': 'back', 'west': 'back', 'up': 'back', 'down': 'back'})],
          parent='minecraft:block/block')
    facing_states('hazard_sign'); model('item/hazard_sign', {'layer0': T('hazard_sign')}, parent='minecraft:item/generated')

    # ---- Ceiling light ----
    model('block/lab_light', {'particle': T('lab_light'), 'light': T('lab_light'), 'steel': T('lab_steel')},
          [{'from': [1, 14, 3], 'to': [15, 16, 13], 'faces': {'down': face('light'), 'north': face('steel'), 'south': face('steel'),
                                                             'east': face('steel'), 'west': face('steel'), 'up': face('steel')},
            'shade': False}], parent='minecraft:block/block')
    wjson('blockstates/lab_light.json', {'variants': {'': {'model': f'{NS}:block/lab_light'}}}); block_item('lab_light')

if __name__ == '__main__':
    tex_dir = os.path.join(ROOT, 'textures')
    for sub in ('models', 'blockstates', 'textures', 'particles'):
        d = os.path.join(ROOT, sub)
        if os.path.isdir(d):
            shutil.rmtree(d)
    gen_cannabis(); gen_tobacco(); gen_poppy(); gen_coca(); gen_mushroom(); gen_wild()
    gen_machines(); gen_items(); gen_misc(); gen_shaders(); gen_lab()
    # Volumetric 3D models replace the simple plant models and add 3D held-item models
    import models3d
    models3d.run()
    # Seeds/spores place plants - their item models are normal items (generated in gen_items)
    print('assets ok')
