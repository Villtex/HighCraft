"""Village drug lab structures (NBT templates) for all five village types.

Layout (x = depth, front/entrance at x=0 facing west; z = width): 10 x 6 x 9 blocks.
The entrance jigsaw at (0,0,4) connects to the village streets like vanilla houses do.
"""
import os
import nbt
from nbt import Tag, LIST, COMPOUND

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'data', 'highcraft', 'structure', 'village')
DATA_VERSION = 3955   # 1.21.1

STYLES = {
    'plains': dict(found='minecraft:cobblestone', wall='minecraft:oak_planks', corner='minecraft:stripped_oak_log[axis=y]',
                   roof='minecraft:oak_planks', slab='minecraft:oak_slab[type=bottom,waterlogged=false]', door='minecraft:oak_door',
                   step='minecraft:oak_stairs', trim=None),
    'desert': dict(found='minecraft:smooth_sandstone', wall='minecraft:cut_sandstone', corner='minecraft:chiseled_sandstone',
                   roof='minecraft:smooth_sandstone', slab='minecraft:smooth_sandstone_slab[type=bottom,waterlogged=false]',
                   door='minecraft:birch_door', step='minecraft:sandstone_stairs', trim='minecraft:terracotta'),
    'savanna': dict(found='minecraft:cobblestone', wall='minecraft:acacia_planks', corner='minecraft:acacia_log[axis=y]',
                    roof='minecraft:acacia_planks', slab='minecraft:acacia_slab[type=bottom,waterlogged=false]', door='minecraft:acacia_door',
                    step='minecraft:acacia_stairs', trim='minecraft:orange_terracotta'),
    'snowy': dict(found='minecraft:stone_bricks', wall='minecraft:spruce_planks', corner='minecraft:stripped_spruce_log[axis=y]',
                  roof='minecraft:spruce_planks', slab='minecraft:spruce_slab[type=bottom,waterlogged=false]', door='minecraft:spruce_door',
                  step='minecraft:spruce_stairs', trim=None, snow=True),
    'taiga': dict(found='minecraft:cobblestone', wall='minecraft:spruce_planks', corner='minecraft:spruce_log[axis=y]',
                  roof='minecraft:spruce_planks', slab='minecraft:spruce_slab[type=bottom,waterlogged=false]', door='minecraft:spruce_door',
                  step='minecraft:spruce_stairs', trim='minecraft:mossy_cobblestone'),
}

def parse(state):
    if '[' not in state:
        return state, {}
    name, props = state[:-1].split('[', 1)
    return name, dict(p.split('=') for p in props.split(','))

class Template:
    def __init__(self, size):
        self.size = size
        self.blocks = {}

    def set(self, x, y, z, state, nbt_=None):
        self.blocks[(x, y, z)] = (state, nbt_)

    def fill(self, x0, y0, z0, x1, y1, z1, state):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.set(x, y, z, state)

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
        nbt.write(path, {'size': list(self.size), 'entities': Tag(LIST, [], elem=COMPOUND), 'blocks': blocks,
                         'palette': palette, 'DataVersion': DATA_VERSION})

