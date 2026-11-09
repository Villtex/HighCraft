"""Plant textures: cannabis, tobacco, poppy, coca, magic mushrooms."""
import math, random
from px import *

# --- Palettes ---
HEMP = dict(d=hexc('#1d4a12'), m=hexc('#2f7020'), l=hexc('#4c9a2e'), h=hexc('#73c04a'), v=hexc('#8fd666'))
HEMP_YOUNG = dict(d=hexc('#2c6418'), m=hexc('#43902a'), l=hexc('#62b43c'), h=hexc('#8ad65c'), v=hexc('#a8e67a'))
STEM = hexc('#3d6b22'); STEM_D = hexc('#2a4d17'); STEM_L = hexc('#5b8f33')
BUD = [hexc('#4f8a2a'), hexc('#6aa83a'), hexc('#88c24e'), hexc('#a7d46a')]
HAIR = hexc('#d9822b'); HAIR2 = hexc('#b8601e'); TRICH = hexc('#eef6dc'); PURP = hexc('#6e4a8a')

FAN_ASCII = [
    "........m.......",
    ".......lm.......",
    "..d....lmd....d.",
    "..ld...lmd...lm.",
    "...lm..lmd..lm..",
    "...lmd.lmd.lmd..",
    "d...lm.lmd.lm..m",
    "ldd.lmdlmdlmd.lm",
    ".llm.lmlmdlm.lmd",
    "..lllmdlmlmdlmd.",
    "....llmllmmlmd..",
    "..ddlllvvvmmmdd.",
    ".lllmmmmvmmmdddm",
    "........v.......",
    "........v.......",
    "........v.......",
]

def fan_leaf(pal=HEMP, n=7, size=7.5, cx=8, cy=13.5, spread=150, seed=0):
    """Cannabis fan leaf from above (tip up), 7 leaflets - pixel art."""
    cv = Canvas()
    cv.ascii(FAN_ASCII, {'d': pal['d'], 'm': pal['m'], 'l': pal['l'], 'v': pal['v'], 'h': pal['h']})
    return cv
    cv = Canvas()
    angles = [(-spread / 2 + spread * i / (n - 1)) for i in range(n)]
    order = sorted(range(n), key=lambda i: -abs(angles[i]))
    for i in order:
        a = angles[i]
        rel = 1 - abs(a) / (spread / 2)
        length = size * (0.45 + 0.55 * rel ** 0.7)
        width = 2.2 + 1.4 * rel
        leaflet(cv, cx, cy, a, length, width, pal['d'], pal['m'] if rel < 0.5 else pal['l'], pal['h'], vein=pal['v'] if rel > 0.3 else None)
    # petiole
    cv.line(8, 13, 8, 15, pal['d'])
    cv.edge_darken(0.75, hl=1.12)
    return cv

def bud_cluster(cv, cx, cy, rx, ry, seed=1, dried=False, purple=False):
    rnd = random.Random(seed)
    cols = BUD if not dried else [hexc('#6b6e2e'), hexc('#868a3c'), hexc('#a2a24e'), hexc('#bdb86a')]
    hair = HAIR if not dried else hexc('#a8582a')
    for y in range(cv.h):
        for x in range(cv.w):
            dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            r = dx * dx + dy * dy
            wob = 0.18 * math.sin(x * 2.1 + y * 1.3 + seed)
            if r <= 1 + wob:
                lvl = 3 - min(3, int((dx + 0.9) * 0.9 + (dy + 0.9) * 1.1 + rnd.random() * 1.2))
                c = cols[max(0, min(3, lvl))]
                if purple and rnd.random() < 0.25:
                    c = mix(c, PURP, 0.5)
                cv.set(x, y, c)
    # pistils & trichomes
    for _ in range(int(rx * ry * 0.9)):
        x = int(cx + (rnd.random() * 2 - 1) * rx * 0.85); y = int(cy + (rnd.random() * 2 - 1) * ry * 0.85)
        if cv.filled(x, y):
            r = rnd.random()
            cv.set(x, y, hair if r < 0.6 else HAIR2 if r < 0.85 else TRICH)

