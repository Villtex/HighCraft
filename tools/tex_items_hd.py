"""HighCraft item icons, 32x32 (HD). Built on hd.py (SDF shapes + hue-shifted ramps)."""
import math, random
import numpy as np
from hd import *
import hd as _hd

# ------------------------------------------------------------------ materials
LEAF = Mat('#4f9a2e', shift=0.09)
LEAF_DRY = Mat('#8e9a44', shift=0.08)
BUD = Mat('#6aa83c', shift=0.10)
BUD_DRY = Mat('#83913f', shift=0.08)
PISTIL = hx('#e8892a'); PISTIL_D = hx('#b5541c'); PISTIL_DRY = hx('#a8582a')
TRICH = hx('#f4f8ea')
PAPER = Mat('#efe9dc', dark=0.55, light=0.9)
CARD = Mat('#d9c8a4', dark=0.6)
EMBER = Mat('#e8501a', shift=0.1, light=0.9)
ASH = Mat('#8f8a84', dark=0.6)
WOOD = Mat('#9a6034', shift=0.08)
WOOD_D = Mat('#6a3f22', shift=0.08)
STEEL = Mat('#a9b2bc', dark=0.7, light=0.85)
GLASS = Mat('#cfe6ee', dark=0.45, light=0.9)
BRASS = Mat('#c9a03a', shift=0.08)

def _t(x0, y0, x1, y1, t):
    return x0 + (x1 - x0) * t, y0 + (y1 - y0) * t

def smoke(ic, pts, col=(225, 225, 225, 150)):
    for x, y in pts:
        ic.px(x, y, col)

# ================================================================== cannabis
def cannabis_leaf(dry=False):
    ic = Icon()
    m = LEAF_DRY if dry else LEAF
    bx, by = 16, 26.5
    ic.shape(capsule(16, 26, 16.8, 31, 0.9), m, r=1, tone=-0.08)
    spec = [(-84, 9.0, 2.8), (-56, 13.5, 3.6), (-28, 17.0, 4.1), (0, 21.0, 4.6), (28, 17.0, 4.1), (56, 13.5, 3.6), (84, 9.0, 2.8)]
    for k in [0, 6, 1, 5, 2, 4, 3]:
        ang, ln, w = spec[k]
        f = leaf(bx, by, ang, ln, w, serr=0.38, teeth=9, power=1.25, tip=0.8, curve=0.10 * (1 if ang < 0 else -1))
        tone = -0.08 if abs(ang) > 70 else (-0.03 if abs(ang) > 40 else (0.0 if abs(ang) > 10 else 0.04))
        ic.shape(f, m, r=1.6, tone=tone)
        a = math.radians(ang)
        ax, ay = math.sin(a), -math.cos(a)
        for i in range(2, int(ln * 0.82)):
            ic.px(bx + ax * i, by + ay * i, m[5] if i < ln * 0.6 else m[4])
    ic.px(16, 26, m[3])
    return ic

def _bud_shape(scale=1.0, ox=0.0, oy=0.0):
    s = scale
    parts = [circle(16 + ox, 22 + oy, 7.2 * s), circle(14.5 + ox, 16 + oy, 6.4 * s), circle(17.5 + ox, 11 + oy, 5.0 * s),
             circle(16 + ox, 6.5 + oy, 3.3 * s), circle(10.5 + ox, 19 + oy, 3.6 * s), circle(21.5 + ox, 17 + oy, 3.8 * s),
             circle(20.5 + ox, 24.5 + oy, 3.6 * s), circle(11.5 + ox, 25 + oy, 3.4 * s)]
    return warp(smooth(1.6, *parts), 0.9, 2.5, 5)

def cannabis_bud(dry=False):
    ic = Icon()
    m = BUD_DRY if dry else BUD
    lm = LEAF_DRY if dry else LEAF
    # sugar leaves behind the bud
    for ang, ln, bxx in ((-70, 11, 13), (66, 12, 19), (-30, 10, 13), (35, 9, 19)):
        ic.shape(leaf(bxx, 23, ang, ln, 3.6, serr=0.35, teeth=6, power=1.2), lm, r=1.4, tone=-0.06)
    ic.shape(capsule(16, 26, 16.5, 31, 1.3), WOOD if dry else lm, r=1, tone=-0.1)
    ic.shape(_bud_shape(0.92), m, r=3.5, tone=-0.22)
    # calyx clusters, back (top) to front (bottom); global light from top-left
    rng = random.Random(11)
    blobs = []
    for cy in np.arange(5.0, 28, 2.9):
        half = 7.6 * math.sin(math.pi * min(1, (cy - 2.5) / 26)) ** 0.8 + 0.5
        for cx in np.arange(16 - half, 16 + half + 0.1, 3.3):
            blobs.append((cx + rng.uniform(-0.7, 0.7), cy + rng.uniform(-0.5, 0.5), rng.uniform(2.3, 3.0)))
    for cx, cy, r in blobs:
        g = 0.10 - 0.014 * (cx - 10) - 0.010 * (cy - 6)
        ic.shape(circle(cx, cy, r), m, r=r * 1.15, shadow=0.8, tone=g + rng.uniform(-0.03, 0.03))
    mask = ic.filled()
    pts = [(x, y) for y in range(3, 28) for x in range(N) if mask[y, x]]
    pist = PISTIL_DRY if dry else PISTIL
    for _ in range(11):
        x, y = rng.choice(pts)
        dx = rng.choice((-1, 1))
        ic.px(x, y, pist); ic.px(x + dx, y - 1, pist); ic.px(x + dx, y - 2, PISTIL_D)
    for _ in range(22 if dry else 12):
        x, y = rng.choice(pts)
        ic.px(x, y, TRICH)
    return ic

def joint(spliff=False):
    ic = Icon()
    x0, y0, x1, y1 = 4.5, 27.5, 26, 6
    # crutch (card filter)
    ic.shape(capsule(*_t(x0, y0, x1, y1, 0.0), *_t(x0, y0, x1, y1, 0.16), 1.6), CARD, r=1.6)
    paper = Mat('#ece2cc', dark=0.55, light=0.9) if spliff else PAPER
    body = capsule(*_t(x0, y0, x1, y1, 0.15), *_t(x0, y0, x1, y1, 0.86), 1.7, 3.3)
    tx = stripes(-45, 3.2, 0.3)
    ic.shape(body, paper, r=3.0, tex=tx, tex_amt=0.18)
    # green / brown showing through the paper
    rng = random.Random(3 if spliff else 2)
    mask = ic.mask_of(body)
    for _ in range(26):
        t = rng.uniform(0.35, 0.85)
        cx, cy = _t(x0, y0, x1, y1, t)
        x, y = int(cx + rng.uniform(-2, 2)), int(cy + rng.uniform(-2, 2))
        if 0 <= x < N and 0 <= y < N and mask[y, x]:
            col = (hx('#7a9a4a', 120) if rng.random() < 0.7 else hx('#8a6a3a', 110)) if not spliff else \
                  (hx('#8a6230', 120) if rng.random() < 0.6 else hx('#6a9a3e', 110))
            ic.px(x, y, col)
    # twisted tip + ember
    tip = capsule(*_t(x0, y0, x1, y1, 0.86), *_t(x0, y0, x1, y1, 0.93), 3.1, 2.4)
    ic.shape(tip, ASH, r=2.0, tex=grain(4), tex_amt=0.4)
    em = capsule(*_t(x0, y0, x1, y1, 0.93), *_t(x0, y0, x1, y1, 0.97), 2.2, 1.6)
    ic.shape(em, EMBER, r=1.5, tone=0.15, tex=grain(5), tex_amt=0.5)
    ex, ey = _t(x0, y0, x1, y1, 0.97)
    ic.px(ex, ey - 1, hx('#ffd27a'))
    smoke(ic, [(28, 3), (29, 2), (29, 1), (28, 0), (27, 4)])
    return ic

# ------------------------------------------------------------------ helpers
def steam(ic, x, y, h=6, phase=0.0):
    for i in range(h):
        ic.px(x + round(math.sin(i * 0.9 + phase) * 1.2), y - i, (245, 245, 245, max(60, 170 - i * 18)))

def diag_seg(x0, y0, x1, y1, t0, t1, r0, r1=None):
    ax, ay = _t(x0, y0, x1, y1, t0)
    bx, by = _t(x0, y0, x1, y1, t1)
    return capsule(ax, ay, bx, by, r0, r1)

def mound(cx, cy, w, h, seed, bump=1.0):
    base = inter(ellipse(cx, cy, w, h * 2), lambda x, y: y - (cy + 0.6))
    return warp(base, bump, 2.2, seed)

def scatter(ic, mask_fn, n, cols, seed, region=None):
    rng = random.Random(seed)
    m = ic.mask_of(mask_fn) if callable(mask_fn) else mask_fn
    pts = [(x, y) for y in range(N) for x in range(N) if m[y, x]]
    for _ in range(n):
        x, y = rng.choice(pts)
        ic.px(x, y, rng.choice(cols))

CERAMIC = Mat('#eeece6', dark=0.5)
PLASTIC = Mat('#dce8f2', dark=0.4, light=0.8)
CORK = Mat('#b5824e', shift=0.06)
DARKP = Mat('#2c2c34', dark=0.6, light=0.35)

