"""Minimal NBT reader/writer (big-endian, gzip) for structure templates."""
import gzip, struct, io

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = range(13)

class Tag:
    """Typed value for writing: Tag(INT, 5), Tag(LIST, [..], elem=INT)."""
    def __init__(self, t, v, elem=None):
        self.t, self.v, self.elem = t, v, elem

def _r(f, fmt):
    return struct.unpack('>' + fmt, f.read(struct.calcsize('>' + fmt)))[0]

def _read_payload(f, t):
    if t == BYTE: return _r(f, 'b')
    if t == SHORT: return _r(f, 'h')
    if t == INT: return _r(f, 'i')
    if t == LONG: return _r(f, 'q')
    if t == FLOAT: return _r(f, 'f')
    if t == DOUBLE: return _r(f, 'd')
    if t == BYTE_ARRAY: n = _r(f, 'i'); return list(f.read(n))
    if t == STRING: n = _r(f, 'H'); return f.read(n).decode('utf-8')
    if t == LIST:
        et = _r(f, 'b'); n = _r(f, 'i')
        return [_read_payload(f, et) for _ in range(n)]
    if t == COMPOUND:
        d = {}
        while True:
            ct = _r(f, 'b')
            if ct == END: return d
            name = _read_payload(f, STRING)
            d[name] = _read_payload(f, ct)
    if t == INT_ARRAY: n = _r(f, 'i'); return [_r(f, 'i') for _ in range(n)]
    if t == LONG_ARRAY: n = _r(f, 'i'); return [_r(f, 'q') for _ in range(n)]
    raise ValueError(t)

def read(path):
    with gzip.open(path, 'rb') as f:
        t = _r(f, 'b'); _read_payload(f, STRING)
        return _read_payload(f, t)

def _w(f, fmt, v):
    f.write(struct.pack('>' + fmt, v))

def _type_of(v):
    if isinstance(v, Tag): return v.t
    if isinstance(v, bool): return BYTE
    if isinstance(v, int): return INT
    if isinstance(v, float): return DOUBLE
    if isinstance(v, str): return STRING
    if isinstance(v, dict): return COMPOUND
    if isinstance(v, list): return LIST
    raise TypeError(type(v))

def _write_payload(f, t, v, elem=None):
    if isinstance(v, Tag): t, elem, v = v.t, v.elem, v.v
    if t == BYTE: _w(f, 'b', int(v))
    elif t == SHORT: _w(f, 'h', v)
    elif t == INT: _w(f, 'i', v)
    elif t == LONG: _w(f, 'q', v)
    elif t == FLOAT: _w(f, 'f', v)
    elif t == DOUBLE: _w(f, 'd', v)
    elif t == STRING:
        b = v.encode('utf-8'); _w(f, 'H', len(b)); f.write(b)
    elif t == LIST:
        et = elem if elem is not None else (_type_of(v[0]) if v else END)
        _w(f, 'b', et); _w(f, 'i', len(v))
        for x in v: _write_payload(f, et, x)
    elif t == COMPOUND:
        for k, x in v.items():
            ct = _type_of(x)
            _w(f, 'b', ct); _write_payload(f, STRING, k); _write_payload(f, ct, x)
        _w(f, 'b', END)
    elif t == INT_ARRAY:
        _w(f, 'i', len(v)); [_w(f, 'i', x) for x in v]
    else:
        raise ValueError(t)

def write(path, root):
    buf = io.BytesIO()
    _w(buf, 'b', COMPOUND); _write_payload(buf, STRING, ''); _write_payload(buf, COMPOUND, root)
    import os
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, 'wb') as f:
        f.write(buf.getvalue())
