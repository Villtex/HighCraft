"""Small pixel-art library for the HighCraft textures (16x16 etc.)."""
import math, random
import numpy as np
from PIL import Image

def hexc(s, a=255):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)

def mix(c1, c2, t):
    return tuple(int(round(c1[i] * (1 - t) + c2[i] * t)) for i in range(4))

def shade(c, f):
    """f<1 darker, f>1 lighter"""
    if f >= 1:
        return mix(c, (255, 255, 255, c[3]), min(1, f - 1))
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3])

class Canvas:
    def __init__(self, w=16, h=16, fill=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.a = np.zeros((h, w, 4), dtype=np.uint8)
        self.a[:, :] = fill

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return tuple(int(v) for v in self.a[y, x])
        return (0, 0, 0, 0)

    def set(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            if len(c) == 3:
                c = (*c, 255)
            if c[3] == 255 or self.a[y, x, 3] == 0:
                self.a[y, x] = c
            else:
                base = self.get(x, y)
                t = c[3] / 255
                self.a[y, x] = (*[int(base[i] * (1 - t) + c[i] * t) for i in range(3)], max(base[3], c[3]))

    def filled(self, x, y):
        return self.get(x, y)[3] > 0

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def line(self, x0, y0, x1, y1, c):
        n = int(max(abs(x1 - x0), abs(y1 - y0))) + 1
        for i in range(n + 1):
            t = i / max(1, n)
            self.set(round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), c)

    def ascii(self, rows, pal, ox=0, oy=0):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in pal and pal[ch] is not None:
                    self.set(ox + x, oy + y, pal[ch])

    def outline(self, c, only_empty=True):
        """Draws an outline around all filled pixels (outside)."""
        mask = self.a[:, :, 3] > 0
        out = []
        for y in range(self.h):
            for x in range(self.w):
                if not mask[y, x]:
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        xx, yy = x + dx, y + dy
                        if 0 <= xx < self.w and 0 <= yy < self.h and mask[yy, xx]:
                            out.append((x, y)); break
        for x, y in out:
            self.set(x, y, c)

    def edge_darken(self, f=0.7, light_dir=(-1, -1), hl=1.15):
        """Pixels with an empty neighbour: bottom/right darker, top/left lighter (volume)."""
        src = self.a.copy()
        mask = src[:, :, 3] > 0
        for y in range(self.h):
            for x in range(self.w):
                if not mask[y, x]:
                    continue
                c = tuple(int(v) for v in src[y, x])
                dark = light = False
                for dx, dy in ((1, 0), (0, 1)):
                    xx, yy = x + dx, y + dy
                    if not (0 <= xx < self.w and 0 <= yy < self.h) or not mask[yy, xx]:
                        dark = True
                for dx, dy in ((-1, 0), (0, -1)):
                    xx, yy = x + dx, y + dy
                    if not (0 <= xx < self.w and 0 <= yy < self.h) or not mask[yy, xx]:
                        light = True
                if dark:
                    self.a[y, x] = shade(c, f)
                elif light:
                    self.a[y, x] = shade(c, hl)

    def paste(self, other, ox=0, oy=0):
        for y in range(other.h):
            for x in range(other.w):
                c = other.get(x, y)
                if c[3] > 0:
                    self.set(ox + x, oy + y, c)

    def flip_h(self):
        self.a = self.a[:, ::-1].copy(); return self

    def image(self):
        return Image.fromarray(self.a, 'RGBA')

    def save(self, path):
        import os
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.image().save(path)

def leaflet(cv, bx, by, angle_deg, length, width, col_dark, col_mid, col_light, serrate=True, vein=None):
    """Tapering leaflet from (bx,by) towards angle (0 = up, clockwise)."""
    ang = math.radians(angle_deg)
    ax, ay = math.sin(ang), -math.cos(ang)       # axis
    px_, py_ = -ay, ax                            # perpendicular
    pts = []
    for y in range(cv.h):
        for x in range(cv.w):
            dx, dy = x + 0.5 - bx, y + 0.5 - by
            t = dx * ax + dy * ay
            d = dx * px_ + dy * py_
            if t < -0.3 or t > length:
                continue
            u = max(0.0, t / length)
            prof = math.sin(math.pi * min(1, u * 0.95 + 0.05)) ** 0.9
            if u > 0.55:
                prof *= 1 - (u - 0.55) * 0.9
            w = width * 0.5 * prof
            if serrate and int(t * 1.6) % 2 == 1:
                w *= 0.82
            if abs(d) <= w + 0.15:
                side = d * (1 if angle_deg % 360 < 180 else -1)
                pts.append((x, y, side, u, abs(d) / max(0.01, w + 0.15)))
    for x, y, side, u, rel in pts:
        c = col_mid if side >= 0 else col_dark
        if rel > 0.8:
            c = shade(c, 0.85)
        cv.set(x, y, c)
    # midrib
    if vein is not None:
        for i in range(int(length * 0.85)):
            cv.set(round(bx + ax * (i + 0.5) - 0.5), round(by + ay * (i + 0.5) - 0.5), vein)
    return cv

def noise_fill(cv, x0, y0, x1, y1, cols, seed=0, weights=None):
    rnd = random.Random(seed)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            cv.set(x, y, rnd.choices(cols, weights=weights)[0])

def contact_sheet(items, scale=8, cols=8, bg=(60, 60, 70, 255), out='sheet.png'):
    """items: list of (name, Canvas|Image)"""
    from PIL import ImageDraw
    imgs = [(n, c.image() if isinstance(c, Canvas) else c) for n, c in items]
    cw = max(i.width for _, i in imgs) * scale + 8
    ch = max(i.height for _, i in imgs) * scale + 20
    rows = (len(imgs) + cols - 1) // cols
    sheet = Image.new('RGBA', (cw * cols, ch * rows), bg)
    d = ImageDraw.Draw(sheet)
    for k, (n, im) in enumerate(imgs):
        x, y = (k % cols) * cw + 4, (k // cols) * ch + 4
        # checkerboard
        big = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
        checker = Image.new('RGBA', big.size, (90, 90, 100, 255))
        cd = ImageDraw.Draw(checker)
        for yy in range(0, big.height, scale * 2):
            for xx in range(0, big.width, scale * 2):
                cd.rectangle([xx, yy, xx + scale - 1, yy + scale - 1], fill=(80, 80, 90, 255))
                cd.rectangle([xx + scale, yy + scale, xx + 2 * scale - 1, yy + 2 * scale - 1], fill=(80, 80, 90, 255))
        checker.alpha_composite(big)
        sheet.paste(checker, (x, y))
        d.text((x, y + big.height + 2), n[:22], fill=(230, 230, 230, 255))
    sheet.save(out)
    return out