# ================================================================== cannabis products
def cannabis_seeds():
    ic = Icon()
    m = Mat('#8a7452', shift=0.06, dark=0.72)
    seeds = ((9.5, 10.5, -35), (22.5, 11, 28), (15.5, 22.5, -5))
    for i, (cx, cy, ang) in enumerate(seeds):
        a = math.radians(ang)
        tipx, tipy = cx + math.sin(a) * 4.6, cy - math.cos(a) * 4.6
        f = smooth(2.2, ellipse(cx, cy, 4.6, 5.6, ang), circle(cx + math.sin(a) * 5.2, cy - math.cos(a) * 5.2, 1.3))
        mott = np.clip(noise(1.1, 40 + i) * 1.3 - 0.15, 0, 1)
        ic.shape(f, m, r=3.2, tex=mott, tex_amt=0.75, spec=0.35, gloss=12)
        for k in range(-2, 3):
            ic.px(cx + math.sin(a) * k * 1.4, cy - math.cos(a) * k * 1.4, m[1] if k < 2 else m[2])
        ic.px(cx - 1.5, cy - 1.5, m[6])
    return ic

def ground_cannabis():
    ic = Icon()
    pile = mound(16, 25.5, 13, 9.5, 4, 1.4)
    ic.shape(pile, BUD, r=5, tex=grain(8), tex_amt=0.65, tone=-0.04)
    scatter(ic, pile, 40, [LEAF[1], LEAF[2], BUD[5], hx('#8a7a3a')], 3)
    scatter(ic, pile, 10, [PISTIL, PISTIL_D], 5)
    scatter(ic, pile, 8, [TRICH], 6)
    for x, y, c in ((5, 29, BUD[2]), (27, 29, BUD[3]), (25, 28, PISTIL)):
        ic.px(x, y, c)
    return ic

def cannabis_resin():
    ic = Icon()
    parch = Mat('#e9dcb8', dark=0.5)
    ic.shape(rbox(16, 20, 12.5, 9.5, 0.6, ang=-8), parch, r=1.5, tex=stripes(-8, 1.3, 0.5), tex_amt=0.06)
    m = Mat('#c27a1c', shift=0.08, light=0.6)
    blob = warp(smooth(2.5, ellipse(15.5, 18.5, 9.5, 6.2), circle(19, 15, 4.5), circle(11, 16.5, 3.8)), 0.8, 2.6, 4)
    ic.shape(blob, m, r=5, spec=0.75, gloss=9, alpha=240, tex=noise(2.5, 9), tex_amt=0.2)
    ic.shape(capsule(23.5, 21, 26, 25.5, 1.6, 1.0), m, r=1.4, spec=0.5)
    ic.glint(12, 14, '#fff6d8', 2); ic.px(14, 13, '#fff6d8'); ic.px(20, 12, '#ffe8b0'); ic.px(9, 20, m[6])
    return ic

def hashish():
    ic = Icon()
    m = Mat('#6e4724', shift=0.07, dark=0.75)
    slab = sub(rbox(16, 17, 12.5, 7.2, 2.2, ang=-14), circle(28.5, 8.5, 3.6))
    ic.shape(slab, m, r=3.2, tex=grain(12), tex_amt=0.35)
    ic.shape(rbox(16, 17, 9.8, 4.6, 1.5, ang=-14), m, r=1.2, tone=-0.12, outline=False, tex=grain(13), tex_amt=0.3)
    # stamped leaf
    for ang in (-60, -28, 0, 28, 60):
        a = math.radians(ang - 14)
        for i in range(1, 5 if abs(ang) < 40 else 4):
            ic.px(16 + math.sin(a) * i, 19 - math.cos(a) * i, m[0])
            ic.px(16 + math.sin(a) * i + 1, 19 - math.cos(a) * i + 1, m[4])
    ic.px(16, 20, m[0]); ic.px(16, 21, m[0])
    ic.shape(rbox(27.5, 23.5, 2.2, 1.8, 0.8, ang=20), m, r=1.5, tex=grain(14), tex_amt=0.3)
    return ic

def hemp_fiber():
    ic = Icon()
    m = Mat('#cdb886', shift=0.06, dark=0.7)
    def w(y):
        return 4.0 + abs(y - 16) * 0.42
    bundle = poly([(16 - w(y), y) for y in range(2, 31)] + [(16 + w(y), y) for y in range(30, 1, -1)])
    ic.shape(warp(bundle, 0.6, 2, 3), m, r=3.5, tex=stripes(4, 1.7, 0.5), tex_amt=0.35)
    rng = random.Random(4)
    for _ in range(9):
        x = rng.randint(8, 23)
        ic.line(x + 0.5, 3, 16 + (x - 16) * 0.45 + 0.5, 13, m[rng.choice((2, 5))])
    twine = Mat('#8a5e34', shift=0.07)
    ic.shape(rbox(16, 16.5, 5.6, 2.0, 0.8), twine, r=1.4, tex=stripes(-60, 1.6, 0.5), tex_amt=0.4)
    ic.shape(capsule(20, 17.5, 24, 22, 0.8), twine, r=0.8)
    ic.shape(capsule(19.5, 17.5, 21.5, 23.5, 0.8), twine, r=0.8)
    return ic

def rolling_paper():
    ic = Icon()
    cover = Mat('#2f7d3e', shift=0.08)
    ic.shape(rbox(16, 9.5, 10.2, 4.6, 0.6, ang=-6), PAPER, r=1.2, tex=stripes(-6, 1.0, 0.5), tex_amt=0.06)
    ic.shape(rbox(16, 19.5, 11.5, 8.6, 1.6, ang=-6), cover, r=2.2, tex=noise(4, 3), tex_amt=0.1)
    ic.shape(rbox(16, 12.3, 11.2, 0.9, 0.3, ang=-6), Mat('#e2c158'), r=0.6, outline=False)
    for ang, ln in ((-60, 4), (-30, 5.5), (0, 6.5), (30, 5.5), (60, 4)):
        ic.shape(leaf(16.4, 24.0, ang - 6, ln, 2.0, power=1.2), Mat('#9cd35e'), r=0.8, outline=False)
    ic.px(16, 24, '#9cd35e'); ic.px(16, 25, '#7ab040')
    return ic

def space_cookie():
    ic = Icon()
    m = Mat('#c98b42', shift=0.08)
    bite = union(circle(28, 6, 5.2), circle(23.5, 3.2, 3.3), circle(29.5, 12, 3.0))
    cookie = sub(warp(circle(16, 17, 12.4), 0.6, 3, 2), bite)
    ic.shape(cookie, m, r=4, tex=grain(3), tex_amt=0.28)
    choc = Mat('#4e2c18', dark=0.6)
    for cx, cy, r in ((10, 12, 1.8), (17, 9.5, 1.5), (21, 16, 1.9), (12, 21, 2.0), (19, 23.5, 1.6), (25, 21, 1.4), (7.5, 18, 1.3)):
        ic.shape(warp(circle(cx, cy, r), 0.4, 1.5, int(cx * 7)), choc, r=1.4, spec=0.3, shadow=0.75)
    scatter(ic, cookie, 14, [LEAF[2], LEAF[4], BUD[3]], 9)
    for x, y in ((27, 14), (29, 16), (26, 16)):
        ic.px(x, y, m[3])
    return ic

# ================================================================== tobacco
TOB = Mat('#6aa244', shift=0.09)
TOB_DRY = Mat('#b37a3a', shift=0.08)

def tobacco_seeds():
    ic = Icon()
    m = Mat('#704426', shift=0.06)
    rng = random.Random(5)
    pts = [(10, 22), (14, 20), (18, 21), (22, 23), (12, 25), (16, 24), (20, 26), (24, 27), (8, 27), (14, 28), (19, 29), (11, 18), (17, 17), (23, 19), (26, 25), (15, 13)]
    for i, (x, y) in enumerate(pts):
        ic.shape(ellipse(x, y, 1.7, 2.3, rng.uniform(-40, 40)), m, r=1.6, spec=0.3, tex=grain(i), tex_amt=0.3)
    return ic

def tobacco_leaf(dry=False):
    ic = Icon()
    m = TOB_DRY if dry else TOB
    f = leaf(6, 27, 42, 32, 15.5, power=0.95, tip=0.85, curve=-0.12)
    tex = noise(1.6, 31) if dry else noise(3, 30)
    ic.shape(warp(f, 0.7 if dry else 0.3, 2, 8), m, r=3.2, tex=tex, tex_amt=0.45 if dry else 0.15)
    a = math.radians(42); ax, ay = math.sin(a), -math.cos(a)
    vein = m[1] if dry else m[5]
    for i in range(0, 25):
        ic.px(6 + ax * i, 27 + ay * i, vein)
    for i in range(3, 21, 4):
        bx, by = 6 + ax * i, 27 + ay * i
        for k in range(1, 6):
            ic.px(bx + ax * k * 0.5 - ay * k * 0.9, by + ay * k * 0.5 + ax * k * 0.9, m[4] if not dry else m[2])
            ic.px(bx + ax * k * 0.5 + ay * k * 0.9, by + ay * k * 0.5 - ax * k * 0.9, m[4] if not dry else m[2])
    ic.shape(capsule(3, 30, 6.5, 26.5, 1.0), m, r=1, tone=-0.1)
    return ic

def cut_tobacco():
    ic = Icon()
    pile = mound(16, 25.5, 13.2, 10, 7, 1.8)
    ic.shape(pile, TOB_DRY, r=5, tex=noise(1.2, 4), tex_amt=0.55)
    rng = random.Random(6)
    m = ic.mask_of(grow(pile, -1))
    pts = [(x, y) for y in range(N) for x in range(N) if m[y, x]]
    for _ in range(26):
        x, y = rng.choice(pts)
        dx, dy = rng.choice(((1, 0), (1, 1), (1, -1), (0, 1)))
        c = rng.choice([TOB_DRY[1], TOB_DRY[2], TOB_DRY[5], hx('#e0a858')])
        for k in range(rng.randint(2, 4)):
            ic.px(x + dx * k, y + dy * k, c)
    for x0, y0, x1, y1 in ((3, 29, 7, 28), (25, 29, 29, 30), (27, 26, 30, 27)):
        ic.line(x0, y0, x1, y1, TOB_DRY[3])
    return ic

