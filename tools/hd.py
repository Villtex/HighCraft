"""HD pixel-art renderer for 32x32 item icons.

Shapes are described as signed distance fields (SDF). Every shape is shaded with a fixed
top-left light, quantised onto a hue-shifted colour ramp and outlined. This gives all icons
the same light direction, outline style and palette logic.
"""
import math, colorsys
import numpy as np
from PIL import Image

N = 32
_ys, _xs = np.mgrid[0:N, 0:N].astype(np.float64)
X = _xs + 0.5
Y = _ys + 0.5

LIGHT = np.array([-0.55, -0.75, 0.62])
LIGHT = LIGHT / np.linalg.norm(LIGHT)
HALF = LIGHT + np.array([0, 0, 1.0])
HALF = HALF / np.linalg.norm(HALF)

# ---------------------------------------------------------------- colours
def hx(s, a=255):
    s = s.lstrip('#')
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)

def _toward(h, target, amt):
    d = (target - h + 0.5) % 1.0 - 0.5
    return (h + d * amt) % 1.0

def hls(c):
    return colorsys.rgb_to_hls(c[0] / 255, c[1] / 255, c[2] / 255)

def from_hls(h, l, s, a=255):
    r, g, b = colorsys.hls_to_rgb(h % 1.0, min(1, max(0, l)), min(1, max(0, s)))
    return (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)), a)

def darker(c, f=0.6, hue=0.04):
    h, l, s = hls(c)
    return from_hls(_toward(h, 0.72, hue), l * f, min(1, s * 1.08 + 0.04), c[3] if len(c) > 3 else 255)

def lighter(c, f=0.3, hue=0.04):
    h, l, s = hls(c)
    return from_hls(_toward(h, 0.14, hue), l + (1 - l) * f, s * 0.95, c[3] if len(c) > 3 else 255)

def ramp(base, n=7, dark=0.80, light=0.48, shift=0.07, sat_dark=0.10, sat_light=-0.12, alpha=255):
    """Colour ramp dark -> light around a base colour; shadows drift towards violet,
    highlights towards warm yellow (classic pixel-art hue shifting)."""
    base = hx(base) if isinstance(base, str) else base
    h, l, s = hls(base)
    mid = (n - 1) * 0.55
    out = []
    for i in range(n):
        if i <= mid:
            t = (mid - i) / mid
            L = l - l * dark * t
            H = _toward(h, 0.72, shift * t)
            S = s + sat_dark * t
            if s < 0.08:
                H, S = 0.64, s + 0.10 * t
        else:
            t = (i - mid) / (n - 1 - mid)
            L = l + (1 - l) * light * t
            H = _toward(h, 0.14, shift * t)
            S = s + sat_light * t
        out.append(from_hls(H, L, S, alpha))
    return out

class Mat:
    def __init__(self, base, n=7, outline=None, **kw):
        self.cols = ramp(base, n, **kw)
        self.n = n
        self.out = outline if outline is not None else darker(self.cols[0], 0.62)
        self.out = hx(self.out) if isinstance(self.out, str) else self.out

    def __getitem__(self, i):
        return self.cols[max(0, min(self.n - 1, i))]

# ---------------------------------------------------------------- SDF primitives
def circle(cx, cy, r):
    return lambda x, y: np.hypot(x - cx, y - cy) - r

def _rot(x, y, cx, cy, ang):
    a = math.radians(ang)
    ca, sa = math.cos(a), math.sin(a)
    dx, dy = x - cx, y - cy
    return dx * ca + dy * sa, -dx * sa + dy * ca

def ellipse(cx, cy, rx, ry, ang=0):
    def f(x, y):
        px, py = _rot(x, y, cx, cy, ang)
        k = np.hypot(px / rx, py / ry)
        return (k - 1) * min(rx, ry)
    return f

def rbox(cx, cy, hw, hh, r=0.0, ang=0):
    def f(x, y):
        px, py = _rot(x, y, cx, cy, ang)
        qx, qy = np.abs(px) - (hw - r), np.abs(py) - (hh - r)
        return np.hypot(np.maximum(qx, 0), np.maximum(qy, 0)) + np.minimum(np.maximum(qx, qy), 0) - r
    return f