def bud_item(dried=False):
    cv = Canvas()
    bud_cluster(cv, 8, 8.5, 4.6, 6.2, seed=3, dried=dried)
    # sugar leaves
    pal = HEMP if not dried else dict(d=hexc('#4a5420'), m=hexc('#5f6a2a'), l=hexc('#77823a'), h=hexc('#8e9a48'), v=hexc('#a0aa58'))
    leaflet(cv, 7, 6, -60, 4.5, 2, pal['d'], pal['l'], pal['h'])
    leaflet(cv, 9, 9, 65, 4.5, 2, pal['d'], pal['l'], pal['h'])
    leaflet(cv, 8, 14, 200, 3, 2, pal['d'], pal['m'], pal['h'])
    cv.set(8, 15, STEM_D); cv.set(8, 14, STEM)
    cv.edge_darken(0.65, hl=1.1)
    cv.outline(shade(hexc('#1a2a0e'), 1.0) if not dried else hexc('#2a2a12'))
    return cv

# ---------------------------------------------------------------------------
# Side views of the plants (for crossed planes)
# ---------------------------------------------------------------------------

def side_leaf(cv, x, y, direction, length, pal, droop=0.4):
    """Fan leaf from the side: stalk + spread, narrow leaflets."""
    # petiole
    ex = x + direction * max(1, int(length * 0.35)); ey = y + int(droop * 2)
    cv.line(x, y, ex, ey, pal['d'])
    for a, lf, col in ((25, 0.75, pal['m']), (60, 1.0, pal['l']), (95, 0.9, pal['l']), (130, 0.6, pal['m'])):
        ang = direction * (a + droop * 25)
        leaflet(cv, ex + 0.5, ey + 0.5, ang, length * lf, 1.5, pal['d'], col, pal['h'], serrate=False)