def cigarette():
    ic = Icon()
    x0, y0, x1, y1 = 5, 27.5, 26.5, 6
    filt = Mat('#d98a3a', shift=0.08)
    ic.shape(diag_seg(x0, y0, x1, y1, 0, 0.33, 1.9), filt, r=1.9, tex=grain(2), tex_amt=0.35)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.31, 0.91, 1.9), PAPER, r=1.9)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.32, 0.345, 1.95), Mat('#d4b24a'), r=1.4, outline=False)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.9, 0.96, 1.9), ASH, r=1.8, tex=grain(7), tex_amt=0.5)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.96, 0.99, 1.6), EMBER, r=1.4, tone=0.15, tex=grain(8), tex_amt=0.5)
    smoke(ic, [(28, 3), (29, 2), (29, 1), (28, 0)])
    return ic

def cigar():
    ic = Icon()
    x0, y0, x1, y1 = 4.5, 27.5, 26, 6
    m = Mat('#7c4a26', shift=0.08)
    ic.shape(diag_seg(x0, y0, x1, y1, 0, 0.9, 2.9, 3.1), m, r=3.2, tex=np.clip(stripes(-15, 2.4, 0.3) + noise(2, 3) * 0.5, 0, 1), tex_amt=0.3)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.17, 0.27, 3.25), Mat('#b42a2a', shift=0.08), r=2.6, spec=0.2)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.205, 0.235, 3.35), Mat('#e7c252'), r=2.4, outline=False, spec=0.3)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.88, 0.96, 3.0), ASH, r=2.4, tex=grain(4), tex_amt=0.55)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.955, 0.985, 2.6), EMBER, r=2, tone=0.15, tex=grain(9), tex_amt=0.5)
    smoke(ic, [(29, 4), (30, 3), (30, 2), (29, 1), (28, 0)])
    return ic

# ================================================================== coca
COCA = Mat('#4e8c2e', shift=0.09)
COCA_DRY = Mat('#8c9a5c', shift=0.07, dark=0.75)

def coca_seeds():
    ic = Icon()
    twig = Mat('#6e4a2c')
    ic.shape(capsule(4, 27, 14, 15, 1.0), twig, r=1)
    ic.shape(capsule(14, 15, 26, 9, 0.9), twig, r=1)
    ic.shape(capsule(14, 15, 17, 5, 0.8), twig, r=1)
    ic.shape(leaf(9, 21, -55, 9, 4.2), COCA, r=1.5)
    berry = Mat('#cf2f2a', shift=0.08, light=0.6)
    for i, (cx, cy) in enumerate(((22, 14), (17, 9), (13, 20), (25, 21), (19, 20.5), (9, 13))):
        ic.shape(ellipse(cx, cy, 3.0, 3.7, 20), berry, r=2.6, spec=0.45, gloss=12, tone=-0.02 * i)
        ic.px(cx - 1.2, cy - 1.8, '#ffe0d8')
    return ic

def coca_leaves(dry=False):
    ic = Icon()
    m = COCA_DRY if dry else COCA
    for k, (ang, ln, w) in enumerate(((-38, 20, 9.5), (36, 19, 9.0), (0, 24, 10.5))):
        f = leaf(16, 29, ang, ln, w, power=1.0, tip=0.75, curve=0.04)
        ic.shape(warp(f, 0.5, 2, k) if dry else f, m, r=3, tone=(-0.06 if k < 2 else 0.04), tex=noise(1.8, k) if dry else None, tex_amt=0.3)
        a = math.radians(ang); ax, ay = math.sin(a), -math.cos(a); px_, py_ = -ay, ax
        for i in range(2, int(ln * 0.85)):
            ic.px(16 + ax * i, 29 + ay * i, m[5] if not dry else m[1])
            u = i / ln
            off = w * 0.22 * math.sin(math.pi * u)
            if 0.15 < u < 0.8:
                ic.px(16 + ax * i + px_ * off, 29 + ay * i + py_ * off, m[4] if not dry else m[2])
                ic.px(16 + ax * i - px_ * off, 29 + ay * i - py_ * off, m[3] if not dry else m[2])
    ic.shape(capsule(16, 28, 16, 31, 1.0), m, r=1, tone=-0.1)
    return ic

def coca_paste():
    ic = Icon()
    ic.shape(ellipse(16, 24, 13.5, 5.2), CERAMIC, r=3)
    ic.shape(ellipse(16, 23.2, 10.5, 3.4), CERAMIC, r=2, tone=-0.18, outline=False)
    paste = Mat('#d6c38e', shift=0.07)
    blob = warp(smooth(2, ellipse(15, 19.5, 8.5, 5), ellipse(19, 16, 4.5, 3.6), ellipse(11.5, 16.5, 3.5, 2.8)), 0.9, 2.2, 6)
    ic.shape(blob, paste, r=4, tex=noise(2, 7), tex_amt=0.35, spec=0.15)
    scatter(ic, blob, 9, [paste[2], hx('#b39a5c')], 4)
    return ic

def cocaine():
    ic = Icon()
    bag = rbox(16, 18.5, 10.5, 11.5, 2)
    ic.shape(bag, PLASTIC, r=2.5, alpha=110, out_alpha=255, amb=0.5)
    powder = inter(grow(bag, -1.5), warp(lambda x, y: 14.5 - y, 1.2, 2.5, 3))
    ic.shape(powder, Mat('#f2f2f6', dark=0.35, light=0.9), r=3, tex=grain(4), tex_amt=0.25, outline=False)
    ic.shape(rbox(16, 8.4, 10.5, 1.1, 0.4), Mat('#d23a34'), r=0.8, outline=False)
    ic.shape(rbox(16, 10.2, 10.5, 0.6, 0.2), Mat('#c8d6e2'), r=0.5, outline=False)
    ic.line(7.5, 12, 7.5, 26, (255, 255, 255, 200))
    ic.line(8.5, 11.5, 9.5, 11.5, (255, 255, 255, 200))
    for x, y in ((23, 24), (21, 27), (11, 26)):
        ic.px(x, y, hx('#c9ccd8'))
    return ic

def cup(tea, decor, extra=None, phase=0.0):
    ic = Icon()
    ic.shape(ellipse(16, 27.5, 13.5, 3.0), CERAMIC, r=2)
    ic.shape(ellipse(16, 27.0, 9.0, 1.6), CERAMIC, r=1, tone=-0.15, outline=False)
    ic.shape(sub(circle(25.2, 18.5, 4.6), circle(25.2, 18.5, 2.3)), CERAMIC, r=1.6)
    body = smooth(1.2, inter(rbox(16, 18, 10, 9, 4), lambda x, y: 11.5 - y), ellipse(16, 12, 10, 2.4))
    ic.shape(body, CERAMIC, r=4)
    ic.shape(inter(rbox(16, 20.5, 10, 1.4), body), decor, r=1, outline=False)
    ic.shape(ellipse(16, 12.2, 8.3, 1.6), tea, r=1.5, outline=False, tone=0.05)
    if extra:
        extra(ic)
    steam(ic, 12, 9, 7, phase); steam(ic, 18, 8, 6, phase + 2)
    return ic

def coca_tea():
    def leafy(ic):
        ic.shape(leaf(13, 13, 70, 7, 3.2), COCA, r=1.2)
    return cup(Mat('#8ca83a', shift=0.08), COCA, leafy)

def detox_tea():
    def lemon(ic):
        ic.shape(circle(21.5, 11.5, 3.6), Mat('#f2d23a'), r=2)
        ic.shape(circle(21.5, 11.5, 2.4), Mat('#fff3a0'), r=1, outline=False)
        for dx, dy in ((0, -1), (1, 0), (0, 1), (-1, 0)):
            ic.px(21.5 + dx * 1.5, 11.5 + dy * 1.5, '#e8c030')
    return cup(Mat('#e2c25a', shift=0.08), Mat('#e8c84a'), lemon, 1.0)

# ================================================================== poppy
POD = Mat('#94b49c', shift=0.06, dark=0.75)
def poppy_pod():
    ic = Icon()
    ic.shape(capsule(16, 24, 16.5, 31, 1.5), Mat('#6f9a5a'), r=1.2)
    body = ellipse(16, 17.5, 9.4, 8.8)
    ic.shape(body, POD, r=6, tex=np.cos((X - 16) / 9.4 * 7.5) * 0.5 + 0.5, tex_amt=0.12, spec=0.15)
    crown = Mat('#a998b2', shift=0.06)
    ic.shape(ellipse(16, 9.0, 6.6, 1.8), crown, r=1.4)
    for i, xx in enumerate(range(11, 22, 2)):
        ic.px(xx, 8, crown[5]); ic.px(xx + 0.4, 7, crown[3])
    ic.shape(ellipse(16, 7.2, 1.6, 1.0), crown, r=1)
    # dried latex tears
    lat = hx('#f2ead2')
    for x, y0, n in ((12, 14, 4), (19, 15, 5), (22, 18, 3)):
        for k in range(n):
            ic.px(x, y0 + k, lat)
        ic.px(x, y0 + n, hx('#d8b860'))
    return ic