def drug_lab(vtype, st):
    t = Template((10, 6, 9))
    F = lambda f: f'[facing={f}]'
    # air inside the building (clears terrain)
    t.fill(1, 1, 0, 9, 4, 8, 'minecraft:air')
    # floor + foundation
    t.fill(1, 0, 0, 9, 0, 8, st['found'])
    t.fill(2, 0, 1, 8, 0, 7, 'highcraft:lab_tiles')
    for z in range(1, 8):
        t.set(7, 0, z, 'highcraft:hazard_stripes')
    t.set(8, 0, 4, 'minecraft:packed_ice')          # deep cold under the crystallizer
    # walls
    for y in range(1, 4):
        for z in range(0, 9):
            t.set(1, y, z, st['wall']); t.set(9, y, z, st['wall'])
        for x in range(1, 10):
            t.set(x, y, 0, st['wall']); t.set(x, y, 8, st['wall'])
        for (x, z) in ((1, 0), (1, 8), (9, 0), (9, 8)):
            t.set(x, y, z, st['corner'])
    if st.get('trim'):
        for z in range(0, 9):
            t.set(1, 3, z, st['trim']); t.set(9, 3, z, st['trim'])
        for x in range(1, 10):
            t.set(x, 3, 0, st['trim']); t.set(x, 3, 8, st['trim'])
    # windows (plastic sheeting)
    ns = 'highcraft:plastic_sheeting[east=false,north=true,south=true,waterlogged=false,west=false]'
    ew = 'highcraft:plastic_sheeting[east=true,north=false,south=false,waterlogged=false,west=true]'
    for z in (2, 6):
        t.set(1, 2, z, ns)
    for x in (3, 6):
        t.set(x, 2, 0, ew); t.set(x, 2, 8, ew)
    # door
    d = st['door']
    t.set(1, 1, 4, f'{d}[facing=east,half=lower,hinge=left,open=false,powered=false]')
    t.set(1, 2, 4, f'{d}[facing=east,half=upper,hinge=left,open=false,powered=false]')
    # roof: flat with a slab layer, overhang above the door
    t.fill(1, 4, 0, 9, 4, 8, st['roof'])
    t.fill(1, 5, 0, 9, 5, 8, st['slab'])
    for z in range(3, 6):
        t.set(0, 4, z, st['slab'].replace('type=bottom', 'type=top'))
    if st.get('snow'):
        t.fill(1, 5, 0, 9, 5, 8, 'minecraft:snow_block')
    t.set(8, 5, 1, 'minecraft:lightning_rod[facing=up,powered=false,waterlogged=false]')   # vent stack above the fume hood
    # lab equipment along the back wall (x=8), all facing the room (west)
    t.set(8, 1, 1, 'highcraft:fume_hood' + F('west'))
    t.set(8, 1, 2, 'highcraft:chemical_reactor[facing=west,lit=false]')
    t.set(8, 1, 3, 'highcraft:lab_counter' + F('west'))
    t.set(8, 1, 4, 'highcraft:crystallizer[facing=west,lit=false]')
    t.set(8, 1, 5, 'highcraft:lab_counter' + F('west'))
    t.set(8, 1, 6, 'highcraft:pill_press[facing=west,lit=false]')
    t.set(8, 1, 7, 'highcraft:lab_counter' + F('west'))
    for z in (3, 5, 7):
        t.set(8, 2, z, 'highcraft:glassware_shelf' + F('west'))
    t.set(8, 3, 4, 'highcraft:exhaust_fan' + F('west'))
    # north side (z=7)
    t.set(2, 1, 7, 'highcraft:chemical_barrel')
    t.set(3, 1, 7, 'highcraft:hazard_barrel')
    t.set(5, 1, 7, 'minecraft:chest[facing=north,type=single,waterlogged=false]',
          {'id': 'minecraft:chest', 'LootTable': 'highcraft:chests/village_drug_lab'})
    t.set(6, 1, 7, 'highcraft:lab_counter' + F('north'))
    t.set(6, 2, 7, 'minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=false,has_bottle_2=true]')
    t.set(7, 1, 7, 'highcraft:precision_scale')
    t.set(4, 2, 7, 'highcraft:hazard_sign' + F('north'))
    # south side (z=1)
    t.set(2, 1, 1, 'highcraft:gas_cylinder')
    t.set(3, 1, 1, 'highcraft:gas_cylinder')
    t.set(4, 1, 1, 'highcraft:lab_counter' + F('south'))
    t.set(5, 1, 1, 'highcraft:lab_counter' + F('south'))
    t.set(5, 2, 1, 'minecraft:cauldron')
    t.set(6, 1, 1, 'highcraft:chemical_barrel')
    # ceiling lights
    t.set(3, 3, 4, 'highcraft:lab_light'); t.set(6, 3, 4, 'highcraft:lab_light')
    # warning signs outside next to the door
    t.set(0, 2, 2, 'highcraft:hazard_sign' + F('west'))
    t.set(0, 2, 6, 'highcraft:hazard_sign' + F('west'))
    # jigsaws: entrance (connects to the street) and villager spawn
    t.set(0, 0, 4, 'minecraft:jigsaw[orientation=west_up]', {
        'id': 'minecraft:jigsaw', 'name': 'minecraft:building_entrance', 'target': 'minecraft:building_entrance',
        'pool': f'minecraft:village/{vtype}/streets', 'joint': 'aligned',
        'final_state': f'{st["step"]}[facing=east,half=bottom,shape=straight,waterlogged=false]',
        'placement_priority': 0, 'selection_priority': 0})
    t.set(4, 0, 4, 'minecraft:jigsaw[orientation=up_north]', {
        'id': 'minecraft:jigsaw', 'name': 'minecraft:bottom', 'target': 'minecraft:bottom',
        'pool': f'minecraft:village/{vtype}/villagers', 'joint': 'rollable', 'final_state': 'highcraft:lab_tiles',
        'placement_priority': 0, 'selection_priority': 0})
    return t

def run():
    for vtype, style in STYLES.items():
        drug_lab(vtype, style).save(os.path.join(ROOT, vtype, 'drug_lab.nbt'))
    return len(STYLES)

if __name__ == '__main__':
    print('structures', run())