def hemp_side(stage, upper=False, seed=0):
    """stage 0..7, lower/upper half as 16x16."""
    cv = Canvas()
    rnd = random.Random(seed + stage * 7 + (100 if upper else 0))
    pal = HEMP_YOUNG if stage <= 2 else HEMP
    if not upper:
        heights = [3, 5, 8, 12, 16, 16, 16, 16]
        H = heights[stage]
        # stem
        for y in range(16 - H, 16):
            cv.set(7, y, STEM_D); cv.set(8, y, STEM)
        if stage == 0:
            leaflet(cv, 8, 14, -60, 3, 2, pal['d'], pal['l'], pal['h'], serrate=False)
            leaflet(cv, 8, 14, 60, 3, 2, pal['d'], pal['l'], pal['h'], serrate=False)
        else:
            nodes = max(1, H // 5)
            for i in range(nodes):
                y = 15 - 3 - i * 5 + (1 if stage > 3 else 0)
                L = 3.0 + min(3.0, stage * 0.5) - i * 0.3
                d = -1 if i % 2 == 0 else 1          # alternate
                side_leaf(cv, 8, y, d, L, pal)
                side_leaf(cv, 8, y + 2, -d, L * 0.85, pal)
            if stage < 4:
                leaflet(cv, 8, 16 - H + 1, 0, 3, 2.5, pal['d'], pal['l'], pal['h'])
    else:
        # upper half from stage 4
        h_up = {4: 5, 5: 9, 6: 13, 7: 15}[stage]
        for y in range(16 - h_up, 16):
            cv.set(7, y, STEM_D); cv.set(8, y, STEM)
        nodes = max(1, h_up // 5)
        for i in range(nodes):
            y = 13 - i * 5
            L = 5.0 - i * 0.8
            d = 1 if i % 2 == 0 else -1
            side_leaf(cv, 8, y, d, L, pal, droop=0.2)
            side_leaf(cv, 8, y + 2, -d, L * 0.8, pal, droop=0.2)
        if stage >= 6:
            # buds (colas) on side branches + top
            bud_cluster(cv, 8, 16 - h_up + 3.2, 2.2, 3.6, seed=stage, purple=(stage == 7))
            bud_cluster(cv, 4, 11, 1.5, 2.2, seed=stage + 3)
            bud_cluster(cv, 12, 9, 1.5, 2.2, seed=stage + 5)
            if stage == 7:
                bud_cluster(cv, 3, 5, 1.3, 2.0, seed=11)
                bud_cluster(cv, 13, 4, 1.3, 2.0, seed=12)
        else:
            leaflet(cv, 8, 16 - h_up + 1, 0, 3, 2.5, pal['d'], pal['l'], pal['h'])
    cv.edge_darken(0.78, hl=1.08)
    return cv

def hemp_stem():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            c = STEM if x % 4 in (1, 2) else STEM_D
            if (y + x) % 7 == 0:
                c = STEM_L
            cv.set(x, y, c)
    return cv

def hemp_cola(dried=False):
    """Texture for the 3D buds (box elements)."""
    cv = Canvas()
    rnd = random.Random(5)
    cols = BUD
    for y in range(16):
        for x in range(16):
            c = rnd.choices(cols, weights=[3, 4, 3, 2])[0]
            cv.set(x, y, c)
    for _ in range(34):
        r = rnd.random()
        cv.set(rnd.randrange(16), rnd.randrange(16), HAIR if r < 0.55 else HAIR2 if r < 0.85 else TRICH)
    for _ in range(5):
        cv.set(rnd.randrange(16), rnd.randrange(16), mix(BUD[1], PURP, 0.5))
    return cv

# ---------------------------------------------------------------------------
TOB = dict(d=hexc('#3c6a1e'), m=hexc('#5a8f2c'), l=hexc('#78ad3e'), h=hexc('#9ccb5a'), v=hexc('#c8e08a'))

def tobacco_leaf_top(dried=False):
    """Large oval tobacco leaf (for tilted leaf planes and the item)."""
    cv = Canvas()
    pal = TOB if not dried else dict(d=hexc('#6b4a22'), m=hexc('#8a6230'), l=hexc('#a77a3e'), h=hexc('#c29652'), v=hexc('#d9b070'))
    leaflet(cv, 8, 15.5, 0, 15, 9.5, pal['d'], pal['l'], pal['h'], serrate=False, vein=pal['v'])
    # side veins
    for i in range(3, 13, 3):
        cv.line(8, 15 - i, 5, 13 - i, mix(pal['l'], pal['v'], 0.5))
        cv.line(8, 15 - i, 11, 13 - i, mix(pal['m'], pal['v'], 0.4))
    cv.edge_darken(0.75)
    return cv

def tobacco_side(stage):
    cv = Canvas()
    heights = [3, 5, 7, 9, 11, 13, 15, 16]
    H = heights[stage]
    for y in range(16 - H, 16):
        cv.set(8, y, hexc('#4f7a2a')); cv.set(7, y, hexc('#3c6a1e'))
    nodes = max(1, H // 3)
    for i in range(nodes):
        y = 15 - 2 - i * 3
        L = 7 - i * 0.9 + min(0, stage - 4) * 0.5
        if L < 2.5: L = 2.5
        a = 70 - i * 6
        leaflet(cv, 8, y, -a, L, 3.4, TOB['m'], TOB['l'], TOB['h'], serrate=False, vein=TOB['h'])
        leaflet(cv, 8, y - 1, a, L, 3.4, TOB['m'], TOB['l'], TOB['h'], serrate=False, vein=TOB['h'])
    if stage >= 6:
        pink, pink2 = hexc('#e889b4'), hexc('#c95b8f')
        for (x, y) in ((8, 0), (6, 1), (10, 1), (7, 2), (9, 1), (5, 3), (11, 2)):
            if stage == 7 or (x + y) % 2 == 0:
                cv.set(x, y, pink); cv.set(x, y + 1, pink2)
    cv.edge_darken(0.8, hl=1.06)
    return cv

def tobacco_flower():
    cv = Canvas()
    pink, pink2, white = hexc('#ef9cc2'), hexc('#c95b8f'), hexc('#fbe2ee')
    for cx, cy in ((5, 6), (10, 4), (8, 10), (12, 11), (4, 12)):
        for dx, dy, c in ((0, 0, white), (1, 0, pink), (-1, 0, pink), (0, 1, pink2), (0, -1, pink), (1, 1, pink2), (-1, -1, pink)):
            cv.set(cx + dx, cy + dy, c)
        cv.line(cx, cy + 2, cx, 15, hexc('#4f7a2a'))
    return cv

# ---------------------------------------------------------------------------
POP_STEM = hexc('#5f8a5e'); POP_STEM_D = hexc('#406a44'); POP_LEAF = dict(d=hexc('#2f5a3f'), m=hexc('#467a55'), l=hexc('#5f9a6c'), h=hexc('#86b890'), v=hexc('#a9cfae'))
RED = hexc('#d0262c'); RED_D = hexc('#8f1418'); RED_L = hexc('#f0544d'); BLACK = hexc('#1a1414')

def poppy_side(stage):
    cv = Canvas()
    heights = [3, 5, 8, 10, 12, 14, 15, 15]
    H = heights[stage]
    stalks = [(8, H)] if stage < 3 else [(6, H - 2), (8, H), (11, H - 3)]
    for sx, sh in stalks:
        for y in range(16 - sh, 16):
            cv.set(sx, y, POP_STEM if y % 3 else POP_STEM_D)
        # stem leaves
        for yy in range(16 - sh + 3, 12, 4):
            leaflet(cv, sx, yy, -45 if yy % 8 else 45, 3, 1.6, POP_LEAF['d'], POP_LEAF['l'], POP_LEAF['h'], serrate=True)
    # basal leaves (serrated, grey-green)
    for i in range(min(3, 1 + stage // 2)):
        y = 15 - i * 2
        leaflet(cv, 8, y, -70 + i * 10, 5 - i, 2.5, POP_LEAF['d'], POP_LEAF['l'], POP_LEAF['h'])
        leaflet(cv, 8, y, 70 - i * 10, 5 - i, 2.5, POP_LEAF['d'], POP_LEAF['m'], POP_LEAF['h'])
    if stage == 4:
        for sx, sh in stalks:  # buds (nodding)
            y = 16 - sh
            cv.set(sx, y, POP_LEAF['d']); cv.set(sx + 1, y, POP_LEAF['m']); cv.set(sx + 1, y + 1, POP_LEAF['l'])
    cv.edge_darken(0.8, hl=1.06)
    return cv

def poppy_petal():
    """Petals from above (for a horizontal plane) - red flower with a black blotch."""
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            lobe = 6.8 + 1.0 * math.cos(ang * 4)
            if r <= lobe:
                c = RED if r > 3 else BLACK
                if 3 <= r < 3.8: c = RED_D
                if r > lobe - 1.2: c = RED_L if dy < 0 else RED_D
                cv.set(x, y, c)
    cv.set(7, 7, hexc('#4a5a3a')); cv.set(8, 8, hexc('#4a5a3a')); cv.set(7, 8, hexc('#6a7a4a')); cv.set(8, 7, hexc('#6a7a4a'))
    return cv

def poppy_pod_tex():
    """Pod (3D box): bluish-green bloom, stigma disc on top."""
    cv = Canvas()
    rnd = random.Random(9)
    base = [hexc('#7f9c86'), hexc('#90ad96'), hexc('#a3bfa8'), hexc('#6c8a73')]
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choices(base, weights=[3, 4, 2, 2])[0])
    for x in range(0, 16, 4):
        for y in range(16):
            cv.set(x, y, hexc('#5f7d66'))
    return cv

def poppy_crown_tex():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            ang = math.atan2(dy, dx)
            c = hexc('#8a8f5a') if int((ang + math.pi) / (2 * math.pi) * 8) % 2 else hexc('#6c7244')
            cv.set(x, y, c)
    return cv

# ---------------------------------------------------------------------------
COCA = dict(d=hexc('#25541c'), m=hexc('#36752a'), l=hexc('#4f963a'), h=hexc('#6fb550'), v=hexc('#9ad07a'))
WOOD = hexc('#6b4a2e'); WOOD_D = hexc('#4a3220')

def coca_side(stage):
    cv = Canvas()
    sizes = [(4, 4), (7, 8), (10, 11), (13, 14), (14, 15)]
    w, h = sizes[stage]
    # branches
    cv.line(8, 15, 8, 16 - h + 3, WOOD)
    if stage >= 1:
        cv.line(8, 13, 4, 16 - h + 4, WOOD_D)
        cv.line(8, 12, 12, 16 - h + 5, WOOD)
    rnd = random.Random(stage * 13)
    # oval leaves
    count = [4, 9, 16, 24, 28][stage]
    for i in range(count):
        x = 8 + (rnd.random() * 2 - 1) * w / 2
        y = 16 - h + rnd.random() * (h - 3) + 1
        a = rnd.uniform(-80, 80) + (0 if rnd.random() < 0.5 else 180)
        leaflet(cv, x, y, a, rnd.uniform(2.5, 3.8), 2.2, COCA['d'], COCA['l'] if i % 2 else COCA['m'], COCA['h'], serrate=False)
    if stage == 4:
        for i in range(7):
            x = 8 + (rnd.random() * 2 - 1) * w / 2.3
            y = 16 - h + rnd.random() * (h - 5) + 2
            cv.set(int(x), int(y), hexc('#d32f2f')); cv.set(int(x) + 1, int(y), hexc('#9a1c1c'))
    cv.edge_darken(0.75, hl=1.08)
    return cv

def coca_leaves_item(dried=False):
    cv = Canvas()
    pal = COCA if not dried else dict(d=hexc('#4a5a26'), m=hexc('#62723a'), l=hexc('#7d8c4a'), h=hexc('#9aa65e'), v=hexc('#b5be7a'))
    leaflet(cv, 5, 15, 25, 12, 5.2, pal['d'], pal['l'], pal['h'], serrate=False, vein=pal['v'])
    leaflet(cv, 11, 15, -20, 11, 5.0, pal['d'], pal['m'], pal['h'], serrate=False, vein=pal['v'])
    cv.edge_darken(0.7)
    cv.outline(hexc('#13240e') if not dried else hexc('#2b3214'))
    return cv

# ---------------------------------------------------------------------------
CAP = [hexc('#8a5a2a'), hexc('#a8742f'), hexc('#c9933f'), hexc('#e2b35c')]

def mushroom_cap_tex():
    cv = Canvas()
    rnd = random.Random(4)
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy) / 8
            lvl = 3 - min(3, int(r * 3.2 + rnd.random() * 0.6))
            c = CAP[lvl]
            if rnd.random() < 0.06: c = hexc('#5f7fae')  # blue bruising
            cv.set(x, y, c)
    return cv

def mushroom_side_tex():
    """Cap side view: dark rim + hint of gills."""
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            c = CAP[2] if y < 6 else CAP[1] if y < 11 else CAP[0]
            if y >= 13: c = hexc('#d9cbb0')
            if (x * 7 + y * 3) % 11 == 0 and y < 12: c = hexc('#6b85b5')
            cv.set(x, y, c)
    return cv

def mushroom_stem_tex():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            c = hexc('#e8dcc4') if x % 3 else hexc('#cfbf9f')
            if y % 5 == 0 and x % 2: c = hexc('#9fb2d6')
            cv.set(x, y, c)
    return cv

def mushroom_gills_tex():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            ang = math.atan2(dy, dx)
            c = hexc('#4a3a30') if int((ang + math.pi) * 6) % 2 else hexc('#6b5646')
            cv.set(x, y, c)
    return cv

if __name__ == '__main__':
    items = [('fan', fan_leaf()), ('bud', bud_item()), ('bud_dry', bud_item(True)), ('cola', hemp_cola()), ('stem', hemp_stem())]
    for s in range(8):
        items.append((f'hemp{s}', hemp_side(s)))
    for s in range(4, 8):
        items.append((f'hemp_up{s}', hemp_side(s, True)))
    items += [('tob_leaf', tobacco_leaf_top()), ('tob_dry', tobacco_leaf_top(True)), ('tob_fl', tobacco_flower())]
    for s in range(8): items.append((f'tob{s}', tobacco_side(s)))
    for s in range(8): items.append((f'pop{s}', poppy_side(s)))
    items += [('petal', poppy_petal()), ('pod', poppy_pod_tex()), ('crown', poppy_crown_tex())]
    for s in range(5): items.append((f'coca{s}', coca_side(s)))
    items += [('coca_item', coca_leaves_item()), ('coca_dry', coca_leaves_item(True)), ('cap', mushroom_cap_tex()), ('capside', mushroom_side_tex()), ('mstem', mushroom_stem_tex()), ('gills', mushroom_gills_tex())]
    contact_sheet(items, scale=6, cols=10, out='/tmp/claude-0/-home-user/91e744b7-49a3-5ae1-9eec-1733811b2a18/scratchpad/plants.png')