def poppy_seeds():
    ic = Icon()
    m = Mat('#5f6c86', shift=0.05, dark=0.7)
    bowl = inter(ellipse(16, 18, 13.5, 10.5), lambda x, y: 18.5 - y)
    ic.shape(bowl, WOOD, r=3.5, tex=stripes(-5, 2.3, 0.4), tex_amt=0.15, spec=0.2)
    ic.shape(ellipse(16, 18.2, 13.5, 3.4), WOOD_D, r=1.5)
    pile = warp(inter(ellipse(16, 18.6, 12.2, 8.0), lambda x, y: y - 20.5), 0.9, 1.8, 3)
    ic.shape(pile, m, r=4, tex=grain(10), tex_amt=0.75, outline=False)
    ic.shape(ellipse(16, 30, 7, 1.2), WOOD_D, r=0.8)
    for x, y in ((26, 30), (28, 29), (5, 30)):
        ic.px(x, y, m[3])
    return ic

def raw_opium():
    ic = Icon()
    lm = Mat('#7aa86a', shift=0.07)
    ic.shape(leaf(3, 25, 72, 28, 13, power=1.0, curve=0.1), lm, r=2.5, tone=-0.04)
    for i in range(3, 25):
        ic.px(3 + math.sin(math.radians(72)) * i, 25 - math.cos(math.radians(72)) * i, lm[5])
    m = Mat('#6a3c1c', shift=0.07, dark=0.75)
    blob = warp(smooth(2.5, ellipse(16, 19, 9, 6.0), circle(13, 14.5, 4.2), circle(19.5, 14.5, 3.8)), 0.9, 2.5, 4)
    ic.shape(blob, m, r=5, spec=0.65, gloss=9, tex=noise(2.5, 4), tex_amt=0.15)
    ic.shape(capsule(21, 23, 22, 27, 1.2, 0.7), m, r=1.2, spec=0.5)
    ic.glint(12, 12, '#f0d0a8', 2); ic.px(19, 12, '#d8a878')
    return ic

def opium():
    ic = Icon()
    ball = Mat('#432614', shift=0.07, dark=0.7, light=0.55)
    ic.shape(circle(16, 15, 9.5), ball, r=6, spec=0.5, gloss=10, tex=noise(2, 2), tex_amt=0.12)
    petal = Mat('#c62f2c', shift=0.09)
    wrap = inter(circle(16, 15.2, 10.6), warp(lambda x, y: 16.5 - y + np.sin(x * 0.8) * 1.2, 0.6, 2, 3))
    ic.shape(wrap, petal, r=4, tex=stripes(80, 2.2, 0.5), tex_amt=0.15, shadow=0.7)
    for cx, ang in ((8, -40), (24, 40)):
        ic.shape(leaf(cx, 20, ang, 6, 4.5), petal, r=1.5, tone=-0.05)
    ic.shape(rbox(16, 26.6, 4.5, 1.2, 0.5), Mat('#d8c070'), r=0.8)
    ic.glint(12, 10, '#e8c8a0', 2)
    return ic

def morphine():
    ic = Icon()
    vial = rbox(16, 19, 6.8, 10.5, 2.4)
    ic.shape(vial, GLASS, r=2.5, alpha=140, out_alpha=255, amb=0.45)
    liq = inter(grow(vial, -1.2), lambda x, y: 13.5 - y)
    ic.shape(liq, Mat('#ecd77a', light=0.7), r=2, alpha=200, outline=False)
    ic.shape(rbox(16, 21, 6.8, 4.3, 0.4), Mat('#f6f6f2', dark=0.45), r=1.2, outline=False)
    red = hx('#d23a34')
    for y in (19, 20, 21, 22, 23):
        ic.px(16, y, red)
    for x in (14, 15, 17, 18):
        ic.px(x, 21, red)
    ic.shape(rbox(16, 7.6, 6.0, 2.1, 0.6), STEEL, r=1.3)
    ic.shape(rbox(16, 4.6, 4.2, 1.4, 0.6), Mat('#d04a3a'), r=1)
    ic.line(11.5, 10.5, 11.5, 16.5, (255, 255, 255, 210)); ic.line(11.5, 25.5, 11.5, 27, (255, 255, 255, 180))
    return ic

def syringe(filled, incomplete=False):
    ic = Icon()
    x0, y0, x1, y1 = 3.5, 28.5, 29, 3
    ghost = 90 if incomplete else None
    def seg(t0, t1, rad, mat, **kw):
        if incomplete and kw.pop('part', None) == 'ghost':
            return ic.shape(diag_seg(x0, y0, x1, y1, t0, t1, rad), mat, alpha=ghost, out_alpha=110, shadow=0, **kw)
        kw.pop('part', None)
        return ic.shape(diag_seg(x0, y0, x1, y1, t0, t1, rad), mat, **kw)
    seg(0.07, 0.36, 0.9, STEEL, r=1, part='ghost')
    ax, ay = _t(x0, y0, x1, y1, 0.04)
    if incomplete:
        ic.shape(rbox(ax, ay, 3.4, 1.0, 0.5, ang=45), DARKP, r=1, alpha=ghost, out_alpha=110, shadow=0)
    else:
        ic.shape(rbox(ax, ay, 3.4, 1.0, 0.5, ang=45), DARKP, r=1)
    fx, fy = _t(x0, y0, x1, y1, 0.34)
    ic.shape(rbox(fx, fy, 4.4, 0.9, 0.4, ang=45), DARKP, r=1)
    barrel = diag_seg(x0, y0, x1, y1, 0.35, 0.76, 2.6)
    ic.shape(barrel, GLASS, r=2.4, alpha=150, out_alpha=255, amb=0.5)
    if not incomplete:
        seg(0.39, 0.43, 2.0, DARKP, r=1.2, outline=False)
    if filled:
        ic.shape(diag_seg(x0, y0, x1, y1, 0.44, 0.745, 1.9), Mat('#e8c860', light=0.7), r=1.6, alpha=210, outline=False)
    for t in np.arange(0.47, 0.75, 0.055):
        cx, cy = _t(x0, y0, x1, y1, t)
        ic.px(cx - 1.6, cy - 1.6, (60, 70, 80, 200))
    seg(0.76, 0.81, 1.3, STEEL, r=1, part='ghost')
    a = _t(x0, y0, x1, y1, 0.81); b = _t(x0, y0, x1, y1, 0.99)
    if incomplete:
        ic.line(a[0], a[1], b[0], b[1], (220, 226, 232, 110))
    else:
        ic.line(a[0], a[1], b[0], b[1], STEEL[5])
        ic.px(b[0] - 0.5, b[1] + 0.5, STEEL[2])
    ic.line(*_t(x0, y0, x1, y1, 0.42), *_t(x0, y0, x1, y1, 0.7), (255, 255, 255, 120))
    return ic

def scoring_knife():
    ic = Icon()
    x0, y0, x1, y1 = 4, 28, 28, 4
    ic.shape(diag_seg(x0, y0, x1, y1, 0.0, 0.42, 2.2, 2.6), WOOD, r=2.2, tex=stripes(45, 2.0, 0.4), tex_amt=0.2)
    ic.shape(diag_seg(x0, y0, x1, y1, 0.41, 0.48, 2.8), BRASS, r=2, spec=0.4)
    bx, by = _t(x0, y0, x1, y1, 0.48)
    blade = poly([(bx - 2.2, by + 0.2), (bx + 0.2, by - 2.2), (26.5, 3.5), (28.5, 2.5), (25.5, 6.5)])
    ic.shape(blade, STEEL, r=1.5, spec=0.5, gloss=8)
    for k in (0.55, 0.65, 0.75):
        cx, cy = _t(bx, by, 27, 4, k)
        ic.px(cx + 1, cy + 1, STEEL[1])
    ic.line(bx + 0.5, by - 1.2, 25.5, 4.8, STEEL[6])
    ic.px(*_t(x0, y0, x1, y1, 0.12), WOOD[2]); ic.px(*_t(x0, y0, x1, y1, 0.25), WOOD[2])
    return ic

# ================================================================== mushrooms
CAP = Mat('#b9782f', shift=0.09)
CAP_DRY = Mat('#8a6236', shift=0.07, dark=0.75)
STEM = Mat('#e8e0cc', dark=0.55)
STEM_DRY = Mat('#c8b48e', dark=0.6)
BLUE = hx('#5d7fc0')

def _shroom(ic, cx, top, w, h, stem_len, lean, m_cap, m_stem, dry, seed):
    sx0, sy0 = cx + lean, top + h + stem_len
    ic.shape(warp(capsule(sx0, sy0, cx, top + h * 0.7, w * 0.22, w * 0.17), 0.6 if dry else 0.25, 2, seed), m_stem, r=2, tex=stripes(0, 1.5, 0.5) if dry else None, tex_amt=0.15)
    ic.px(sx0, sy0, BLUE); ic.px(sx0 + 1, sy0 - 1, BLUE); ic.px(sx0 - 1, sy0, darker(BLUE, 0.8))
    capf = smooth(1.2, inter(ellipse(cx, top + h, w, h), lambda x, y: y - (top + h + 0.6)), circle(cx, top + h * 0.18, w * 0.22))
    if dry:
        capf = warp(capf, 1.0, 1.8, seed + 1)
    ic.shape(ellipse(cx, top + h + 0.6, w * 0.95, 1.4), Mat('#5a3c24'), r=1, tone=-0.05)
    rad = np.clip(np.hypot(X - cx, (Y - top) * 1.4) / (w * 1.1), 0, 1)
    ic.shape(capf, m_cap, r=w * 0.55, spec=0.35 if not dry else 0.05, gloss=10, tex=rad, tex_amt=0.35 if not dry else 0.2)
    ic.glint(cx - w * 0.35, top + h * 0.45, '#f6e2b8' if not dry else '#c8a880', 1)

