"""Village drug lab structures (NBT templates) for all five village types.

Design notes (common Minecraft building guidelines applied to a small workshop):
- shape: a 9 x 9 timber-frame body with a 45 degree gable roof (low-pitched in savanna, flat with parapet in desert)
  instead of a box; the gable faces the street and the roof overhangs one block on every side. The whole piece is
  11 x 11 (about a vanilla medium house) - a larger bounding box rarely fits between the other village pieces;
- depth: log frame (corner/door posts + wall plate), stone plinth with a sloped skirt, recessed panes with
  trapdoor shutters and stair sills, corbels under the eaves, a stepped chimney, planters and a porch awning;
- palette: 3-4 materials per village type that match the vanilla houses (plus the lab blocks inside);
- interior: exposed roof truss with pendant lab lights, a process line along the back wall (fume hood next to the
  reactor, crystallizer on packed ice), a central lab bench and storage along the side walls;
- story: a smoking chimney (campfire) above the fume hood, a vent pipe, an exhaust fan in the back gable and one
  window covered with plastic sheeting.

Coordinates: x = depth (0 = street side, the entrance faces west), z = width, y = up.
  x=0 front overhang (entrance step, planters, awning), x=1..9 walls (front gable at x=1), x=10 back overhang;
  z=0 / z=10 eaves, z=1..9 walls (door and ridge at z=5); y=0 foundation, y=1 plinth + floor, y=2..4 walls, y=5 plate.
The entrance jigsaw at (0,0,5) connects to the village streets like vanilla houses do. Village pieces are placed with
"known shape", so every connecting block (panes, stairs) gets its final state here.
"""
import os
import sys
import nbt
from nbt import Tag, LIST, COMPOUND

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'data', 'highcraft', 'structure', 'village')
DATA_VERSION = 3955   # 1.21.1

# ---------------------------------------------------------------- block state helpers
def S(name, **props):
    if ':' not in name:
        name = 'minecraft:' + name
    if not props:
        return name
    val = lambda v: ('true' if v else 'false') if isinstance(v, bool) else str(v)
    return name + '[' + ','.join(f'{k}={val(v)}' for k, v in sorted(props.items())) + ']'

def stairs(m, facing, half='bottom', shape='straight'):
    return S(m, facing=facing, half=half, shape=shape, waterlogged=False)

def slab(m, typ='bottom'):
    return S(m, type=typ, waterlogged=False)

def log(m, axis='y'):
    return S(m, axis=axis)

def pane(m, axis):
    """axis 'x': the pane runs along x (connects east/west), 'z': along z."""
    return S(m, east=axis == 'x', west=axis == 'x', north=axis == 'z', south=axis == 'z', waterlogged=False)

def trapdoor(m, facing, half='bottom', open_=True):
    """An open trapdoor lies against the block opposite to its facing: facing = away from the wall."""
    return S(m, facing=facing, half=half, open=open_, powered=False, waterlogged=False)

def door(m, facing, half, hinge='left'):
    return S(m, facing=facing, half=half, hinge=hinge, open=False, powered=False)

def lantern(hanging=True):
    return S('lantern', hanging=hanging, waterlogged=False)

CHAIN = S('chain', axis='y', waterlogged=False)
CAMPFIRE = S('campfire', facing='north', lit=True, signal_fire=False, waterlogged=False)

def hc(name, facing=None, lit=None):
    props = {}
    if facing:
        props['facing'] = facing
    if lit is not None:
        props['lit'] = lit
    return S('highcraft:' + name, **props)