def capsule(ax, ay, bx, by, ra, rb=None):
    rb = ra if rb is None else rb
    def f(x, y):
        pax, pay = x - ax, y - ay
        bax, bay = bx - ax, by - ay
        t = np.clip((pax * bax + pay * bay) / (bax * bax + bay * bay), 0, 1)
        return np.hypot(pax - bax * t, pay - bay * t) - (ra + (rb - ra) * t)
    return f

def poly(pts):
    pts = [(float(a), float(b)) for a, b in pts]
    def f(x, y):
        d = np.full(x.shape, 1e9)
        s = np.ones(x.shape)
        n = len(pts)
        for i in range(n):
            (ax, ay), (bx, by) = pts[i], pts[i - 1]
            ex, ey = bx - ax, by - ay
            wx, wy = x - ax, y - ay
            t = np.clip((wx * ex + wy * ey) / (ex * ex + ey * ey + 1e-9), 0, 1)
            d = np.minimum(d, (wx - ex * t) ** 2 + (wy - ey * t) ** 2)
            c1 = y >= ay
            c2 = y < by
            c3 = ex * wy > ey * wx
            flip = (c1 & c2 & c3) | (~c1 & ~c2 & ~c3)
            s = np.where(flip, -s, s)
        return s * np.sqrt(d)
    return f

def union(*fs):
    return lambda x, y: np.minimum.reduce([f(x, y) for f in fs])

def smooth(k, *fs):
    def f(x, y):
        d = fs[0](x, y)
        for g in fs[1:]:
            d2 = g(x, y)
            h = np.clip(0.5 + 0.5 * (d2 - d) / k, 0, 1)
            d = d2 * (1 - h) + d * h - k * h * (1 - h)
        return d
    return f

def inter(a, b):
    return lambda x, y: np.maximum(a(x, y), b(x, y))

def sub(a, b):
    return lambda x, y: np.maximum(a(x, y), -b(x, y))

def grow(f, d):
    return lambda x, y: f(x, y) - d

def move(f, dx, dy):
    return lambda x, y: f(x - dx, y - dy)

def warp(f, amp, scale, seed):
    nx = noise(scale, seed, full=True)
    ny = noise(scale, seed + 77, full=True)
    def g(x, y):
        ix = np.clip(x.astype(int), 0, N - 1) if isinstance(x, np.ndarray) else min(N - 1, max(0, int(x)))
        iy = np.clip(y.astype(int), 0, N - 1) if isinstance(y, np.ndarray) else min(N - 1, max(0, int(y)))
        return f(x + (nx[iy, ix] - 0.5) * 2 * amp, y + (ny[iy, ix] - 0.5) * 2 * amp)
    return g

def leaf(bx, by, ang, length, width, serr=0.0, teeth=7, power=0.85, curve=0.0, tip=1.0, base=0.0, steps=40):
    """Leaf outline as polygon: base (bx,by), angle in degrees (0 = up, clockwise)."""
    a = math.radians(ang)
    ax, ay = math.sin(a), -math.cos(a)
    px, py = -ay, ax
    left, right = [], []
    for i in range(steps + 1):
        u = i / steps
        w = width * 0.5 * (math.sin(math.pi * min(1.0, u ** power)) ** tip)
        w = max(w, base * (1 - u) * 0.5)
        if serr:
            fr = (u * teeth) % 1.0
            w *= 1 - serr * fr * (0.3 + 0.7 * math.sin(math.pi * u))
        c = curve * math.sin(math.pi * u) * length * 0.15
        cx, cy = bx + ax * u * length + px * c, by + ay * u * length + py * c
        left.append((cx + px * w, cy + py * w))
        right.append((cx - px * w, cy - py * w))
    return poly(left + right[::-1])