def magic_mushroom(dry=False):
    ic = Icon()
    mc, ms = (CAP_DRY, STEM_DRY) if dry else (CAP, STEM)
    _shroom(ic, 22.5, 12 if not dry else 14, 5.6 if not dry else 5.0, 4.2 if not dry else 3.6, 10, 1.0, mc, ms, dry, 3)
    _shroom(ic, 12.5, 3 if not dry else 5, 9.2 if not dry else 8.0, 7.2 if not dry else 6.0, 16 if not dry else 15, -0.5 if not dry else 1.5, mc, ms, dry, 7)
    return ic

def mushroom_spores():
    ic = Icon()
    sack = Mat('#b89562', shift=0.07)
    weave = np.clip(stripes(0, 1.6, 0.5) * 0.5 + stripes(90, 1.6, 0.5) * 0.5, 0, 1)
    body = smooth(2.5, ellipse(16, 22, 10.5, 8.5), rbox(16, 12.5, 4.2, 3.0, 1))
    ic.shape(body, sack, r=4.5, tex=weave, tex_amt=0.18)
    top = warp(poly([(9, 9), (23, 9), (21, 3.5), (18, 5.5), (16, 2.5), (14, 5.5), (11, 3.5)]), 0.5, 2, 2)
    ic.shape(top, sack, r=2, tex=weave, tex_amt=0.18, tone=0.03)
    ic.shape(rbox(16, 10.0, 5.6, 1.1, 0.5), Mat('#7a4e2c'), r=0.9, tex=stripes(-60, 1.4, 0.5), tex_amt=0.3)
    ic.shape(capsule(21, 10.5, 23.5, 14.5, 0.7), Mat('#7a4e2c'), r=0.8)
    spore = Mat('#5a3a6a', shift=0.05)
    ic.shape(circle(16, 22.5, 4.4), Mat('#e8e2d4', dark=0.5), r=1.6, outline=False)
    for k in range(10):
        a = k / 10 * math.tau
        for rr in (1.2, 2.4, 3.4):
            ic.px(16 + math.cos(a) * rr, 22.5 + math.sin(a) * rr, spore[2] if rr < 3 else spore[3])
    ic.px(16, 22, spore[1])
    for x, y in ((25, 29), (27, 30), (28, 28)):
        ic.px(x, y, spore[3])
    return ic

def mushroom_tea():
    ic = Icon()
    flask = smooth(1.2, circle(16, 21, 8.8), rbox(16, 9.5, 2.6, 5))
    ic.shape(flask, GLASS, r=2, alpha=130, out_alpha=255, amb=0.45)
    liq = inter(grow(circle(16, 21, 8.8), -1.2), lambda x, y: 17.2 - y)
    ic.shape(liq, Mat('#7b4a86', shift=0.08, light=0.6), r=3, alpha=230, outline=False, tex=noise(2, 5), tex_amt=0.2)
    ic.shape(ellipse(16, 17.4, 7.0, 0.9), Mat('#b582c0'), r=0.5, outline=False, flat=True)
    for x, y, c in ((12, 22, CAP[4]), (19, 24, CAP[3]), (15, 26, STEM[3]), (20, 20, '#d8b8e0'), (11, 25, '#d8b8e0')):
        ic.px(x, y, c)
    ic.shape(rbox(16, 4.4, 3.2, 2.2, 0.8), CORK, r=1.4, tex=grain(3), tex_amt=0.3)
    ic.line(11.5, 16.5, 9.5, 21, (255, 255, 255, 190)); ic.line(14.2, 6.5, 14.2, 11, (255, 255, 255, 170))
    return ic

# ================================================================== lsd
def ergot():
    ic = Icon()
    straw = Mat('#d8b85e', shift=0.07)
    ic.shape(capsule(4, 30, 12.5, 19.5, 1.0), straw, r=1)
    ic.shape(leaf(7, 27, 35, 11, 3.6, curve=0.2), Mat('#c4a24e'), r=1.2, tone=-0.08)
    gm = Mat('#dbb24e', shift=0.08)
    sk = Mat('#45285a', shift=0.05, dark=0.72, light=0.5)
    x0, y0, x1, y1 = 12.5, 19.5, 24.5, 4.5
    ax, ay = (x1 - x0), (y1 - y0); ln = math.hypot(ax, ay); ax, ay = ax / ln, ay / ln
    px_, py_ = -ay, ax
    for i in range(6):
        t = i / 5.6
        cx, cy = _t(x0, y0, x1, y1, t)
        for side in (-1, 1):
            w = 2.1 * (1 - t * 0.35)
            gx, gy = cx + px_ * side * w, cy + py_ * side * w
            ic.shape(ellipse(gx, gy, 1.9, 2.7, 38 + side * 22), gm, r=1.6, tone=0.03 * side)
            ic.line(gx + px_ * side * 1.8, gy + py_ * side * 1.8 - 1, gx + px_ * side * 4.5 + ax * 2, gy + py_ * side * 4.5 + ay * 2 - 1, gm[5])
    for t, side, ln_ in ((0.2, 1, 7.5), (0.5, -1, 7.0), (0.78, 1, 5.5)):
        cx, cy = _t(x0, y0, x1, y1, t)
        sx, sy = cx + px_ * side * 1.2, cy + py_ * side * 1.2
        ex, ey = sx + px_ * side * ln_ * 0.75 + ax * ln_ * 0.55, sy + py_ * side * ln_ * 0.75 + ay * ln_ * 0.55
        ic.shape(capsule(sx, sy, ex, ey, 1.9, 0.8), sk, r=1.6, spec=0.45, gloss=10)
    return ic

def lsd_art(w=N, h=N, cx=16, cy=16, seed=0):
    """Psychedelic swirl as RGBA array (quantised rainbow)."""
    yy, xx = np.mgrid[0:h, 0:w] + 0.5
    ang = np.arctan2(yy - cy, xx - cx)
    rad = np.hypot(xx - cx, yy - cy)
    hue = (ang / math.tau + rad / 9.0) % 1.0
    hue = np.floor(hue * 8) / 8
    out = np.zeros((h, w, 4))
    import colorsys as cs
    for y in range(h):
        for x in range(w):
            r, g, b = cs.hsv_to_rgb(hue[y, x], 0.62, 0.98)
            out[y, x] = (r * 255, g * 255, b * 255, 255)
    eye = rad < 2.6
    out[eye] = (40, 30, 60, 255)
    out[(rad >= 2.6) & (rad < 3.6)] = (250, 250, 245, 255)
    return out

def lsd_blotter(half=False):
    ic = Icon()
    sheetf = rbox(16, 16, 13.5, 13.5, 0.5)
    ic.shape(sheetf, PAPER, r=1.4)
    m = ic.mask_of(grow(sheetf, -0.6))
    art = lsd_art()
    if half:
        m &= X < 16
    ic.a = np.where(m[:, :, None], art, ic.a)
    perf = ((np.floor(X) - 3) % 6.5 < 1) | ((np.floor(Y) - 3) % 6.5 < 1)
    dots = perf & m & (((np.floor(X) + np.floor(Y)) % 2) == 0)
    ic.tint(dots, 0.72)
    ic.tint(m & (X < 4) & ~perf, 1.12)
    return ic

def lsd_solution():
    ic = Icon()
    amber = Mat('#b3621c', shift=0.07, light=0.55)
    body = smooth(2.5, rbox(16, 21.5, 7.6, 8.5, 2.6), rbox(16, 13, 3.4, 2))
    ic.shape(body, amber, r=3, spec=0.5, gloss=10, alpha=235, out_alpha=255)
    lab = rbox(16, 22, 7.6, 4.4, 0.3)
    ic.shape(lab, Mat('#f4f1e8', dark=0.45), r=1, outline=False)
    lm = ic.mask_of(rbox(16, 22, 7.6, 1.6))
    art = lsd_art(cx=8, cy=22)
    ic.a = np.where(lm[:, :, None], art, ic.a)
    ic.shape(rbox(16, 10, 4.2, 2.4, 0.8), DARKP, r=1.4, tex=stripes(0, 1.0, 0.5), tex_amt=0.2)
    ic.shape(smooth(1, ellipse(16, 5.0, 2.8, 3.4), rbox(16, 7.5, 2.2, 1)), DARKP, r=2, spec=0.4)
    ic.line(10.5, 15.5, 10.5, 19, (255, 230, 200, 200)); ic.px(10.5, 27, (255, 230, 200, 160))
    return ic

# ================================================================== lab & tools
def solvent_can(ic, cx=15, cy=18.5):
    can = Mat('#3f70b2', shift=0.07)
    ic.shape(sub(rbox(cx - 3, cy - 10.5, 5.5, 3.2, 1.6), rbox(cx - 3, cy - 10.2, 3.6, 1.5, 0.8)), can, r=1.2)
    ic.shape(rbox(cx + 6.5, cy - 11.0, 2.4, 2.8, 0.8), DARKP, r=1.2, tex=stripes(90, 1.0, 0.5), tex_amt=0.2)
    ic.shape(rbox(cx, cy, 10.5, 10.5, 2.4), can, r=3, spec=0.3, gloss=12)
    ic.line(cx - 8, cy - 7.5, cx + 7, cy + 7.5, can[3]); ic.line(cx - 8, cy + 7.5, cx + 7, cy - 7.5, can[3])
    return can

