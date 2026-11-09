"""Block, armor and GUI textures for the drug lab (16x16 block textures, 64x32 armor layers)."""
import random, math
from px import *

STEEL = [hexc('#b9c0c7'), hexc('#a7aeb6'), hexc('#c9d0d6'), hexc('#9aa1a9')]
STEEL_D = hexc('#6b727a'); STEEL_DD = hexc('#4a5057'); STEEL_L = hexc('#e2e7eb')

def _noise(cv, cols, seed, x0=0, y0=0, x1=15, y1=15, weights=None):
    noise_fill(cv, x0, y0, x1, y1, cols, seed, weights)

def _frame(cv, c_dark, c_light):
    for i in range(16):
        cv.set(i, 0, c_light); cv.set(0, i, c_light); cv.set(i, 15, c_dark); cv.set(15, i, c_dark)

def steel(seed=1, frame=True):
    cv = Canvas(); _noise(cv, STEEL, seed, weights=[4, 3, 2, 1])
    if frame: _frame(cv, STEEL_D, STEEL_L)
    return cv

def brushed(seed=2):
    cv = Canvas(); rnd = random.Random(seed)
    for y in range(16):
        base = rnd.choice(STEEL)
        for x in range(16):
            cv.set(x, y, base if rnd.random() < 0.8 else rnd.choice(STEEL))
    return cv

# ---------------- tiles / stripes ----------------
def lab_tiles():
    cv = Canvas(); rnd = random.Random(3)
    tiles = [hexc('#f2f3f1'), hexc('#ecedeb'), hexc('#f7f8f6')]
    for ty in range(2):
        for tx in range(2):
            c = rnd.choice(tiles)
            cv.rect(tx * 8, ty * 8, tx * 8 + 7, ty * 8 + 7, c)
            for i in range(8):
                cv.set(tx * 8 + i, ty * 8, hexc('#ffffff')); cv.set(tx * 8, ty * 8 + i, hexc('#ffffff'))
            cv.set(tx * 8 + 2, ty * 8 + 2, hexc('#ffffff'))
    grout = hexc('#b8bcbc')
    for i in range(16):
        cv.set(i, 7, grout); cv.set(7, i, grout); cv.set(i, 15, grout); cv.set(15, i, grout)
    return cv

def hazard_stripes():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#f0c020') if ((x + y) // 4) % 2 == 0 else hexc('#1e1e1e'))
    for i in range(16):
        cv.set(i, 0, shade(cv.get(i, 0), 1.1)); cv.set(i, 15, shade(cv.get(i, 15), 0.8))
    return cv

# ---------------- chemical reactor ----------------
def reactor_base(on=False):
    cv = steel(4)
    cv.rect(3, 4, 12, 11, hexc('#2a2d32'))
    for x in range(4, 12):
        for y in (5, 7, 9):
            cv.set(x, y, hexc('#ff6a1a') if on else hexc('#4a4e55'))
            if on and x % 2 == 0: cv.set(x, y + 1, hexc('#ffb040'))
    cv.set(13, 4, hexc('#40d040') if on else hexc('#304030')); cv.set(13, 6, hexc('#d04040'))
    return cv

def reactor_vessel():
    cv = brushed(5)
    for y in (3, 12):
        for x in range(16):
            cv.set(x, y, STEEL_D)
            if x % 3 == 1: cv.set(x, y, STEEL_L)
    # gauge
    for (x, y) in ((11, 6), (12, 6), (13, 7), (13, 8), (12, 9), (11, 9), (10, 8), (10, 7)):
        cv.set(x, y, hexc('#2a2a2a'))
    cv.rect(11, 7, 12, 8, hexc('#f4f4f0')); cv.set(12, 7, hexc('#d03030'))
    return cv

def reactor_top():
    cv = brushed(6)
    for x in range(16):
        for y in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.5 < d < 6.6: cv.set(x, y, STEEL_D)
            if d < 2: cv.set(x, y, hexc('#3a3e45'))
    return cv

def reactor_liquid(on=False):
    cv = Canvas(); rnd = random.Random(7)
    cols = [hexc('#bfe0ea', 220), hexc('#d8eef3', 220), hexc('#a6d0de', 220)] if on else [hexc('#9fb8c0', 200), hexc('#b0c6cc', 200)]
    _noise(cv, cols, 7)
    return cv