# ---------------------------------------------------------------- noise / textures
def noise(scale, seed, full=False):
    rng = np.random.default_rng(seed)
    g = rng.random((int(N / scale) + 3, int(N / scale) + 3))
    gx, gy = _xs / scale, _ys / scale
    x0, y0 = np.floor(gx).astype(int), np.floor(gy).astype(int)
    fx, fy = gx - x0, gy - y0
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a = g[y0, x0] * (1 - fx) + g[y0, x0 + 1] * fx
    b = g[y0 + 1, x0] * (1 - fx) + g[y0 + 1, x0 + 1] * fx
    return a * (1 - fy) + b * fy

def grain(seed):
    return np.random.default_rng(seed).random((N, N))

def stripes(ang, period, width=0.5, phase=0.0):
    a = math.radians(ang)
    t = (X * math.cos(a) + Y * math.sin(a)) / period + phase
    return ((t % 1.0) < width).astype(float)

# ---------------------------------------------------------------- icon canvas
def _dil4(m):
    o = m.copy()
    o[1:, :] |= m[:-1, :]; o[:-1, :] |= m[1:, :]
    o[:, 1:] |= m[:, :-1]; o[:, :-1] |= m[:, 1:]
    return o

class Icon:
    def __init__(self):
        self.a = np.zeros((N, N, 4), dtype=np.float64)

    # -- low level
    def filled(self):
        return self.a[:, :, 3] > 0

    def put(self, mask, col, alpha=None):
        col = np.array(col, dtype=np.float64)
        if col.ndim == 1:
            col = np.broadcast_to(col, (N, N, 4))
        al = col[:, :, 3:4] / 255.0 if alpha is None else np.full((N, N, 1), alpha / 255.0)
        m = mask[:, :, None]
        under = self.a
        rgb = np.where(under[:, :, 3:4] > 0, under[:, :, :3] * (1 - al) + col[:, :, :3] * al, col[:, :, :3])
        a_new = np.maximum(under[:, :, 3:4], al * 255)
        new = np.concatenate([rgb, a_new], axis=2)
        self.a = np.where(m, new, self.a)

    def px(self, x, y, col):
        x, y = int(x), int(y)
        if 0 <= x < N and 0 <= y < N:
            col = hx(col) if isinstance(col, str) else col
            m = np.zeros((N, N), bool); m[y, x] = True
            self.put(m, col)

    def line(self, x0, y0, x1, y1, col):
        n = int(max(abs(x1 - x0), abs(y1 - y0))) + 1
        for i in range(n + 1):
            t = i / max(1, n)
            self.px(round(x0 + (x1 - x0) * t - 0.5), round(y0 + (y1 - y0) * t - 0.5), col)

    def get(self, x, y):
        return tuple(int(v) for v in self.a[int(y), int(x)])

    # -- shaded shape
    def shape(self, sdf, mat, r=3.0, amb=0.22, dif=0.72, spec=0.0, gloss=18, tone=0.0, tex=None, tex_amt=0.0,
              outline=True, shadow=0.7, alpha=None, clip=None, flat=False, light=None, out_alpha=255, inner=None):
        """Draws a shaded shape. r = rounding radius of the height profile (bevel width)."""
        d = sdf(X, Y)
        mask = d <= 0.0
        if clip is not None:
            mask &= clip(X, Y) <= 0.0
        if not mask.any():
            return mask
        L = LIGHT if light is None else light
        if flat:
            nx = np.zeros((N, N)); ny = np.zeros((N, N)); nz = np.ones((N, N))
        else:
            e = 0.5
            def h(xx, yy):
                t = np.clip(-sdf(xx, yy) / r, 0, 1)
                return r * np.sqrt(1 - (1 - t) ** 2)
            nx = -(h(X + e, Y) - h(X - e, Y)) / (2 * e)
            ny = -(h(X, Y + e) - h(X, Y - e)) / (2 * e)
            nz = np.ones((N, N))
            ln = np.sqrt(nx * nx + ny * ny + nz * nz)
            nx, ny, nz = nx / ln, ny / ln, nz / ln
        lam = np.maximum(nx * L[0] + ny * L[1] + nz * L[2], 0)
        I = amb + dif * lam + tone
        if spec:
            I = I + spec * np.maximum(nx * HALF[0] + ny * HALF[1] + nz * HALF[2], 0) ** gloss
        if tex is not None:
            I = I + (tex - 0.5) * tex_amt
        idx = np.clip(np.round(I * (mat.n - 1)), 0, mat.n - 1).astype(int)
        cols = np.array(mat.cols, dtype=np.float64)[idx]
        if outline:
            ring = _dil4(mask) & ~mask
            if clip is not None:
                ring &= _dil4(clip(X, Y) <= 0.0)
            empty = ~self.filled()
            # top-left of the shape: slightly lighter outline (selective outline)
            below = np.zeros((N, N), bool); below[:-1, :] = mask[1:, :]
            right = np.zeros((N, N), bool); right[:, :-1] = mask[:, 1:]
            lit = below | right
            oc = np.array(mat.out, dtype=np.float64)
            oc_l = np.array(mat.cols[0], dtype=np.float64)
            self.put(ring & empty & ~lit, (*oc[:3], out_alpha))
            self.put(ring & empty & lit, (*oc_l[:3], out_alpha))
            if shadow:
                sh = ring & ~empty
                if sh.any():
                    cur = self.a.copy()
                    dark = cur.copy()
                    for yy, xx in zip(*np.nonzero(sh)):
                        dark[yy, xx] = darker(tuple(cur[yy, xx]), shadow)
                    self.a = np.where(sh[:, :, None], dark, self.a)
        if inner is not None:
            # inner edge line (e.g. rim): pixels of the shape bordering outside
            edge = mask & ~(_erode4(mask))
            self.put(edge & inner[0](X, Y), inner[1])
        self.put(mask, cols, alpha)
        return mask

    def mask_of(self, sdf):
        return sdf(X, Y) <= 0

    def tint(self, mask, f):
        """Darkens (f<1) or brightens (f>1) existing pixels inside mask."""
        cur = self.a.copy()
        for yy, xx in zip(*np.nonzero(mask & self.filled())):
            c = tuple(cur[yy, xx])
            cur[yy, xx] = darker(c, f) if f < 1 else lighter(c, f - 1)
        self.a = cur

    def erase(self, mask):
        self.a[mask] = 0

    def glint(self, x, y, col='#ffffff', size=1):
        self.px(x, y, col)
        if size >= 2:
            c = hx(col) if isinstance(col, str) else col
            soft = (c[0], c[1], c[2], 150)
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                self.px(x + dx, y + dy, soft)

    def outline_all(self, col):
        m = self.filled()
        ring = _dil4(m) & ~m
        self.put(ring, hx(col) if isinstance(col, str) else col)

    # -- output
    def image(self):
        a = np.clip(np.round(self.a), 0, 255).astype(np.uint8)
        return Image.fromarray(a, 'RGBA')

    def save(self, path):
        import os
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.image().save(path)

def _erode4(m):
    o = m.copy()
    o[1:, :] &= m[:-1, :]; o[:-1, :] &= m[1:, :]
    o[:, 1:] &= m[:, :-1]; o[:, :-1] &= m[:, 1:]
    o[0, :] = False; o[-1, :] = False; o[:, 0] = False; o[:, -1] = False
    return o

def sheet(items, out, scale=6, cols=10, bg=(139, 139, 139, 255)):
    """Contact sheet like a Minecraft inventory (slot grey) for review."""
    from PIL import ImageDraw
    cell = N * scale + 28
    rows = (len(items) + cols - 1) // cols
    img = Image.new('RGBA', (cols * cell, rows * cell), bg)
    d = ImageDraw.Draw(img)
    for i, (name, ic) in enumerate(items):
        im = ic.image() if hasattr(ic, 'image') else ic
        big = im.resize((im.width * scale * N // im.width, im.height * scale * N // im.height), Image.NEAREST)
        x, y = (i % cols) * cell + 14, (i // cols) * cell + 4
        img.alpha_composite(big, (x, y))
        d.text((x - 8, y + N * scale + 2), name[:22], fill=(20, 20, 20, 255))
    img.save(out)