def hazard_label(ic, cx, cy, hw=6, hh=5):
    ic.shape(rbox(cx, cy, hw, hh, 0.6), Mat('#f2f0e6', dark=0.45), r=1.2, outline=True, shadow=0.75)
    tri = poly([(cx, cy - hh + 1.4), (cx + hw - 1.4, cy + hh - 1.2), (cx - hw + 1.4, cy + hh - 1.2)])
    ic.shape(tri, Mat('#e8a020'), r=1, outline=False)
    flame = smooth(0.8, ellipse(cx, cy + 1.6, 1.8, 1.6), ellipse(cx + 0.2, cy, 0.8, 1.9))
    ic.shape(flame, Mat('#c8301e'), r=0.8, outline=False)

def solvent():
    ic = Icon()
    solvent_can(ic)
    hazard_label(ic, 15, 19.5)
    return ic

def solvent_label_3d():
    """Front face texture for the 3D canister model."""
    ic = Icon()
    can = Mat('#3f70b2', shift=0.07)
    ic.shape(rbox(16, 16, 20, 20), can, r=1, flat=True, outline=False, tex=noise(4, 2), tex_amt=0.08)
    hazard_label(ic, 16, 17, 9, 8)
    return ic

def blotter_top_3d():
    ic = Icon()
    ic.a = lsd_art()
    perf = ((np.floor(X) % 8) == 0) | ((np.floor(Y) % 8) == 0)
    dots = perf & (((np.floor(X) + np.floor(Y)) % 2) == 0)
    ic.tint(dots, 0.72)
    return ic

def reagent():
    ic = Icon()
    neck = rbox(16, 7.5, 2.6, 4.5, 0.6)
    body = poly([(13.4, 11), (18.6, 11), (27, 27), (26.5, 29.5), (5.5, 29.5), (5, 27)])
    flask = smooth(1.2, neck, body)
    ic.shape(flask, GLASS, r=2, alpha=140, out_alpha=255, amb=0.45)
    liq = Mat('#3a8de0', shift=0.08, light=0.6)
    ic.shape(inter(grow(body, -1.2), lambda x, y: 19.5 - y), liq, r=2, amb=0.3, alpha=235, outline=False, tex=noise(2, 4), tex_amt=0.15)
    ic.shape(ellipse(16, 19.6, 6.0, 0.9), Mat('#8fd0ff'), r=0.6, outline=False, flat=True)
    for x, y in ((12, 24), (18, 22), (15, 26), (20, 26), (17, 25)):
        ic.px(x, y, hx('#cfeeff'))
    ic.shape(rbox(16, 3.6, 3.4, 1.5, 0.6), CORK, r=1, tex=grain(2), tex_amt=0.3)
    ic.line(14.3, 12.5, 14.3, 7.5, hx('#ffffff', 200))
    ic.line(11, 18, 8, 25, hx('#ffffff', 170))
    return ic

def lighter():
    ic = Icon()
    body = rbox(16, 20.5, 5.4, 9.5, 2.0)
    ic.shape(body, Mat('#d8342a', shift=0.08), r=3, spec=0.25, gloss=12)
    ic.shape(rbox(16, 10.2, 5.4, 1.8, 0.5), STEEL, r=1.2)
    ic.shape(rbox(13.4, 7.2, 2.6, 2.4, 0.4), STEEL, r=1.2, tone=-0.05)
    ic.shape(ellipse(18.7, 7.5, 1.7, 2.3), Mat('#5a5e66'), r=1.4, tex=stripes(0, 1.0, 0.5), tex_amt=0.3)
    flame_o = smooth(1.5, ellipse(13.5, 2.6, 2.3, 3.6), circle(13.5, 4.3, 2.3))
    ic.shape(flame_o, Mat('#ff8c1a', light=0.9), r=1.6, tone=0.15, outline=False)
    ic.shape(ellipse(13.5, 3.8, 1.1, 1.8), Mat('#fff2a0', light=0.9), r=1, tone=0.2, outline=False)
    return ic

def fertilizer():
    ic = Icon()
    sack = Mat('#d6bd88', shift=0.07)
    body = smooth(2.5, rbox(16, 20.5, 10.8, 9.5, 4), rbox(16, 10, 6.5, 3, 1))
    ic.shape(body, sack, r=4.5, tex=noise(2.5, 3), tex_amt=0.15)
    top = warp(poly([(9.5, 9), (22.5, 9), (24, 3), (21, 4.5), (18.5, 2.5), (16, 4.2), (13.5, 2.5), (11, 4.5), (8, 3)]), 0.4, 2, 5)
    ic.shape(top, sack, r=2, tone=0.04)
    ic.shape(rbox(16, 9.6, 7.2, 1.1, 0.5), Mat('#8a5e34'), r=0.9, tex=stripes(-60, 1.4, 0.5), tex_amt=0.3)
    green = Mat('#4fa03a', shift=0.09)
    ic.shape(capsule(16, 26, 16, 19, 0.8), green, r=0.8)
    ic.shape(leaf(16, 21, -55, 7, 4.2), green, r=1.5)
    ic.shape(leaf(16, 19, 50, 8, 4.6), green, r=1.5)
    ic.shape(rbox(16, 27.2, 6, 0.9, 0.3), Mat('#7a5a34'), r=0.6, outline=False)
    for x, y, c in ((26, 29, '#5aa0d0'), (28, 30, '#7ac060'), (4, 30, '#5aa0d0')):
        ic.px(x, y, c)
    return ic

def smoking_pipe():
    ic = Icon()
    ic.shape(capsule(4, 24.5, 10, 22.5, 1.4, 1.6), DARKP, r=1.4, spec=0.5, gloss=10)
    ic.shape(capsule(10, 22.5, 18, 21, 1.7, 2.1), WOOD, r=1.8, tex=stripes(-10, 2.2, 0.4), tex_amt=0.15)
    bowl = smooth(1.5, rbox(22, 16, 5.6, 7.6, 2.6), ellipse(22, 22, 4.6, 2.8))
    ic.shape(bowl, WOOD, r=3.5, spec=0.35, gloss=12, tex=np.clip(stripes(12, 2.6, 0.35) + noise(3, 2) * 0.4, 0, 1), tex_amt=0.2)
    ic.shape(ellipse(22, 8.8, 5.5, 1.7), WOOD_D, r=1, tone=0.1)
    ic.shape(ellipse(22, 9.0, 4.0, 1.0), Mat('#3a2418'), r=0.6, outline=False)
    for x, c in ((20, EMBER[4]), (22, EMBER[6]), (24, EMBER[3]), (21, ASH[3])):
        ic.px(x, 9, c)
    steam(ic, 21, 6, 6, 0.5)
    return ic

def drug_test():
    ic = Icon()
    white = Mat('#eef0f3', dark=0.45)
    ic.shape(rbox(16, 17, 6.8, 13.2, 2.4), white, r=2.4)
    ic.shape(rbox(16, 4.8, 4.2, 1.6, 0.6), Mat('#4a8ad8'), r=1.1, shadow=0.75)
    ic.shape(rbox(16, 12.5, 3.0, 5.6, 0.6), Mat('#d9d2c6', dark=0.4), r=1, tone=-0.1)
    ic.shape(rbox(16, 12.5, 2.0, 4.6, 0.3), Mat('#f4eee4', dark=0.3), r=0.6, outline=False, flat=True)
    red = hx('#d83a3a')
    for y in (10, 15):
        ic.line(14.5, y + 0.5, 17.5, y + 0.5, red)
    grey = hx('#9a9aa2')
    ic.px(20, 10, grey); ic.px(21, 10, grey); ic.px(20, 15, grey); ic.px(21, 15, grey); ic.px(20, 16, grey)
    ic.shape(circle(16, 24, 2.6), white, r=1.6, tone=-0.3)
    ic.shape(circle(16, 24.2, 1.3), Mat('#c8c2b8'), r=1, outline=False, tone=-0.1)
    return ic

def _sheet(ic):
    sh = poly([(3.5, 15.5), (24.5, 8.5), (28.5, 18.5), (7.5, 25.5)])
    ic.shape(sh, PAPER, r=1.6, tex=stripes(-18, 1.2, 0.5), tex_amt=0.05)
    ic.shape(capsule(7.8, 25.2, 28.2, 18.3, 1.6), PAPER, r=1.6, tone=0.04)
    return sh

def incomplete_rolled(fill_cols, seed):
    ic = Icon()
    sh = _sheet(ic)
    band = inter(sh, capsule(8, 19.5, 25, 13.8, 3.6))
    rng = random.Random(seed)
    m = ic.mask_of(band)
    pts = [(x, y) for y in range(N) for x in range(N) if m[y, x]]
    for _ in range(85):
        x, y = rng.choice(pts)
        ic.px(x, y, rng.choice(fill_cols))
    return ic

def incomplete_cigar():
    ic = Icon()
    x0, y0, x1, y1 = 4.5, 27.5, 26, 6
    ic.shape(diag_seg(x0, y0, x1, y1, 0.3, 0.97, 2.8), TOB_DRY, r=2.6, tex=noise(1.1, 5), tex_amt=0.6)
    rng = random.Random(3)
    for _ in range(10):
        t = rng.uniform(0.5, 0.95)
        cx, cy = _t(x0, y0, x1, y1, t)
        ic.line(cx - 1.5, cy + 1.5, cx + 1.2, cy - 1.2, rng.choice([TOB_DRY[5], TOB_DRY[1], hx('#e0a858')]))
    m = Mat('#7c4a26', shift=0.08)
    ic.shape(diag_seg(x0, y0, x1, y1, 0, 0.5, 3.0, 3.1), m, r=3.2, tex=stripes(-15, 2.4, 0.3), tex_amt=0.3)
    ax, ay = _t(x0, y0, x1, y1, 0.5)
    ic.shape(poly([(ax - 2.4, ay - 2.0), (ax + 2.2, ay + 2.4), (ax + 4.5, ay - 1.5), (ax + 1.5, ay - 4.0)]), m, r=1.6, tone=0.05, tex=stripes(30, 2.5, 0.3), tex_amt=0.2)
    return ic