# ---------------- crystallizer ----------------
def crystallizer_side():
    cv = steel(8)
    rnd = random.Random(8)
    for _ in range(14):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 6)
        cv.set(x, y, hexc('#eaf6fb'))
    cv.rect(2, 8, 13, 9, hexc('#3a6ab0'))
    return cv

def crystallizer_front(on=False):
    cv = crystallizer_side()
    cv.rect(4, 10, 11, 14, hexc('#24272c'))
    flake = hexc('#7fd0ff') if on else hexc('#4a5560')
    for (x, y) in ((7, 10), (7, 11), (7, 12), (7, 13), (7, 14), (5, 12), (6, 12), (8, 12), (9, 12), (5, 10), (9, 14), (9, 10), (5, 14)):
        cv.set(x, y, flake)
    return cv

def crystallizer_top(on=False):
    cv = Canvas()
    _noise(cv, [hexc('#2f3a44'), hexc('#36424d')], 9)
    rnd = random.Random(10)
    if on:
        for _ in range(26):
            x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
            cv.set(x, y, rnd.choice([hexc('#e6f4f8'), hexc('#c7e6ef'), hexc('#ffffff')]))
            if rnd.random() < 0.5: cv.set(x + 1, y, hexc('#a9d3e0'))
    for i in range(16):
        cv.set(i, 0, STEEL_L); cv.set(0, i, STEEL_L); cv.set(i, 15, STEEL_D); cv.set(15, i, STEEL_D)
    return cv

def frost():
    cv = Canvas(); rnd = random.Random(11)
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.35: cv.set(x, y, hexc('#e8f6fb', 150))
    return cv

# ---------------- pill press ----------------
def press_paint(seed=12):
    cv = Canvas(); _noise(cv, [hexc('#3f6b5a'), hexc('#456f5e'), hexc('#3a6252')], seed)
    _frame(cv, hexc('#294538'), hexc('#5d8a77'))
    return cv

def pill_press_front(on=False):
    cv = press_paint(13)
    cv.rect(5, 4, 10, 9, hexc('#e8e6dc'))
    for (x, y) in ((6, 5), (9, 5), (7, 8)):
        cv.set(x, y, hexc('#555555'))
    cv.set(7, 6, hexc('#d03030')); cv.set(8, 7, hexc('#d03030'))
    cv.set(12, 11, hexc('#40d040') if on else hexc('#305030'))
    return cv

def pill_press_hopper():
    cv = brushed(14)
    cv.rect(3, 3, 12, 12, hexc('#2a2d32'))
    for (x, y) in ((5, 6), (8, 5), (10, 8), (6, 10), (9, 11)):
        cv.set(x, y, hexc('#f07ab5')); cv.set(x + 1, y, hexc('#d05a95'))
    return cv

# ---------------- fume hood / exhaust fan ----------------
def enamel(seed=15):
    cv = Canvas(); _noise(cv, [hexc('#e9ebe6'), hexc('#e1e3de'), hexc('#eff1ec')], seed)
    _frame(cv, hexc('#a9aca6'), hexc('#ffffff'))
    return cv

def hood_inside():
    cv = Canvas(); _noise(cv, [hexc('#3a3f44'), hexc('#41474d')], 16)
    for x in range(2, 14, 3):
        for y in range(2, 6):
            cv.set(x, y, hexc('#22262a'))
    return cv

def hood_glass():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#cfe7ef', 70))
    for i in range(5):
        cv.set(2 + i, 2 + i, hexc('#ffffff', 150)); cv.set(3 + i, 2 + i, hexc('#ffffff', 100))
    return cv

def fan_front():
    cv = steel(17)
    cv.rect(1, 1, 14, 14, hexc('#2b2e33'))
    cx = cy = 7.5
    for y in range(1, 15):
        for x in range(1, 15):
            a = math.atan2(y - cy, x - cx); r = math.hypot(x - cx, y - cy)
            if 1.5 < r < 6.5 and (math.sin(a * 4 + r * 0.35) > 0.35):
                cv.set(x, y, hexc('#8d949c'))
            if r <= 1.5: cv.set(x, y, hexc('#c9d0d6'))
    for i in range(1, 15, 2):
        for j in range(1, 15):
            if cv.get(i, j) == hexc('#2b2e33'): cv.set(i, j, hexc('#3a3e44'))
    return cv