# ---------------------------------------------------------------- styles (match the vanilla village palettes)
STYLES = {
    'plains': dict(
        leaves='oak_leaves',
        roof='gable45', found='cobblestone', plinth='cobblestone', plinth_alt='mossy_cobblestone', skirt='cobblestone_stairs',
        post='oak_log', infill='white_terracotta', gable='oak_planks', roof_mat='spruce', cap='dark_oak_slab',
        ridge='stripped_spruce_log', wood='spruce', chimney='cobblestone', chimney_stairs='cobblestone_stairs',
        soil='coarse_dirt', plants=['poppy', 'highcraft:wild_cannabis', 'dandelion', 'azure_bluet'],
        back=[('hay_block', 'y'), ('hay_block', 'y'), ('hay_block', 'y')]),
    'taiga': dict(
        leaves='spruce_leaves',
        roof='gable45', found='cobblestone', plinth='cobblestone', plinth_alt='mossy_cobblestone', skirt='mossy_cobblestone_stairs',
        post='spruce_log', infill='spruce_planks', wainscot='cobblestone', gable='stripped_spruce_wood', roof_mat='spruce',
        cap='mossy_cobblestone_slab', ridge='spruce_log', wood='spruce', chimney='cobblestone', chimney_stairs='cobblestone_stairs',
        soil='podzol', plants=['fern', 'highcraft:wild_cannabis', S('sweet_berry_bush', age=3), 'lily_of_the_valley'],
        back=[('spruce_log', 'x'), ('spruce_log', 'x'), ('spruce_log', 'x')]),
    'snowy': dict(
        leaves='spruce_leaves',
        roof='gable_snow', found='stone_bricks', plinth='stone_bricks', plinth_alt='cracked_stone_bricks', skirt='stone_brick_stairs',
        post='stripped_spruce_log', infill='spruce_planks', gable='spruce_planks', roof_mat='dark_oak', cap=None,
        ridge='stripped_spruce_log', wood='spruce', chimney='stone_bricks', chimney_stairs='stone_brick_stairs',
        soil='podzol', plants=['spruce_sapling', 'highcraft:wild_poppy', S('sweet_berry_bush', age=2), 'fern'],
        back=[('spruce_log', 'x'), ('spruce_log', 'x'), ('packed_ice', None)]),
    'savanna': dict(
        leaves='acacia_leaves',
        roof='gable_low', found='cobblestone', plinth='packed_mud', plinth_alt='packed_mud', skirt='mud_brick_stairs',
        post='acacia_log', infill='mud_bricks', gable='acacia_planks', roof_mat='acacia', cap=None,
        ridge='stripped_acacia_log', wood='acacia', chimney='mud_bricks', chimney_stairs='mud_brick_stairs',
        soil='coarse_dirt', plants=['highcraft:wild_tobacco', 'dead_bush', 'orange_tulip', 'highcraft:wild_cannabis'],
        back=[('hay_block', 'y'), ('hay_block', 'y'), ('hay_block', 'y')]),
    'desert': dict(
        roof='flat', found='smooth_sandstone', plinth='cut_sandstone', plinth_alt='cut_sandstone', skirt='smooth_sandstone_stairs',
        post='cut_sandstone', infill='smooth_sandstone', viga='stripped_jungle_log', wood='jungle',
        chimney='sandstone', chimney_stairs='sandstone_stairs', accent='chiseled_sandstone',
        soil='sand', plants=['dead_bush', 'highcraft:wild_coca', 'highcraft:wild_poppy', 'dead_bush'],
        back=[(S('decorated_pot', cracked=False, facing='east', waterlogged=False), None),
              (S('decorated_pot', cracked=False, facing='east', waterlogged=False), None), ('sandstone', None)]),
}

SX, SZ = 11, 11                 # footprint incl. overhangs
XF, XB, ZN, ZS, ZM = 1, 9, 1, 9, 5    # front/back wall, side walls, centre line
XM = 5                          # middle frame post / roof truss
POSTS_SIDE = (XF, XM, XB)       # frame posts in the side walls (x)
POSTS_FRONT = (1, 4, 6, 9)      # frame posts in the front wall (z): corners + door frame
POSTS_BACK = (1, 5, 9)
WIN_SIDE = (3, 7)               # one 1-wide window (rows y=3..4) per bay of the side walls (x)
WIN_FRONT = ((2, 3), (7, 8))    # (z)
CHIMNEY_X = (6, 7, 8)           # on the z=10 side, the shaft is the middle one