# ================================================================== meth & ecstasy
def _flask(ic, liquid, cloud, stopper=None):
    flask = smooth(1.2, circle(16, 21, 8.8), rbox(16, 9.5, 2.6, 5))
    ic.shape(flask, GLASS, r=2, alpha=130, out_alpha=255, amb=0.45)
    liq = inter(grow(circle(16, 21, 8.8), -1.2), lambda x, y: 16.5 - y)
    ic.shape(liq, liquid, r=3, alpha=225, outline=False, tex=noise(1.8, 7), tex_amt=cloud)
    ic.shape(ellipse(16, 16.7, 7.2, 0.9), Mat(_hd.lighter(liquid[4], 0.4)), r=0.5, outline=False, flat=True)
    for x, y in ((12, 23), (18, 21), (15, 26), (20, 25)):
        ic.px(x, y, (255, 255, 255, 170))
    ic.shape(rbox(16, 4.4, 3.2, 2.2, 0.8), stopper or Mat('#5a5e66'), r=1.4)
    ic.line(11.5, 16.5, 9.5, 21, (255, 255, 255, 190)); ic.line(14.2, 6.5, 14.2, 11, (255, 255, 255, 170))
    return ic

def meth_batch():
    return _flask(Icon(), Mat('#cfe3ea', dark=0.5, light=0.8), 0.45)

def mdma_batch():
    return _flask(Icon(), Mat('#b78a52', shift=0.07, light=0.6), 0.4)

def precursor():
    ic = Icon()
    jar = rbox(16, 19.5, 9.5, 10, 2.6)
    ic.shape(jar, GLASS, r=2.5, alpha=140, out_alpha=255, amb=0.45)
    powder = inter(grow(jar, -1.4), warp(lambda x, y: 15.5 - y, 1.0, 2, 4))
    ic.shape(powder, Mat('#f1f0ec', dark=0.4, light=0.9), r=3, outline=False, tex=grain(3), tex_amt=0.3)
    ic.shape(rbox(16, 22, 9.5, 3.8, 0.3), Mat('#f4efe0', dark=0.45), r=1, outline=False)
    ic.shape(rbox(16, 22, 9.5, 1.0, 0.1), Mat('#e2742a'), r=0.5, outline=False, flat=True)
    ic.shape(rbox(16, 8.2, 8.2, 2.4, 1.0), CORK, r=1.4, tex=grain(5), tex_amt=0.3)
    ic.line(8.5, 11.5, 8.5, 17, (255, 255, 255, 200))
    return ic

def catalyst():
    ic = Icon()
    steel = Mat('#8e9aa6', dark=0.7, light=0.85)
    copper = Mat('#c8794a', shift=0.08)
    rng = random.Random(4)
    pts = [(10, 21), (16, 23), (22, 21), (13, 16), (19, 16), (8, 26), (14, 27), (20, 27), (25, 26), (16, 11)]
    for i, (x, y) in enumerate(pts):
        m = copper if i in (2, 6) else steel
        rr = rng.uniform(3.0, 3.8)
        ic.shape(circle(x, y, rr), m, r=rr, spec=0.7, gloss=10, shadow=0.75)
        ic.px(x - rr * 0.45, y - rr * 0.45, '#ffffff')
    return ic

def aromatic_oil():
    ic = Icon()
    oil = Mat('#d9a12c', shift=0.08, light=0.6)
    body = smooth(1.5, rbox(14, 20.5, 6.4, 9, 2.5), rbox(14, 10, 2.4, 3))
    ic.shape(body, GLASS, r=2.4, alpha=140, out_alpha=255, amb=0.45)
    ic.shape(inter(grow(body, -1.2), lambda x, y: 15 - y), oil, r=3, alpha=225, outline=False, spec=0.3)
    ic.shape(rbox(14, 5.6, 3.0, 2.2, 0.8), CORK, r=1.4, tex=grain(2), tex_amt=0.3)
    ic.line(9.5, 14.5, 9.5, 26, (255, 255, 255, 190))
    pine = Mat('#3f7a3a', shift=0.08)
    ic.shape(capsule(21, 30, 27.5, 13, 0.7), Mat('#6a4a2a'), r=0.7)
    for k in range(6):
        y = 27 - k * 2.6
        x = 21 + (30 - y) * 0.38
        ic.shape(capsule(x, y, x - 3.2, y - 1.4, 0.6), pine, r=0.6, outline=False)
        ic.shape(capsule(x, y, x + 3.2, y - 1.8, 0.6), pine, r=0.6, outline=False)
    return ic

def _shards(ic, mat, seed, shards):
    for i, (cx, cy, w, h, ang) in enumerate(shards):
        a = math.radians(ang)
        ca, sa = math.cos(a), math.sin(a)
        pts = [(0, -h), (w * 0.55, -h * 0.35), (w * 0.5, h * 0.6), (0, h * 0.8), (-w * 0.5, h * 0.55), (-w * 0.6, -h * 0.3)]
        poly_pts = [(cx + px * ca - py * sa, cy + px * sa + py * ca) for px, py in pts]
        ic.shape(poly(poly_pts), mat, r=1.6, spec=0.9, gloss=6, tex=stripes(ang + 60, 2.2, 0.35), tex_amt=0.25, shadow=0.75)
        tx, ty = poly_pts[0]
        ic.px(tx, ty + 1, '#ffffff')

def crystal_meth():
    ic = Icon()
    m = Mat('#d7eef4', dark=0.55, light=0.9, shift=0.04)
    _shards(ic, m, 3, [(11, 15, 7, 10, -20), (21, 13, 6, 9, 25), (16, 22, 9, 9, 5), (8, 26, 5, 5, -40), (25, 24, 5, 6, 35)])
    for x, y in ((14, 9), (24, 19), (12, 20)):
        ic.glint(x, y, '#ffffff', 2)
    return ic

def mdma_crystals():
    ic = Icon()
    m = Mat('#e2c79a', dark=0.6, light=0.8, shift=0.05)
    _shards(ic, m, 5, [(12, 14, 7, 9, -15), (21, 15, 6, 8, 20), (15, 23, 8, 8, 0), (24, 25, 5, 5, 30), (7, 24, 4, 5, -30)])
    for x, y in ((13, 9), (22, 20)):
        ic.glint(x, y, '#fff6e0', 2)
    return ic

def mdma_powder():
    ic = Icon()
    paper = poly([(3, 20), (17, 13), (29, 19), (15, 27)])
    ic.shape(paper, PAPER, r=1.5, tex=stripes(-25, 1.4, 0.5), tex_amt=0.05)
    ic.shape(capsule(4, 20, 15.5, 26.5, 0.9), PAPER, r=1, tone=-0.05)
    pile = warp(inter(ellipse(16, 20.5, 8.5, 7), lambda x, y: y - 21.5), 1.0, 2, 6)
    ic.shape(pile, Mat('#ece2cf', dark=0.45, light=0.9), r=4, tex=grain(4), tex_amt=0.3)
    for x, y in ((9, 21), (24, 20), (12, 24), (21, 24)):
        ic.px(x, y, hx('#e2d6c0'))
    return ic

def _stamp(kind, cx, cy, s):
    if kind == 'heart':
        return smooth(0.6, circle(cx - s * 0.42, cy - s * 0.2, s * 0.48), circle(cx + s * 0.42, cy - s * 0.2, s * 0.48),
                      poly([(cx - s * 0.88, cy - 0.05 * s), (cx + s * 0.88, cy - 0.05 * s), (cx, cy + s * 0.95)]))
    if kind == 'star':
        pts = []
        for k in range(10):
            a = -math.pi / 2 + k * math.pi / 5
            r = s if k % 2 == 0 else s * 0.45
            pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
        return poly(pts)
    if kind == 'diamond':
        return poly([(cx, cy - s), (cx + s * 0.75, cy), (cx, cy + s), (cx - s * 0.75, cy)])
    return circle(cx, cy, s * 0.9)  # smiley base

def pill(color, kind):
    ic = Icon()
    m = Mat(color, shift=0.07, light=0.6)
    ic.shape(circle(25, 7.5, 4.6), m, r=3, spec=0.25, tone=-0.05)
    ic.shape(rbox(25, 7.5, 2.4, 0.45, 0, ang=-35), m, r=0.4, tone=-0.25, outline=False, flat=True)
    main = circle(14, 19, 9.6)
    ic.shape(main, m, r=4.5, spec=0.3, gloss=12, tex=grain(9), tex_amt=0.1)
    # embossed stamp: dark lower-right edge, light upper-left edge
    st = _stamp(kind, 14, 19, 5.4)
    mask = ic.mask_of(st)
    ic.tint(mask, 0.9)
    lit = np.zeros_like(mask); lit[1:, 1:] = mask[:-1, :-1]
    ic.tint(mask & ~lit, 1.18)
    shade = np.zeros_like(mask); shade[:-1, :-1] = mask[1:, 1:]
    ic.tint(shade & ~mask, 0.75)
    if kind == 'smiley':
        dark = darker(m[2], 0.8)
        ic.px(11.5, 16.5, dark); ic.px(16.5, 16.5, dark); ic.px(11.5, 17.5, dark); ic.px(16.5, 17.5, dark)
        for x in range(11, 19):
            y = 20.5 + round(math.sin((x - 10.5) / 8 * math.pi) * 1.8)
            ic.px(x, y, dark)
    ic.glint(9, 13, '#ffffff', 1)
    return ic