def duct():
    cv = brushed(18)
    for x in range(16):
        cv.set(x, 4, STEEL_D); cv.set(x, 11, STEEL_D)
    return cv

# ---------------- furniture ----------------
def counter_front():
    cv = Canvas(); _noise(cv, [hexc('#e8e6df'), hexc('#dfddd5')], 19)
    for y in range(16):
        cv.set(7, y, hexc('#9a9890')); cv.set(8, y, hexc('#f6f5f0'))
    for i in range(16):
        cv.set(i, 0, hexc('#2a2d30')); cv.set(i, 15, hexc('#7d7b74'))
    for y in (5, 6, 7):
        cv.set(5, y, hexc('#8e959c')); cv.set(10, y, hexc('#8e959c'))
    return cv

def counter_side():
    cv = Canvas(); _noise(cv, [hexc('#e8e6df'), hexc('#dfddd5')], 20)
    for i in range(16):
        cv.set(i, 0, hexc('#2a2d30')); cv.set(i, 15, hexc('#7d7b74'))
    return cv

def counter_top():
    cv = Canvas(); _noise(cv, [hexc('#2c2f33'), hexc('#303438'), hexc('#282b2f')], 21)
    rnd = random.Random(21)
    for _ in range(4): cv.set(rnd.randrange(16), rnd.randrange(16), hexc('#4a4f55'))
    return cv

def barrel_side(base, band, symbol=False):
    cv = Canvas()
    b = hexc(base)
    for y in range(16):
        for x in range(16):
            c = b
            if x < 3: c = shade(b, 1.15)
            elif x > 12: c = shade(b, 0.8)
            cv.set(x, y, c)
    for y in (2, 13):
        for x in range(16):
            cv.set(x, y, shade(b, 0.7)); cv.set(x, y + 1, shade(b, 1.1))
    if symbol:
        cv.rect(4, 5, 11, 11, hexc('#1e1e1e'))
        cv.ascii(["..##..", ".#..#.", "######", "#.##.#", ".#..#.", "..##.."], {'#': hexc(band)}, 5, 5)
    return cv

def barrel_top(base):
    cv = Canvas(); b = hexc(base)
    _noise(cv, [b, shade(b, 0.95)], 22)
    for x in range(16):
        for y in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 6.3 < d < 7.4: cv.set(x, y, shade(b, 0.7))
    cv.rect(3, 3, 5, 5, hexc('#cfd3d7')); cv.rect(10, 10, 11, 11, hexc('#cfd3d7'))
    return cv

def cylinder_side():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            c = hexc('#c8342c')
            if x < 4: c = shade(c, 1.2)
            elif x > 11: c = shade(c, 0.75)
            cv.set(x, y, c)
    for x in range(16):
        cv.set(x, 6, hexc('#f2f2ee')); cv.set(x, 7, hexc('#f2f2ee'))
    return cv

def cylinder_top():
    cv = Canvas(); _noise(cv, [hexc('#b02c26'), hexc('#c8342c')], 23)
    cv.rect(6, 6, 9, 9, hexc('#9aa1a9'))
    return cv