def parse(state):
    if '[' not in state:
        return state, {}
    name, props = state[:-1].split('[', 1)
    return name, dict(p.split('=') for p in props.split(','))

class Template:
    def __init__(self):
        self.blocks = {}

    def set(self, x, y, z, state, nbt_=None):
        if ':' not in state:
            state = 'minecraft:' + state
        self.blocks[(x, y, z)] = (state, nbt_)

    def get(self, x, y, z):
        b = self.blocks.get((x, y, z))
        return b[0] if b else None

    def fill(self, x0, y0, z0, x1, y1, z1, state):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.set(x, y, z, state)

    def size(self):
        return [max(p[i] for p in self.blocks) + 1 for i in range(3)]

    def save(self, path):
        palette, index, blocks = [], {}, []
        for (x, y, z), (state, data) in sorted(self.blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
            if state not in index:
                index[state] = len(palette)
                name, props = parse(state)
                entry = {'Name': name}
                if props:
                    entry['Properties'] = props
                palette.append(entry)
            b = {'pos': [x, y, z], 'state': index[state]}
            if data:
                b['nbt'] = data
            blocks.append(b)
        nbt.write(path, {'size': self.size(), 'entities': Tag(LIST, [], elem=COMPOUND), 'blocks': blocks,
                         'palette': palette, 'DataVersion': DATA_VERSION})

# ---------------------------------------------------------------- roof profiles
def roof_profile(kind):
    """Per z (0..10): list of (y, state-kind) roof elements, the ridge row (z=5) and the y of the first roof block.
    Kinds: 'stair_s'/'stair_n' (rising towards +z / -z), 'block', 'slab_b', 'slab_t', 'ridge', 'cap', 'snow'."""
    prof = {}
    if kind in ('gable45', 'gable_snow'):
        for z in range(0, 5):
            y = 5 + z
            if kind == 'gable45':
                prof[z] = [(y, 'stair_s')]
                prof[10 - z] = [(y, 'stair_n')]
            else:
                prof[z] = [(y, 'block'), (y + 1, 'snow')]
                prof[10 - z] = [(y, 'block'), (y + 1, 'snow')]
        prof[5] = [(9, 'ridge'), (10, 'cap' if kind == 'gable45' else 'snow')]
    elif kind == 'gable_low':
        seq = [(5, 'slab_t'), (6, 'slab_b'), (6, 'slab_t'), (7, 'slab_b'), (7, 'slab_t')]
        for z, e in enumerate(seq):
            prof[z] = [e]
            prof[10 - z] = [e]
        prof[5] = [(7, 'ridge'), (8, 'slab_b')]
    return prof

def roof_bottom(prof, z):
    """Lowest roof y at z (walls/gables are filled up to below it)."""
    return min(y for y, _ in prof[z])

def roof_top(prof, z):
    return max(y for y, _ in prof[z])

# ---------------------------------------------------------------- the building
def drug_lab(vtype, st):
    t = Template()
    flat = st['roof'] == 'flat'
    wood = st['wood']
    prof = None if flat else roof_profile(st['roof'])
    top_y = 8 if flat else roof_top(prof, ZM)

    # air over the whole footprint (clears terrain), foundation below
    t.fill(0, 1, 0, SX - 1, top_y, SZ - 1, 'air')
    t.fill(0, 0, 0, SX - 1, 0, SZ - 1, st['found'])

    # plinth (y=1): under the walls and as a ring around the building; interior floor
    t.fill(0, 1, 0, SX - 1, 1, SZ - 1, st['plinth'])
    for x, z in ((XF, ZN), (XF, ZS), (XB, ZN), (XB, ZS), (XM, ZN), (XM, ZS), (XB, ZM)):
        t.set(x, 1, z, st['plinth_alt'])
    t.fill(XF + 1, 1, ZN + 1, XB - 1, 1, ZS - 1, 'highcraft:lab_tiles')
    t.fill(XB - 2, 1, ZN + 1, XB - 2, 1, ZS - 1, 'highcraft:hazard_stripes')   # in front of the process line
    t.set(XB - 1, 1, 4, 'packed_ice')                                            # deep cold under the crystallizer
    # sloped skirt along the sides and the back (corners stay full blocks)
    for x in range(XF, XB + 1):
        t.set(x, 1, 0, stairs(st['skirt'], 'south'))
        t.set(x, 1, SZ - 1, stairs(st['skirt'], 'north'))
    for z in range(ZN, ZS + 1):
        t.set(SX - 1, 1, z, stairs(st['skirt'], 'west'))

    # walls: frame posts + infill (y=2..4), wall plate / band (y=5)
    def infill(y):
        return st.get('wainscot', st['infill']) if y == 2 else st['infill']
    post = (lambda axis: st['post']) if flat else (lambda axis: log(st['post'], axis))
    for x in range(XF, XB + 1):
        for z in (ZN, ZS):
            for y in range(2, 5):
                t.set(x, y, z, post('y') if x in POSTS_SIDE else infill(y))
            t.set(x, 5, z, st['accent'] if flat and x in POSTS_SIDE else post('x'))
    for x, posts in ((XF, POSTS_FRONT), (XB, POSTS_BACK)):
        for z in range(ZN, ZS + 1):
            for y in range(2, 5):
                t.set(x, y, z, post('y') if z in posts else infill(y))
            t.set(x, 5, z, st['accent'] if flat and z in (ZN, ZS) else post('z'))

    # windows: recessed panes, trapdoor shutters, upside-down stair sills
    glass = 'glass_pane'
    plastic = 'highcraft:plastic_sheeting'
    for side, zw, zo, out in ((0, ZN, 0, 'north'), (1, ZS, SZ - 1, 'south')):
        sill_facing = 'south' if side == 0 else 'north'
        for bay, x in enumerate(WIN_SIDE):
            if side == 1 and bay == 1:
                continue                                   # chimney
            mat = plastic if (side == 0 and bay == 1) else glass
            for y in (3, 4):
                t.set(x, y, zw, pane(mat, 'x'))
                t.set(x - 1, y, zo, trapdoor(wood + '_trapdoor', out))
                t.set(x + 1, y, zo, trapdoor(wood + '_trapdoor', out))
            t.set(x, 2, zo, stairs(wood + '_stairs' if not flat else st['skirt'], sill_facing, 'top'))
    for cols in WIN_FRONT:
        for z in cols:
            for y in (3, 4):
                t.set(XF, y, z, pane(glass, 'z'))
    # back wall: same windows as the front with shutters (no sills: the back props stand below them)
    for cols in WIN_FRONT:
        for z in cols:
            for y in (3, 4):
                t.set(XB, y, z, pane(glass, 'z'))
        if not flat:                                    # desert: the exhaust fan sits between the windows
            for z in (cols[0] - 1, cols[1] + 1):
                for y in (3, 4):
                    t.set(SX - 1, y, z, trapdoor(wood + '_trapdoor', 'east'))

    # front: raised planters under the windows, porch step between two plinth blocks, door
    for cols, plants in ((WIN_FRONT[0], st['plants'][:2]), (WIN_FRONT[1], st['plants'][2:])):
        for z, p in zip(cols, plants):
            t.set(0, 1, z, st['soil'])
            t.set(0, 2, z, p if ':' in p else 'minecraft:' + p)
    t.set(0, 1, ZM, stairs(st['skirt'], 'east'))
    t.set(XF, 2, ZM, door(wood + '_door', 'east', 'lower'))
    t.set(XF, 3, ZM, door(wood + '_door', 'east', 'upper'))
    if flat:
        t.set(XF, 4, ZM, st['accent'])
    else:
        t.set(XF, 4, ZM, log(st['post'], 'z'))                     # lintel

    if flat:
        build_flat_roof(t, st)
    else:
        build_gable_roof(t, st, prof)

    # chimney on the z=10 side above the fume hood: stepped base, shaft, campfire smoke
    cx0, cx, cx1 = CHIMNEY_X
    shaft_top = 7 if st['roof'] in ('flat', 'gable_low') else 8
    for x in (cx0, cx1):
        t.set(x, 1, SZ - 1, st['chimney'])
        t.set(x, 2, SZ - 1, st['chimney'])
    t.set(cx0, 3, SZ - 1, stairs(st['chimney_stairs'], 'east'))
    t.set(cx1, 3, SZ - 1, stairs(st['chimney_stairs'], 'west'))
    for y in range(1, shaft_top + 1):
        t.set(cx, y, SZ - 1, st['chimney'])
    t.set(cx, shaft_top + 1, SZ - 1, CAMPFIRE)

    interior(t, st, prof)
    exterior(t, st, prof)

    # jigsaws: entrance (connects to the street) and villager spawn
    t.set(0, 0, ZM, 'minecraft:jigsaw[orientation=west_up]', {
        'id': 'minecraft:jigsaw', 'name': 'minecraft:building_entrance', 'target': 'minecraft:building_entrance',
        'pool': f'minecraft:village/{vtype}/streets', 'joint': 'aligned', 'final_state': 'minecraft:' + st['found'],
        'placement_priority': 0, 'selection_priority': 0})
    t.set(3, 1, 3, 'minecraft:jigsaw[orientation=up_north]', {
        'id': 'minecraft:jigsaw', 'name': 'minecraft:bottom', 'target': 'minecraft:bottom',
        'pool': f'minecraft:village/{vtype}/villagers', 'joint': 'rollable', 'final_state': 'highcraft:lab_tiles',
        'placement_priority': 0, 'selection_priority': 0})
    return t

def build_gable_roof(t, st, prof):
    rm = st['roof_mat']
    snow = st['roof'] == 'gable_snow'
    for z, elems in prof.items():
        for y, kind in elems:
            for x in range(0, SX):
                if kind == 'stair_s':
                    s = stairs(rm + '_stairs', 'south')
                elif kind == 'stair_n':
                    s = stairs(rm + '_stairs', 'north')
                elif kind == 'block':
                    s = rm + '_planks'
                elif kind == 'slab_b':
                    s = slab(rm + '_slab', 'bottom')
                elif kind == 'slab_t':
                    # gable rows: a double slab closes the half-block gap above the gable wall
                    s = slab(rm + '_slab', 'double' if x in (XF, XB) else 'top')
                elif kind == 'ridge':
                    s = log(st['ridge'], 'x')
                elif kind == 'cap':
                    s = slab(st['cap'], 'bottom')
                else:   # snow
                    s = S('snow', layers=2)
                t.set(x, y, z, s)
    # gable walls (triangle above the plate) with a small window / the exhaust fan at the back
    for x in (XF, XB):
        for z in range(ZN + 1, ZS):
            for y in range(6, roof_bottom(prof, z)):
                t.set(x, y, z, st['gable'])
    gy = max(6, roof_bottom(prof, ZM) - 2)
    t.set(XF, gy, ZM, pane('glass_pane', 'z'))
    t.set(XB, gy, ZM, st['gable'])
    t.set(XB + 1, gy, ZM, hc('exhaust_fan', 'east'))
    # corbels under the eaves at the frame posts
    if st['roof'] != 'gable_low':
        for x in POSTS_SIDE:
            t.set(x, 4, 0, stairs(st['wood'] + '_stairs', 'south', 'top'))
            t.set(x, 4, SZ - 1, stairs(st['wood'] + '_stairs', 'north', 'top'))
    # porch awning over the door, lanterns, sign
    for z in (ZM - 1, ZM, ZM + 1):
        t.set(XF - 1, 4, z, stairs(st['wood'] + '_stairs', 'east'))
    t.set(XF - 1, 3, ZM - 1, lantern())
    t.set(XF - 1, 3, ZM + 1, lantern())
    t.set(XF - 1, 5, ZM, hc('hazard_sign', 'west'))
    # interior truss: tie beam, king post (+ pendant lights hang from the ridge beam)
    ridge_y = roof_bottom(prof, ZM)
    for z in range(ZN + 1, ZS):
        t.set(XM, 5, z, log(st['post'], 'z'))
    for y in range(6, ridge_y):
        t.set(XM, y, ZM, log(st['post'], 'y'))
    for x in (XM - 2, XM + 2):
        for y in range(5, ridge_y):
            t.set(x, y, ZM, CHAIN)
    # vent pipe through the roof above the reactor
    vz = 6 if prof[7][-1][1] == 'slab_b' else 7          # never on a bottom slab (it would float)
    vy = roof_top(prof, vz) + (0 if snow else 1)
    t.set(XB - 1, vy, vz, S('lightning_rod', facing='up', powered=False, waterlogged=False))

def build_flat_roof(t, st):
    """Desert: flat roof with a cornice, crenellated parapet and wooden vigas (protruding roof beams)."""
    sk = st['skirt']
    t.fill(XF + 1, 5, ZN + 1, XB - 1, 5, ZS - 1, st['infill'])           # roof deck = ceiling
    # cornice (upside-down stairs) all around at y=5
    for x in range(0, SX):
        t.set(x, 5, 0, stairs(sk, 'south', 'top'))
        t.set(x, 5, SZ - 1, stairs(sk, 'north', 'top'))
    for z in range(ZN, ZS + 1):
        t.set(XF - 1, 5, z, stairs(sk, 'east', 'top'))
        t.set(SX - 1, 5, z, stairs(sk, 'west', 'top'))
    # parapet: merlons at corners / posts, slabs between
    merlon_x, merlon_z = POSTS_SIDE, (ZN, ZM, ZS)
    for x in range(XF, XB + 1):
        for z in (ZN, ZS):
            t.set(x, 6, z, st['post'] if x in merlon_x else slab('smooth_sandstone_slab'))
    for x in (XF, XB):
        for z in range(ZN + 1, ZS):
            t.set(x, 6, z, st['post'] if z in merlon_z else slab('smooth_sandstone_slab'))
    # vigas at the posts
    for x in POSTS_SIDE:
        t.set(x, 4, 0, log(st['viga'], 'z'))
        t.set(x, 4, SZ - 1, log(st['viga'], 'z'))
    # entrance portal: projecting pillars and arch; hazard signs on the front corners
    for z in (ZM - 1, ZM + 1):
        for y in (2, 3, 4):
            t.set(XF - 1, y, z, st['post'])
        t.set(XF - 1, 5, z, st['accent'])
    t.set(XF - 1, 4, ZM, slab('smooth_sandstone_slab', 'top'))
    t.set(XF - 1, 5, ZM, st['accent'])
    t.set(XF - 1, 6, ZM, slab('smooth_sandstone_slab'))
    for z in (ZN, ZS):
        t.set(XF - 1, 3, z, hc('hazard_sign', 'west'))
    # exhaust fan on the back wall, vent pipe on the roof
    t.set(XB + 1, 3, ZM, hc('exhaust_fan', 'east'))
    t.set(XB - 1, 6, 7, S('lightning_rod', facing='up', powered=False, waterlogged=False))

def interior(t, st, prof):
    W, B = 'west', XB - 1          # the process line stands in front of the back wall
    # process line along the back wall, facing the room
    t.set(B, 2, 2, hc('pill_press', W, False))
    t.set(B, 2, 3, hc('lab_counter', W))
    t.set(B, 3, 3, hc('precision_scale', W))
    t.set(B, 2, 4, hc('crystallizer', W, False))
    t.set(B, 4, 4, hc('hazard_sign', W))
    t.set(B, 2, 5, hc('lab_counter', W))
    t.set(B, 3, 5, hc('glassware_shelf', W))
    t.set(B, 2, 6, hc('lab_counter', W))
    t.set(B, 3, 6, S('brewing_stand', has_bottle_0=True, has_bottle_1=False, has_bottle_2=True))
    t.set(B, 2, 7, hc('chemical_reactor', W, False))
    t.set(B, 2, 8, hc('fume_hood', W))
    t.set(B - 1, 4, 8, hc('exhaust_fan', 'north'))
    # north side (z=2): loot chest, counter under the window, sink, gas cylinders at the post
    t.set(2, 2, 2, S('chest', facing='south', type='single', waterlogged=False),
          {'id': 'minecraft:chest', 'LootTable': 'highcraft:chests/village_drug_lab'})
    t.set(3, 2, 2, hc('lab_counter', 'south'))
    t.set(4, 2, 2, S('water_cauldron', level=2))
    t.set(4, 3, 2, hc('glassware_shelf', 'south'))
    t.set(5, 2, 2, 'highcraft:gas_cylinder')
    t.set(6, 2, 2, 'highcraft:gas_cylinder')
    t.set(7, 2, 2, hc('lab_counter', 'south'))
    # south side (z=8): lab station under the window, barrels, counter in the reactor corner
    t.set(2, 2, 8, 'highcraft:chemical_barrel')
    t.set(3, 2, 8, hc('lab_station', 'north', False))
    t.set(4, 2, 8, hc('lab_counter', 'north'))
    t.set(4, 3, 8, hc('glassware_shelf', 'north'))
    t.set(5, 2, 8, 'highcraft:hazard_barrel')
    t.set(6, 2, 8, 'highcraft:chemical_barrel')
    t.set(7, 2, 8, hc('lab_counter', 'north'))
    # central lab bench
    t.set(4, 2, ZM, hc('lab_counter', 'north'))
    t.set(5, 2, ZM, hc('lab_counter', 'south'))
    t.set(4, 3, ZM, S('brewing_stand', has_bottle_0=False, has_bottle_1=True, has_bottle_2=False))
    # lights: under the tie beam (gable) / the ceiling (flat), pendants over the aisles
    for x, z in ((XM, 3), (XM, 7), (XM - 2, ZM), (XM + 2, ZM)):
        t.set(x, 4, z, 'highcraft:lab_light')

def exterior(t, st, prof):
    # back: drying rack (grower job site) and a stack of style props (hay, firewood, pots)
    t.set(SX - 1, 1, 3, hc('drying_rack', 'east'))
    t.set(SX - 1, 1, 2, 'highcraft:chemical_barrel')
    (b0, a0), (b1, a1), (b2, a2) = st['back']
    for (x, y, z), (b, a) in (((SX - 1, 1, 7), (b0, a0)), ((SX - 1, 1, 8), (b1, a1)), ((SX - 1, 2, 8), (b2, a2))):
        t.set(x, y, z, log(b, a) if a else b)
    # small shrubs at the front corners soften the plinth line
    if st.get('leaves'):
        for z in (0, SZ - 1):
            t.set(0, 1, z, S(st['leaves'], distance=1, persistent=True, waterlogged=False))
    # outdoor gas cylinders next to the chimney (z=10 side)
    t.set(CHIMNEY_X[0] - 1, 1, SZ - 1, 'highcraft:gas_cylinder')
    t.set(CHIMNEY_X[2] + 1, 1, SZ - 1, 'highcraft:gas_cylinder')

def run(root=ROOT):
    for vtype, style in STYLES.items():
        drug_lab(vtype, style).save(os.path.join(root, vtype, 'drug_lab.nbt'))
    return len(STYLES)

if __name__ == '__main__':
    print('structures', run(sys.argv[1] if len(sys.argv) > 1 else ROOT))