def glass_pipe():
    ic = Icon()
    x0, y0, x1, y1 = 4, 28, 21, 11
    ic.shape(diag_seg(x0, y0, x1, y1, 0, 1.0, 1.5), GLASS, r=1.5, alpha=150, out_alpha=255, amb=0.5)
    bulb = circle(23.5, 8.5, 5.6)
    ic.shape(bulb, GLASS, r=4, alpha=140, out_alpha=255, amb=0.5, spec=0.5)
    ic.shape(inter(grow(bulb, -1.4), lambda x, y: 9.5 - y), Mat('#b0b8bc'), r=1.5, alpha=150, outline=False)
    ic.shape(circle(25.5, 4.6, 1.6), GLASS, r=1, alpha=160)
    ic.px(21, 5, '#ffffff'); ic.px(20, 6, '#ffffff'); ic.line(6, 25, 15, 16, (255, 255, 255, 170))
    return ic

RUBBER = Mat('#33363d', dark=0.6, light=0.4)
HAZ = Mat('#e8c12a', shift=0.08)

def gas_mask():
    ic = Icon()
    ic.shape(capsule(3, 13, 7, 15, 1.2), Mat('#5a5d63'), r=1)
    ic.shape(capsule(29, 13, 25, 15, 1.2), Mat('#5a5d63'), r=1)
    face = smooth(2.5, ellipse(16, 14, 11, 9.5), ellipse(16, 21, 7.5, 6))
    ic.shape(face, RUBBER, r=5, spec=0.35, gloss=10)
    lens_rim = Mat('#9aa3ad', dark=0.7)
    lens = Mat('#6fc3d8', shift=0.05, light=0.7)
    for cx in (11, 21):
        ic.shape(circle(cx, 12.5, 4.6), lens_rim, r=1.6, spec=0.4)
        ic.shape(circle(cx, 12.5, 3.2), lens, r=3, spec=0.8, gloss=8, outline=False)
        ic.px(cx - 1.5, 11, '#ffffff'); ic.px(cx - 1, 10.5, '#ffffff')
    filt = Mat('#6f7a3a', shift=0.06)
    ic.shape(ellipse(16, 25.5, 5.6, 4.8), filt, r=3, spec=0.3)
    ic.shape(ellipse(16, 25.5, 3.6, 3.0), filt, r=1.5, tone=-0.15, outline=False, tex=stripes(0, 1.2, 0.5), tex_amt=0.4)
    ic.shape(rbox(16, 19.5, 2.5, 1.3, 0.5), Mat('#9aa3ad'), r=0.8)
    return ic

def hazmat_suit():
    ic = Icon()
    body = poly([(9, 5), (23, 5), (29, 10), (29, 20), (25, 21), (24, 13), (23, 29), (9, 29), (8, 13), (7, 21), (3, 20), (3, 10)])
    ic.shape(warp(body, 0.4, 3, 2), HAZ, r=3, tex=noise(3, 4), tex_amt=0.15, spec=0.2)
    ic.shape(rbox(16, 5.5, 4.5, 1.6, 0.8), RUBBER, r=1)
    ic.line(16, 7, 16, 28, HAZ[1]); ic.line(17, 7, 17, 28, HAZ[5])
    ic.shape(rbox(16, 22, 7.2, 1.1, 0.3), Mat('#7d8086'), r=0.8, outline=False)
    ic.shape(rbox(5, 20, 2.4, 1.6, 0.6), RUBBER, r=1); ic.shape(rbox(27, 20, 2.4, 1.6, 0.6), RUBBER, r=1)
    return ic

def hazmat_pants():
    ic = Icon()
    pants = poly([(7, 4), (25, 4), (27, 28), (19, 28), (16, 12), (13, 28), (5, 28)])
    ic.shape(warp(pants, 0.4, 3, 6), HAZ, r=3, tex=noise(3, 5), tex_amt=0.15, spec=0.2)
    ic.shape(rbox(16, 5, 9, 1.4, 0.4), Mat('#7d8086'), r=0.8)
    for cx in (9.5, 22.5):
        ic.shape(rbox(cx, 18, 3.2, 2.2, 0.8), Mat('#c9a018'), r=1.2, outline=False)
        ic.shape(rbox(cx, 27, 4.0, 1.4, 0.4), RUBBER, r=0.8)
    return ic

def hazmat_boots():
    ic = Icon()
    for ox, tone in ((-6.5, -0.05), (6.5, 0.0)):
        boot = smooth(1.5, rbox(16 + ox, 14, 3.8, 9, 1.5), rbox(16 + ox + 2.2, 24.5, 6.2, 3.5, 1.8))
        ic.shape(boot, RUBBER, r=3, spec=0.45, gloss=10, tone=tone)
        ic.shape(rbox(16 + ox, 9, 3.8, 1.0, 0.3), HAZ, r=0.8, outline=False)
        ic.shape(rbox(16 + ox + 2.2, 27.6, 6.4, 0.9, 0.3), Mat('#1d1f23'), r=0.5, outline=False)
    return ic

def lab_items():
    return {
        'precursor': precursor(), 'catalyst': catalyst(), 'aromatic_oil': aromatic_oil(), 'meth_batch': meth_batch(),
        'crystal_meth': crystal_meth(), 'glass_pipe': glass_pipe(), 'mdma_batch': mdma_batch(), 'mdma_crystals': mdma_crystals(),
        'mdma_powder': mdma_powder(),
        'ecstasy_pill_pink': pill('#f07ab5', 'heart'), 'ecstasy_pill_blue': pill('#5fb4ec', 'star'),
        'ecstasy_pill_yellow': pill('#f1d23a', 'smiley'), 'ecstasy_pill_purple': pill('#a77ae0', 'diamond'),
        'gas_mask': gas_mask(), 'hazmat_suit': hazmat_suit(), 'hazmat_pants': hazmat_pants(), 'hazmat_boots': hazmat_boots(),
    }

def all_items():
    G = [LEAF[2], LEAF[4], BUD[3], BUD[5], PISTIL]
    T = [TOB_DRY[2], TOB_DRY[4], TOB_DRY[5], hx('#d8a050')]
    return {
        'cannabis_seeds': cannabis_seeds(),
        'cannabis_leaf': cannabis_leaf(),
        'cannabis_bud': cannabis_bud(),
        'dried_cannabis_bud': cannabis_bud(True),
        'ground_cannabis': ground_cannabis(),
        'cannabis_resin': cannabis_resin(),
        'hashish': hashish(),
        'hemp_fiber': hemp_fiber(),
        'rolling_paper': rolling_paper(),
        'joint': joint(),
        'spliff': joint(True),
        'space_cookie': space_cookie(),
        'tobacco_seeds': tobacco_seeds(),
        'tobacco_leaf': tobacco_leaf(),
        'dried_tobacco': tobacco_leaf(True),
        'cut_tobacco': cut_tobacco(),
        'cigarette': cigarette(),
        'cigar': cigar(),
        'coca_seeds': coca_seeds(),
        'coca_leaves': coca_leaves(),
        'dried_coca_leaves': coca_leaves(True),
        'coca_paste': coca_paste(),
        'cocaine': cocaine(),
        'coca_tea': coca_tea(),
        'poppy_seeds': poppy_seeds(),
        'poppy_pod': poppy_pod(),
        'raw_opium': raw_opium(),
        'opium': opium(),
        'morphine': morphine(),
        'empty_syringe': syringe(False),
        'morphine_syringe': syringe(True),
        'mushroom_spores': mushroom_spores(),
        'magic_mushroom': magic_mushroom(),
        'dried_magic_mushroom': magic_mushroom(True),
        'mushroom_tea': mushroom_tea(),
        'ergot': ergot(),
        'lsd_solution': lsd_solution(),
        'lsd_blotter': lsd_blotter(),
        'solvent': solvent(),
        'reagent': reagent(),
        'fertilizer': fertilizer(),
        'scoring_knife': scoring_knife(),
        'smoking_pipe': smoking_pipe(),
        'lighter': lighter(),
        'drug_test': drug_test(),
        'detox_tea': detox_tea(),
        **lab_items(),
    }

def addon_items():
    G = [LEAF[2], LEAF[4], BUD[3], BUD[5], LEAF[1], PISTIL]
    T = [TOB_DRY[2], TOB_DRY[4], TOB_DRY[5], hx('#d8a050'), TOB_DRY[1]]
    return {
        'incomplete_joint': incomplete_rolled(G, 1),
        'incomplete_spliff': incomplete_rolled(G[:4] + T[:3], 2),
        'incomplete_cigarette': incomplete_rolled(T, 3),
        'incomplete_cigar': incomplete_cigar(),
        'incomplete_blotter_sheet': lsd_blotter(half=True),
        'incomplete_syringe': syringe(False, incomplete=True),
    }

def textures_3d():
    return {'blotter_top': blotter_top_3d(), 'solvent_label': solvent_label_3d()}

if __name__ == '__main__':
    import sys
    out = sys.argv[1] if len(sys.argv) > 1 else 'items_hd.png'
    sheet(list(all_items().items()) + list(addon_items().items()) + list(textures_3d().items()), out, scale=5, cols=9)