def bottles():
    """Atlas for shelf bottles: brown/green/blue glass + labels."""
    cv = Canvas()
    for x in range(16):
        for y in range(16):
            col = [hexc('#7a4a20', 230), hexc('#3a7a3a', 230), hexc('#3a6ab0', 220), hexc('#e8e4d8')][x // 4]
            cv.set(x, y, shade(col, 1.15) if x % 4 == 0 else col)
    for x in range(0, 12):
        cv.set(x, 8, hexc('#f0eee6')); cv.set(x, 9, hexc('#f0eee6'))
    return cv

def plastic():
    cv = Canvas(); rnd = random.Random(24)
    for y in range(16):
        for x in range(16):
            a = 95 + int(25 * math.sin(x * 0.9 + y * 0.3)) + rnd.randint(-6, 6)
            cv.set(x, y, (232, 238, 236, a))
    for y in range(16):
        x = int(5 + 2 * math.sin(y * 0.6))
        cv.set(x, y, (255, 255, 255, 150)); cv.set(x + 6, y, (210, 220, 218, 150))
    return cv

def plastic_top():
    cv = Canvas()
    for y in range(16):
        for x in range(7, 9):
            cv.set(x, y, (225, 232, 230, 170))
    return cv

def hazard_sign():
    cv = Canvas()
    cv.rect(0, 0, 15, 15, hexc('#f0c020'))
    _frame(cv, hexc('#1e1e1e'), hexc('#1e1e1e'))
    # triangle
    for y in range(3, 14):
        half = (y - 3) * 0.62
        for x in range(16):
            if abs(x - 7.5) <= half + 0.3:
                cv.set(x, y, hexc('#1e1e1e'))
            if abs(x - 7.5) <= half - 1.2 and 5 <= y <= 12:
                cv.set(x, y, hexc('#f0c020'))
    # small skull
    cv.ascii(["..##..", ".####.", ".#..#.", "..##..", ".#..#."], {'#': hexc('#1e1e1e')}, 5, 7)
    return cv

def sign_back():
    return steel(25)

def light_panel(on=True):
    cv = Canvas()
    cv.rect(0, 0, 15, 15, hexc('#e8eaec'))
    for x in range(1, 15):
        for y in (4, 5, 6, 9, 10, 11):
            cv.set(x, y, hexc('#ffffff') if on else hexc('#c8ccd0'))
    _frame(cv, hexc('#a9aeb3'), hexc('#ffffff'))
    return cv

# ---------------- armor layers (64x32) ----------------
def gas_mask_layer():
    cv = Canvas(64, 32)
    rubber = hexc('#2f3238'); rubber_l = hexc('#3d4148'); strap = hexc('#55595f')
    # head cube UV (8x8x8 at 0,0): top 8..15/0..7, front 8..15/8..15, right 0..7, left 16..23, back 24..31
    for x in range(8, 16):            # front: mask over the lower face
        for y in range(10, 16):
            cv.set(x, y, rubber if (x + y) % 5 else rubber_l)
    for cx in (9, 13):                # lenses
        cv.rect(cx, 10, cx + 1, 11, hexc('#7fd0e6')); cv.set(cx, 10, hexc('#d8f4fb'))
    cv.rect(11, 13, 12, 15, hexc('#6f7a3a')); cv.set(11, 14, hexc('#4f5a2a'))   # filter
    for x in list(range(0, 8)) + list(range(16, 32)):   # straps around the head
        cv.set(x, 11, strap); cv.set(x, 12, strap)
    for x in range(10, 14):
        cv.set(x, 2, strap)
    return cv

def hazmat_layer1():
    cv = Canvas(64, 32)
    y_ = hexc('#e8c12a'); y_d = hexc('#c9a018'); grey = hexc('#7d8086'); dark = hexc('#33363d')
    # body: front 20..27 x 20..31, back 32..39, right 16..19, left 28..31, top 20..27/16..19
    for x in range(16, 40):
        for y in range(16, 32):
            if (x, y) >= (16, 16):
                cv.set(x, y, y_ if (x * 7 + y * 3) % 11 else y_d)
    for y in range(20, 32):
        cv.set(23, y, grey); cv.set(24, y, grey)                 # zipper
    for x in range(20, 28):
        cv.set(x, 27, grey)                                       # belt
    # arms: 40..55 x 16..31
    for x in range(40, 56):
        for y in range(16, 32):
            cv.set(x, y, y_ if (x * 5 + y) % 9 else y_d)
    for x in range(40, 56):
        for y in (29, 30, 31):
            cv.set(x, y, dark)                                    # gloves
    # boots part of layer 1: legs 0..15 x 16..31 (only the bottom for boots)
    for x in range(0, 16):
        for y in range(26, 32):
            cv.set(x, y, dark)
        cv.set(x, 26, y_)
    return cv

def hazmat_layer2():
    cv = Canvas(64, 32)
    y_ = hexc('#e8c12a'); y_d = hexc('#c9a018'); grey = hexc('#7d8086')
    for x in range(0, 16):
        for y in range(16, 32):
            cv.set(x, y, y_ if (x * 3 + y * 5) % 10 else y_d)
        cv.set(x, 23, hexc('#c9a018')); cv.set(x, 24, hexc('#c9a018'))   # knee pads
    for x in range(16, 40):          # waist part of the body
        for y in range(16, 32):
            cv.set(x, y, y_ if (x + y) % 7 else y_d)
    for x in range(20, 28):
        cv.set(x, 20, grey)
    return cv
