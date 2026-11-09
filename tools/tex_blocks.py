"""Block, GUI, effect, particle and villager textures."""
import math, random
from px import *

def metal(seed, base, var=10, cv=None):
    cv = cv or Canvas()
    rnd = random.Random(seed)
    b = hexc(base)
    for y in range(cv.h):
        for x in range(cv.w):
            f = 1 + (rnd.random() - 0.5) * var / 100
            cv.set(x, y, shade(b, f) if f < 1 else mix(b, (255, 255, 255, 255), f - 1))
    return cv

def bevel(cv, x0, y0, x1, y1, light, dark):
    for x in range(x0, x1 + 1):
        cv.set(x, y0, light); cv.set(x, y1, dark)
    for y in range(y0, y1 + 1):
        cv.set(x0, y, light); cv.set(x1, y, dark)

def rivets(cv, pts, c=hexc('#d0d4d8'), d=hexc('#5a5e64')):
    for x, y in pts:
        cv.set(x, y, c); cv.set(x + 1, y + 1, d)

# ---------------- Grinder ----------------
def grinder_side():
    cv = Canvas()
    rnd = random.Random(3)
    stones = [hexc('#8a8a86'), hexc('#7a7a76'), hexc('#9a9a94'), hexc('#6e6e6a')]
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choice(stones))
    for y in (4, 9):
        for x in range(16): cv.set(x, y, hexc('#55554f'))
    for x in (5, 12):
        for y in range(0, 4): cv.set(x, y, hexc('#55554f'))
    for x in (2, 9):
        for y in range(5, 9): cv.set(x, y, hexc('#55554f'))
    for y in range(10, 16):
        for x in range(16):
            cv.set(x, y, metal(1, '#5a5e66').get(x, y))
    bevel(cv, 0, 10, 15, 15, hexc('#7a808a'), hexc('#3a3e44'))
    rivets(cv, [(2, 12), (13, 12)])
    return cv

def grinder_front(on=False):
    cv = grinder_side()
    cv.rect(5, 10, 10, 15, hexc('#1e1e1e'))
    bevel(cv, 4, 10, 11, 15, hexc('#9aa0aa'), hexc('#2a2e34'))
    if on:
        cv.rect(5, 13, 10, 14, hexc('#5f9a3a')); cv.set(6, 12, hexc('#88c24e')); cv.set(8, 13, hexc('#a7d46a')); cv.set(9, 12, hexc('#6aa83a'))
    else:
        cv.rect(5, 14, 10, 14, hexc('#2a2a2a'))
    return cv

def millstone():
    cv = Canvas()
    rnd = random.Random(8)
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choice([hexc('#b0aba0'), hexc('#a29d92'), hexc('#bdb8ad'), hexc('#958f84')]))
    for x in range(16):
        cv.set(x, 0, hexc('#d0cbc0')); cv.set(x, 15, hexc('#6e695e'))
    return cv

def millstone_top():
    cv = Canvas()
    rnd = random.Random(9)
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy)
            c = rnd.choice([hexc('#b0aba0'), hexc('#a29d92'), hexc('#bdb8ad')])
            ang = math.atan2(dy, dx)
            if int((ang + math.pi) / (2 * math.pi) * 12) % 2 == 0 and r > 3: c = shade(c, 0.85)
            if r < 2.5: c = hexc('#2a2a2a')
            cv.set(x, y, c)
    return cv

# ---------------- Extractor ----------------
def extractor_base(front=False, on=False):
    cv = metal(4, '#6a7078')
    bevel(cv, 0, 0, 15, 15, hexc('#9aa0a8'), hexc('#3a3e44'))
    rivets(cv, [(1, 1), (13, 1), (1, 13), (13, 13)])
    if front:
        cv.rect(4, 4, 11, 11, hexc('#2a2e34'))
        bevel(cv, 3, 3, 12, 12, hexc('#3a3e44'), hexc('#9aa0a8'))
        # pressure gauge
        for y in range(5, 10):
            for x in range(5, 10):
                if math.hypot(x - 7, y - 7) <= 2.3: cv.set(x, y, hexc('#e8e8e0'))
        cv.line(7, 7, 8 if on else 6, 5, hexc('#c02020'))
        cv.set(10, 6, hexc('#30e040') if on else hexc('#205020'))
        cv.set(10, 9, hexc('#e04030') if not on else hexc('#502020'))
    return cv

def glass_tex(tint=(220, 240, 255), alpha=70):
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, (*tint, alpha))
    for i in range(16):
        cv.set(i, 0, (255, 255, 255, 170)); cv.set(i, 15, (180, 200, 210, 170))
        cv.set(0, i, (255, 255, 255, 170)); cv.set(15, i, (180, 200, 210, 170))
    for i in range(2, 9):
        cv.set(2 + i // 3, 2 + i, (255, 255, 255, 150))
    cv.set(3, 3, (255, 255, 255, 200))
    return cv

def liquid_frames(base, hl, frames=8, alpha=200):
    """Animated liquid (16 x 16*frames) with rising bubbles."""
    cv = Canvas(16, 16 * frames)
    rnd = random.Random(7)
    b, h = hexc(base, alpha), hexc(hl, alpha)
    bubbles = [(rnd.randint(1, 14), rnd.randint(0, 15)) for _ in range(6)]
    for f in range(frames):
        for y in range(16):
            for x in range(16):
                wave = math.sin((x + f * 2) * 0.8) * 0.5
                c = mix(b, h, 0.25 + 0.25 * wave) if y > 1 else h
                cv.set(x, f * 16 + y, c)
        for bx, by in bubbles:
            yy = (by - f * 2) % 16
            cv.set(bx, f * 16 + yy, (230, 255, 230, 230))
            cv.set(bx + 1, f * 16 + yy, (200, 240, 200, 200))
    return cv

def extractor_cap():
    cv = metal(5, '#7a8088')
    bevel(cv, 0, 0, 15, 15, hexc('#a8aeb6'), hexc('#4a4e54'))
    for y in range(5, 11):
        for x in range(5, 11):
            if math.hypot(x - 7.5, y - 7.5) <= 3: cv.set(x, y, hexc('#4a4e54'))
            if math.hypot(x - 7.5, y - 7.5) <= 2: cv.set(x, y, hexc('#8a9098'))
    return cv

# ---------------- Lab ----------------
def lab_side():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#e4e6e8'))
    bevel(cv, 0, 0, 15, 15, hexc('#ffffff'), hexc('#a0a4a8'))
    for y in (5, 10):
        for x in range(1, 15): cv.set(x, y, hexc('#b0b4b8'))
    return cv

def lab_front():
    cv = lab_side()
    for (y0, y1) in ((1, 4), (6, 9), (11, 14)):
        bevel(cv, 1, y0, 14, y1, hexc('#ffffff'), hexc('#a0a4a8'))
        cv.rect(6, (y0 + y1) // 2, 9, (y0 + y1) // 2, hexc('#5a5e64'))
    cv.set(2, 2, hexc('#f0c040')); cv.set(3, 2, hexc('#1a1a1a'))  # Warnaufkleber
    return cv

def lab_top():
    cv = Canvas()
    rnd = random.Random(2)
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choice([hexc('#26282c'), hexc('#2c2e33'), hexc('#222428')]))
    for i in range(16):
        cv.set(i, 7, hexc('#3a3d42'))
    return cv

def burner():
    cv = metal(6, '#3a5a8a')
    for x in range(16):
        cv.set(x, 0, hexc('#8aa0c0')); cv.set(x, 15, hexc('#1a2a44'))
    return cv

# ---------------- Press ----------------
def press_base():
    cv = metal(7, '#4a4e56', 14)
    for y in range(10, 16):
        for x in range(16):
            cv.set(x, y, hexc('#e8c020') if ((x + y) // 3) % 2 == 0 else hexc('#1e1e1e'))
    bevel(cv, 0, 0, 15, 15, hexc('#7a808a'), hexc('#2a2e34'))
    rivets(cv, [(2, 3), (12, 3)])
    return cv

def press_column():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            c = [hexc('#c8ccd2'), hexc('#a8aeb6'), hexc('#8a9098'), hexc('#6a7078')][min(3, abs(x - 5) // 3)]
            cv.set(x, y, c)
    return cv

def press_plate():
    cv = metal(8, '#8a9098')
    bevel(cv, 0, 0, 15, 15, hexc('#c0c6ce'), hexc('#4a4e56'))
    for i in range(3, 13, 3):
        cv.line(i, 2, i, 13, hexc('#6a7078'))
    return cv

def press_top():
    cv = metal(9, '#3a5a2a', 10)
    bevel(cv, 0, 0, 15, 15, hexc('#5a8a42'), hexc('#1e3014'))
    rivets(cv, [(2, 2), (12, 2), (2, 12), (12, 12)])
    for y in range(5, 11):
        for x in range(5, 11):
            if math.hypot(x - 7.5, y - 7.5) <= 3: cv.set(x, y, hexc('#8a9098'))
    return cv

# ---------------- Grow tent ----------------
def tent_fabric(zipper=False):
    cv = Canvas()
    rnd = random.Random(11)
    for y in range(16):
        for x in range(16):
            c = rnd.choice([hexc('#1e1f22'), hexc('#222326'), hexc('#1a1b1e')])
            if (x + y) % 4 == 0: c = shade(c, 1.15)
            cv.set(x, y, c)
    for i in range(16):
        cv.set(i, 0, hexc('#34363b')); cv.set(0, i, hexc('#34363b'))
    if zipper:
        for y in range(16):
            cv.set(7, y, hexc('#55575c')); cv.set(8, y, hexc('#7a7c82') if y % 2 else hexc('#44464b'))
        cv.rect(7, 9, 8, 11, hexc('#a0a4aa'))
    return cv

def tent_mylar():
    cv = Canvas()
    rnd = random.Random(12)
    for y in range(16):
        for x in range(16):
            v = 170 + int(40 * math.sin((x + y) * 0.8)) + rnd.randint(-12, 12)
            if (x - y) % 8 == 0 or (x + y) % 8 == 0: v += 30
            v = max(0, min(255, v))
            cv.set(x, y, (v, v, min(255, v + 8), 255))
    return cv

def tent_frame():
    cv = metal(13, '#2e3034', 8)
    return cv

def tent_lamp(on=True):
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#2a2a2e'))
            if x % 3 == 1 and y % 3 == 1:
                cv.set(x, y, hexc('#ff6ad8') if on and (x + y) % 2 else hexc('#7a8aff') if on else hexc('#444444')) 
    return cv

def soil():
    cv = Canvas()
    rnd = random.Random(14)
    for y in range(16):
        for x in range(16):
            cv.set(x, y, rnd.choice([hexc('#3e2a1a'), hexc('#4a3220'), hexc('#35241a'), hexc('#5a4030')]))
    for _ in range(10):
        cv.set(rnd.randrange(16), rnd.randrange(16), hexc('#d8d4c8'))   # Perlit
    return cv

# ---------------- Grow lamp ----------------
def lamp_bottom():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#1e1e22'))
    for y in range(2, 14, 2):
        for x in range(2, 14, 2):
            c = [hexc('#ff4fd8'), hexc('#ff8ae6'), hexc('#8a6aff'), hexc('#ff4f6a')][(x // 2 + y // 2) % 4]
            cv.set(x, y, c)
    bevel(cv, 0, 0, 15, 15, hexc('#3a3a40'), hexc('#0e0e10'))
    return cv

def lamp_top():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#4a4e56') if x % 2 else hexc('#2a2e34'))
    bevel(cv, 0, 0, 15, 15, hexc('#6a6e76'), hexc('#1a1c20'))
    return cv

def lamp_side():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#2a2e34'))
    for x in range(16):
        cv.set(x, 0, hexc('#5a5e66')); cv.set(x, 15, hexc('#ff6ad8'))
    return cv

# ---------------- Bong ----------------
def bong_glass():
    cv = glass_tex((190, 235, 225), 80)
    for y in range(16):
        cv.set(12, y, (120, 220, 200, 120))
    return cv

def bong_water():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, (70, 150, 210, 150) if y > 0 else (180, 220, 255, 190))
    return cv

def bong_metal():
    return metal(15, '#9aa0a8', 12)

# ---------------- Precision scale ----------------
def scale_top():
    cv = metal(16, '#4a4e56', 6)
    cv.rect(1, 1, 14, 10, hexc('#b8c0c8'))
    bevel(cv, 1, 1, 14, 10, hexc('#e0e6ec'), hexc('#7a828c'))
    for i in range(3, 13, 2):
        cv.line(i, 2, i + 2, 9, hexc('#c8d0d8'))
    cv.rect(4, 12, 11, 14, hexc('#20301e'))
    for x, c in ((5, '#7ae07a'), (7, '#7ae07a'), (8, '#7ae07a'), (10, '#7ae07a')):
        cv.set(x, 13, hexc(c))
    cv.set(13, 13, hexc('#e04040'))
    return cv

def scale_side():
    cv = metal(17, '#3a3e46', 6)
    for x in range(16):
        cv.set(x, 0, hexc('#6a6e76'))
    return cv

# ---------------- Drying rack ----------------
def rope():
    cv = Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, hexc('#c8b07a') if (x + y) % 3 else hexc('#9a8456'))
    return cv

# ---------------- Particles ----------------
def smoke_frames():
    out = []
    for f in range(4):
        cv = Canvas(8, 8)
        r = 2.0 + f * 0.7
        rnd = random.Random(f)
        for y in range(8):
            for x in range(8):
                d = math.hypot(x + 0.5 - 4, y + 0.5 - 4)
                if d <= r:
                    a = int(255 * (1 - (d / (r + 0.01)) ** 2) * (0.9 - f * 0.12))
                    v = 235 - rnd.randint(0, 25)
                    cv.set(x, y, (v, v, v, max(0, a)))
        out.append(cv)
    return out

def sparkle_frames():
    out = []
    shapes = [
        ["..#..", ".###.", "#####", ".###.", "..#.."],
        ["#...#", ".#.#.", "..#..", ".#.#.", "#...#"],
        ["..#..", "..#..", "#####", "..#..", "..#.."],
        [".....", ".#.#.", "..#..", ".#.#.", "....."],
    ]
    for sh in shapes:
        cv = Canvas(8, 8)
        cv.ascii(sh, {'#': (255, 255, 255, 255)}, 1, 1)
        cv.set(3, 3, (255, 255, 255, 255))
        out.append(cv)
    return out

def vignette():
    cv = Canvas(256, 256)
    a = np.zeros((256, 256, 4), dtype=np.uint8)
    yy, xx = np.mgrid[0:256, 0:256]
    d = np.sqrt(((xx + 0.5 - 128) / 128) ** 2 + ((yy + 0.5 - 128) / 128) ** 2)
    alpha = np.clip((d - 0.45) / 0.75, 0, 1) ** 1.6
    a[:, :, 0:3] = 255
    a[:, :, 3] = (alpha * 255).astype(np.uint8)
    cv.a = a
    return cv

# ---------------- Effect icons (18x18) ----------------
def effect_icon(rows, pal):
    cv = Canvas(18, 18)
    cv.ascii(rows, pal, 1, 1)
    cv.outline(hexc('#1a1a1a'))
    return cv

def effect_icons():
    import tex_plants as P
    icons = {}
    leaf = P.fan_leaf()
    cv = Canvas(18, 18); cv.paste(leaf, 1, 1); cv.outline(hexc('#10260a')); icons['stoned'] = cv
    icons['munchies'] = effect_icon([
        "................",
        "....kkkkkkk.....",
        "...kCCCCCCCk....",
        "..kCCcCCCCCCk...",
        "..kCCCCCCcCCk...",
        "..kCcCCCCCCCk...",
        "..kCCCCcCCC.....",
        "..kCCCCCC.......",
        "..kCCcCC........",
        "..kCCCCCC.......",
        "..kCCCCCCC......",
        "..kCCCCcCCCk....",
        "...kCCCCCCk.....",
        "....kkkkkk......",
        "................",
        "................"], {'k': hexc('#6a3a1a'), 'C': hexc('#d49a5a'), 'c': hexc('#4a2a18')})
    icons['rush'] = effect_icon([
        "..........ww....",
        ".........wWw....",
        "........wWw.....",
        ".......wWw......",
        "......wWWwwww...",
        ".....wWWWWWw....",
        "....wwwwWWw.....",
        ".......wWw......",
        "......wWw.......",
        ".....wWw........",
        "....wWw.........",
        "...wWw..........",
        "...ww...........",
        "................",
        "................",
        "................"], {'w': hexc('#c8d8f0'), 'W': hexc('#ffffff')})
    icons['crash'] = effect_icon([
        "................",
        "................",
        "...ww......ww...",
        "..wWWw....wWWw..",
        "..wWWWw..wWWWw..",
        "...wWWWwwWWWw...",
        "....wWWWWWWw....",
        ".....wWWWWw.....",
        "......wWWw......",
        ".......ww.......",
        "................",
        "...bbbbbbbbbb...",
        "...bBBBBBBBBb...",
        "...bbbbbbbbbb...",
        "................",
        "................"], {'w': hexc('#5a5a66'), 'W': hexc('#8a8a96'), 'b': hexc('#3a3a46'), 'B': hexc('#c03030')})
    icons['sedated'] = effect_icon([
        "................",
        "...........zzz..",
        ".............z..",
        "............z...",
        "...........zzz..",
        "......zzzz......",
        "........z.......",
        ".......z........",
        "......zzzz......",
        "..rrrrrrrrrr....",
        ".rRRRRRRRRRRr...",
        ".rRRRRRRRRRRr...",
        "..rrrrrrrrrr....",
        "................",
        "................",
        "................"], {'z': hexc('#d0b8f0'), 'r': hexc('#5a2a6a'), 'R': hexc('#a868c0')})
    icons['trip'] = effect_icon([
        "................",
        ".....rrrrrr.....",
        "...rroooooorr...",
        "..rooyyyyyyoor..",
        ".royygggggyyor..",
        ".roygbbbbbgyor..",
        "royggb....bgyor.",
        "roygb......bgyo.",
        "roygb......bgyo.",
        "roygb..mm..bgyo.",
        ".roygb.mm.bgyor.",
        ".roygbbbbbbgyor.",
        "..royggggggyor..",
        "...rooyyyyoor...",
        ".....rrrrrr.....",
        "................"], {'r': hexc('#e03030'), 'o': hexc('#f08a2a'), 'y': hexc('#f0e040'), 'g': hexc('#40c040'),
                             'b': hexc('#4080f0'), 'm': hexc('#c040f0')})
    icons['acid'] = effect_icon([
        "................",
        "................",
        "................",
        "....wwwwwwww....",
        "..wwkkkkkkkkww..",
        ".wkkrrooyyggkkw.",
        "wkkrrobbbbyggkkw",
        "wkrrobbppbbyggkw",
        "wkrrobbppbbyggkw",
        "wkkrrobbbbyggkkw",
        ".wkkrrooyyggkkw.",
        "..wwkkkkkkkkww..",
        "....wwwwwwww....",
        "................",
        "................",
        "................"], {'w': hexc('#f0f0f0'), 'k': hexc('#40e0d0'), 'r': hexc('#e03030'), 'o': hexc('#f08a2a'),
                             'y': hexc('#f0e040'), 'g': hexc('#40c040'), 'b': hexc('#4060e0'), 'p': hexc('#1a1a2a')})
    icons['nicotine_buzz'] = effect_icon([
        "................",
        "..........s.....",
        ".........s......",
        "..........s.....",
        ".........s......",
        "................",
        "................",
        "..oooowwwwwwwer.",
        "..oOoowwwwwwwer.",
        "..ooOowwwwwwwer.",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"], {'o': hexc('#d98a3a'), 'O': hexc('#f2c48a'), 'w': hexc('#f4f1ea'), 'e': hexc('#8a8580'), 'r': hexc('#ff7a1a'),
                             's': hexc('#c0c0c0')})
    icons['withdrawal'] = effect_icon([
        "................",
        "....kkkkkkk.....",
        "...kSSSSSSSk....",
        "..kSSSSSSSSSk...",
        "..kSkkSSSkkSk...",
        "..kSSSSSSSSSk...",
        "..kSSSSSSSSSk...",
        "..kSSkkkkkSSk...",
        "..kSkSSSSSkSk...",
        "...kSSSSSSSk....",
        "....kkkkkkk..d..",
        "............ddd.",
        "............ddd.",
        "................",
        "................",
        "................"], {'k': hexc('#2a3a4a'), 'S': hexc('#9ab0c0'), 'd': hexc('#6ab0ff')})
    icons['overdose'] = effect_icon([
        "................",
        "....kkkkkkk.....",
        "...kWWWWWWWk....",
        "..kWWWWWWWWWk...",
        "..kWkkWWWkkWk...",
        "..kWkkWWWkkWk...",
        "..kWWWWkWWWWk...",
        "...kWWWWWWWk....",
        "....kWkWkWk.....",
        "....kkkkkkk.....",
        "..r.........r...",
        "...r.......r....",
        "....rrr.rrr.....",
        ".......r........",
        "....rrr.rrr.....",
        "...r.......r...."], {'k': hexc('#3a0a0a'), 'W': hexc('#f0e8e0'), 'r': hexc('#c01818')})
    return icons

# ---------------- GUI ----------------
GUI_BG = hexc('#c6c6c6'); GUI_L = hexc('#ffffff'); GUI_D = hexc('#555555'); GUI_K = hexc('#000000')
SLOT_BG = hexc('#8b8b8b'); SLOT_D = hexc('#373737')

def gui_panel(w=176, h=166):
    cv = Canvas(256, 256)
    cv.rect(2, 2, w - 3, h - 3, GUI_BG)
    for x in range(2, w - 2):
        cv.set(x, 0, GUI_K); cv.set(x, 1, GUI_L); cv.set(x, 2, GUI_L)
        cv.set(x, h - 1, GUI_K); cv.set(x, h - 2, GUI_D); cv.set(x, h - 3, GUI_D)
    for y in range(2, h - 2):
        cv.set(0, y, GUI_K); cv.set(1, y, GUI_L); cv.set(2, y, GUI_L)
        cv.set(w - 1, y, GUI_K); cv.set(w - 2, y, GUI_D); cv.set(w - 3, y, GUI_D)
    cv.set(1, 1, GUI_K); cv.set(w - 2, h - 2, GUI_K); cv.set(w - 2, 1, GUI_K); cv.set(1, h - 2, GUI_K)
    cv.set(3, 3, GUI_L); cv.set(w - 4, h - 4, GUI_D)
    return cv

def slot(cv, x, y, big=False):
    """x,y = item position (as in the menu); 18x18 slot frame around (x-1,y-1)."""
    s = 26 if big else 18
    o = 5 if big else 1
    x0, y0 = x - o, y - o
    cv.rect(x0, y0, x0 + s - 1, y0 + s - 1, SLOT_BG)
    for i in range(s):
        cv.set(x0 + i, y0, SLOT_D); cv.set(x0, y0 + i, SLOT_D)
        cv.set(x0 + i, y0 + s - 1, GUI_L); cv.set(x0 + s - 1, y0 + i, GUI_L)
    cv.set(x0 + s - 1, y0, SLOT_BG); cv.set(x0, y0 + s - 1, SLOT_BG)

def arrow(cv, x, y, fill=None, border=hexc('#8b8b8b')):
    rows = [
        "..............#.........",
        "..............##........",
        "..............###.......",
        "..............####......",
        "..............#####.....",
        "#######################.",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "#######################.",
        "..............#####.....",
        "..............####......",
        "..............###.......",
        "..............##........",
        "..............#.........",
    ]
    cv.ascii(rows, {'#': fill or border}, x, y)

def flame(cv, x, y, fill):
    rows = [
        "......#.......",
        ".....##.......",
        ".....###......",
        "....####..#...",
        "....#####.##..",
        "...#########..",
        "..##########..",
        "..###########.",
        ".############.",
        ".#############",
        "##############",
        "##############",
        ".############.",
        "..##########..",
    ]
    cv.ascii(rows, {'#': fill}, x, y)

def bar_frame(cv, x, y, w=10, h=52):
    cv.rect(x - 1, y - 1, x + w, y + h, SLOT_D)
    cv.rect(x, y, x + w - 1, y + h - 1, hexc('#2a2a2a'))
    for i in range(w + 2):
        cv.set(x - 1 + i, y + h, GUI_L)
    for i in range(h + 2):
        cv.set(x + w, y - 1 + i, GUI_L)
    for yy in range(y + 4, y + h, 6):
        cv.set(x, yy, hexc('#444444')); cv.set(x + w - 1, yy, hexc('#444444'))

def gui_texture(machine):
    import json
    cv = gui_panel()
    L = MACHINE_LAYOUT[machine]
    for (x, y) in L['inputs']:
        slot(cv, x, y)
    if L.get('fuel'):
        slot(cv, *L['fuel'])
    for (x, y) in L['outputs']:
        slot(cv, x, y, big=(len(L['outputs']) == 1))
    arrow(cv, *L['arrow'])
    if L.get('flame'):
        if L.get('coolant'):
            snowflake(cv, L['flame'][0], L['flame'][1], hexc('#8b8b8b'))
        else:
            flame(cv, L['flame'][0], L['flame'][1], hexc('#8b8b8b'))
    bar_frame(cv, 8, 17)
    if machine == 'grow_tent':
        bar_frame(cv, 24, 17)
        # icons next to the slots
        cv.ascii(["..g..", ".ggg.", "ggggg", "..b..", "..b.."], {'g': hexc('#4f9a2e'), 'b': hexc('#7a5a2a')}, 37, 22)
        cv.ascii([".w.", "www", "www"], {'w': hexc('#3a6ab0')}, 38, 58)
    # player inventory
    for row in range(3):
        for col in range(9):
            slot(cv, 8 + col * 18, 84 + row * 18)
    for col in range(9):
        slot(cv, 8 + col * 18, 142)
    # sprites on the right: flame (176,0), arrow (176,14), energy (176,31), water (186,31)
    if L.get('coolant'):
        snowflake(cv, 176, 0, hexc('#7fd0ff'))
        for (x, y) in ((182, 6), (183, 6), (182, 5)):
            cv.set(x, y, hexc('#ffffff'))
    else:
        flame(cv, 176, 0, hexc('#ff9a2a'))
        for (x, y) in ((179, 3), (180, 6), (182, 8), (183, 10), (181, 11)):
            cv.set(x, y, hexc('#ffe070'))
    arrow(cv, 176, 14, fill=hexc('#ffffff'))
    for yy in range(52):
        t = yy / 51
        c = mix(hexc('#ff3a2a'), hexc('#ffd040'), t)
        for xx in range(10):
            cv.set(176 + xx, 31 + yy, shade(c, 1.15) if xx < 3 else c if xx < 8 else shade(c, 0.8))
        cw = mix(hexc('#2a7ad8'), hexc('#5ab0ff'), t)
        for xx in range(10):
            cv.set(186 + xx, 31 + yy, shade(cw, 1.15) if xx < 3 else cw if xx < 8 else shade(cw, 0.8))
    return cv

MACHINE_LAYOUT = {
    'grinder': dict(inputs=[(56, 17)], fuel=(56, 53), outputs=[(116, 35)], arrow=(79, 34), flame=(57, 37)),
    'extractor': dict(inputs=[(38, 17), (62, 17)], fuel=(50, 53), outputs=[(116, 35)], arrow=(82, 34), flame=(51, 37)),
    'lab_station': dict(inputs=[(30, 17), (48, 17), (66, 17)], fuel=(48, 53), outputs=[(124, 35)], arrow=(90, 34), flame=(49, 37)),
    'press': dict(inputs=[(56, 17)], fuel=(56, 53), outputs=[(116, 35)], arrow=(79, 34), flame=(57, 37)),
    'grow_tent': dict(inputs=[(44, 17), (44, 35), (44, 53)], fuel=None, outputs=[(98, 26), (116, 26), (98, 44), (116, 44)], arrow=(66, 35), flame=None),
    'chemical_reactor': dict(inputs=[(30, 17), (48, 17), (66, 17)], fuel=(48, 53), outputs=[(124, 35)], arrow=(90, 34), flame=(49, 37)),
    'crystallizer': dict(inputs=[(56, 17)], fuel=(56, 53), outputs=[(116, 35)], arrow=(79, 34), flame=(57, 37), coolant=True),
    'pill_press': dict(inputs=[(30, 17), (48, 17), (66, 17)], fuel=(48, 53), outputs=[(124, 35)], arrow=(90, 34), flame=(49, 37)),
}

SNOWFLAKE = [
    "......#.......",
    "...#..#..#....",
    "....#.#.#.....",
    "..#..###..#...",
    "...#.###.#....",
    "#..#######..#.",
    "##############",
    "#..#######..#.",
    "...#.###.#....",
    "..#..###..#...",
    "....#.#.#.....",
    "...#..#..#....",
    "......#.......",
    "..............",
]

def snowflake(cv, x, y, fill):
    cv.ascii(SNOWFLAKE, {'#': fill}, x, y)

# ---------------- Villager professions (64x64 overlay) ----------------
def box_uv(cv, u, v, w, h, d, faces):
    """Paints the UV faces of a box (Minecraft layout). faces: dict name->callable(cv,x0,y0,w,h) or colour."""
    rects = {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h),
    }
    for name, fn in faces.items():
        x0, y0, ww, hh = rects[name]
        if callable(fn):
            fn(cv, x0, y0, ww, hh)
        else:
            cv.rect(x0, y0, x0 + ww - 1, y0 + hh - 1, fn)

def noisy(base, seed, var=0.12):
    rnd = random.Random(seed)
    b = hexc(base)
    def f(cv, x0, y0, w, h):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                k = 1 + (rnd.random() - 0.5) * var * 2
                cv.set(x, y, shade(b, k) if k < 1 else mix(b, (255, 255, 255, 255), (k - 1) * 0.5))
    return f

def dealer_villager(zombie=False):
    cv = Canvas(64, 64)
    hood = '#2a2c30'
    # hood over the head (hat layer 32,0; 8x10x8)
    box_uv(cv, 32, 0, 8, 10, 8, {'top': noisy(hood, 1), 'right': noisy(hood, 2), 'left': noisy(hood, 3), 'back': noisy(hood, 4)})
    def hood_front(cv, x0, y0, w, h):
        noisy(hood, 5)(cv, x0, y0, w, h)
        cv.rect(x0 + 1, y0 + 2, x0 + w - 2, y0 + h - 1, (0, 0, 0, 0))
        cv.a[y0 + 2:y0 + h, x0 + 1:x0 + w - 1] = 0
    box_uv(cv, 32, 0, 8, 10, 8, {'front': hood_front})
    # sunglasses on the face (head 0,0: front 8..15 x 8..17)
    for x in range(8, 16):
        cv.set(x, 12, hexc('#111111'))
    for x in (9, 10, 13, 14):
        cv.set(x, 13, hexc('#1a1a2a'))
    cv.set(9, 12, hexc('#3a3a5a')); cv.set(13, 12, hexc('#3a3a5a'))
    # hoodie: body (16,20; 8x12x6) and jacket (0,38; 8x20x6)
    box_uv(cv, 16, 20, 8, 12, 6, {k: noisy(hood, 10 + i) for i, k in enumerate(['top', 'bottom', 'right', 'front', 'left', 'back'])})
    box_uv(cv, 0, 38, 8, 20, 6, {k: noisy('#25272b', 20 + i) for i, k in enumerate(['top', 'right', 'front', 'left', 'back'])})
    # gold chain on the front
    for (x, y) in ((22 + 1, 26), (22 + 2, 27), (22 + 3, 28), (22 + 4, 28), (22 + 5, 27), (22 + 6, 26)):
        cv.set(x, y, hexc('#e8c040'))
    cv.set(26, 29, hexc('#f8e070'))
    for (x, y) in ((7, 45), (8, 46), (9, 47), (10, 47), (11, 46), (12, 45)):
        cv.set(x, y, hexc('#e8c040'))
    # belt bag
    cv.rect(7, 52, 12, 55, hexc('#1e2024'))
    # arms (44,22; 4x8x4) and crossed arms (40,38; 8x4x4)
    box_uv(cv, 44, 22, 4, 8, 4, {k: noisy(hood, 30 + i) for i, k in enumerate(['top', 'bottom', 'right', 'front', 'left', 'back'])})
    box_uv(cv, 40, 38, 8, 4, 4, {k: noisy(hood, 40 + i) for i, k in enumerate(['top', 'bottom', 'right', 'front', 'left', 'back'])})
    return cv

def grower_villager():
    cv = Canvas(64, 64)
    straw, straw_d = '#d8b860', '#a88838'
    # straw hat: crown (32,0) top part only, brim (30,47; 16x16x1)
    def crown(cv, x0, y0, w, h):
        for y in range(y0, y0 + 3):
            for x in range(x0, x0 + w):
                cv.set(x, y, hexc(straw) if (x + y) % 2 else hexc(straw_d))
        for x in range(x0, x0 + w):
            cv.set(x, y0 + 3, hexc('#3a7a2a'))  # green hat band
    box_uv(cv, 32, 0, 8, 10, 8, {'top': noisy(straw, 2), 'front': crown, 'right': crown, 'left': crown, 'back': crown})
    def brim(cv, x0, y0, w, h):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                dx, dy = x - x0 - 7.5, y - y0 - 7.5
                if math.hypot(dx, dy) <= 7.8:
                    cv.set(x, y, hexc(straw) if (x * 3 + y) % 4 else hexc(straw_d))
    box_uv(cv, 30, 47, 16, 16, 1, {'front': brim, 'back': brim})
    # green bib apron over body/robe
    apron = '#3f7a32'
    def apron_front(cv, x0, y0, w, h):
        noisy(apron, 7)(cv, x0 + 1, y0, w - 2, h)
        cv.rect(x0 + 2, y0 + 3, x0 + w - 3, y0 + 5, hexc('#356a2a'))   # Tasche
        cv.set(x0 + 3, y0 + 4, hexc('#e8e0c0'))                          # Etikett
    box_uv(cv, 0, 38, 8, 20, 6, {'front': apron_front})
    box_uv(cv, 16, 20, 8, 12, 6, {'front': lambda cv, x0, y0, w, h: noisy(apron, 8)(cv, x0 + 1, y0 + 2, w - 2, h - 2)})
    # gloves on the arms
    box_uv(cv, 44, 22, 4, 8, 4, {'bottom': hexc('#8a6a3a')})
    box_uv(cv, 40, 38, 8, 4, 4, {'right': hexc('#8a6a3a'), 'left': hexc('#8a6a3a')})
    return cv

if __name__ == '__main__':
    items = [(n, f()) for n, f in [('grinder_side', grinder_side), ('grinder_front', grinder_front), ('grinder_on', lambda: grinder_front(True)),
             ('millstone', millstone), ('millstone_top', millstone_top), ('extr_base', extractor_base), ('extr_front', lambda: extractor_base(True)),
             ('extr_cap', extractor_cap), ('glass', glass_tex), ('lab_side', lab_side), ('lab_front', lab_front), ('lab_top', lab_top),
             ('burner', burner), ('press_base', press_base), ('press_col', press_column), ('press_plate', press_plate), ('press_top', press_top),
             ('tent', tent_fabric), ('tent_zip', lambda: tent_fabric(True)), ('mylar', tent_mylar), ('frame', tent_frame), ('tlamp', tent_lamp),
             ('soil', soil), ('lamp_b', lamp_bottom), ('lamp_t', lamp_top), ('lamp_s', lamp_side), ('bong_glass', bong_glass), ('bong_w', bong_water),
             ('scale_top', scale_top), ('scale_side', scale_side), ('rope', rope)]]
    items += [(k, v) for k, v in effect_icons().items()]
    items += [(f'smoke{i}', c) for i, c in enumerate(smoke_frames())] + [(f'spark{i}', c) for i, c in enumerate(sparkle_frames())]
    contact_sheet(items, scale=5, cols=10, out='/tmp/claude-0/-home-user/91e744b7-49a3-5ae1-9eec-1733811b2a18/scratchpad/blocks.png')
    big = [('gui_ext', gui_texture('extractor')), ('gui_tent', gui_texture('grow_tent')), ('dealer', dealer_villager()), ('grower', grower_villager())]
    contact_sheet(big, scale=2, cols=4, out='/tmp/claude-0/-home-user/91e744b7-49a3-5ae1-9eec-1733811b2a18/scratchpad/gui.png')
