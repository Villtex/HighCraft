"""Datapack content: recipes, loot tables, tags, worldgen, damage type, loot modifiers, advancements."""
import json, os, shutil
DATA = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'data')
NS = 'highcraft'

def w(rel, obj):
    path = os.path.join(DATA, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)

def it(name): return name if ':' in name else f'{NS}:{name}'
def ing(name, count=None):
    d = {'tag': name[1:]} if name.startswith('#') else {'item': it(name)}
    if count: d['count'] = count
    return d
WATER_BOTTLE = {'type': 'neoforge:components', 'items': 'minecraft:potion',
                'components': {'minecraft:potion_contents': {'potion': 'minecraft:water'}}}

# =====================================================================
# Crafting
# =====================================================================
def shaped(name, pattern, key, result, count=1, category='misc'):
    w(f'{NS}/recipe/{name}.json', {'type': 'minecraft:crafting_shaped', 'category': category, 'pattern': pattern,
                                   'key': {k: (v if isinstance(v, dict) else ing(v)) for k, v in key.items()},
                                   'result': {'id': it(result), 'count': count}})

def shapeless(name, ingredients, result, count=1, quality=False, category='misc'):
    w(f'{NS}/recipe/{name}.json', {'type': f'{NS}:quality_shapeless' if quality else 'minecraft:crafting_shapeless', 'category': category,
                                   'ingredients': [i if isinstance(i, dict) else ing(i) for i in ingredients],
                                   'result': {'id': it(result), 'count': count}})

def crafting():
    shapeless('rolling_paper', ['minecraft:paper', 'hemp_fiber'], 'rolling_paper', 8)
    shapeless('hemp_fiber', ['cannabis_leaf', 'cannabis_leaf', 'cannabis_leaf'], 'hemp_fiber', 2)
    shapeless('string_from_hemp', ['hemp_fiber', 'hemp_fiber'], 'minecraft:string', 1)
    shaped('paper_from_hemp', ['FFF'], {'F': 'hemp_fiber'}, 'minecraft:paper', 3)
    shapeless('joint', ['rolling_paper', 'ground_cannabis', 'ground_cannabis'], 'joint', 1, quality=True)
    shapeless('spliff', ['rolling_paper', 'ground_cannabis', 'cut_tobacco'], 'spliff', 1, quality=True)
    shapeless('cigarette', ['rolling_paper', 'rolling_paper', 'cut_tobacco'], 'cigarette', 2, quality=True)
    shapeless('cigar', ['dried_tobacco', 'dried_tobacco', 'dried_tobacco', 'tobacco_leaf'], 'cigar', 1, quality=True)
    shapeless('space_cookie', ['ground_cannabis', 'minecraft:wheat', 'minecraft:wheat', 'minecraft:cocoa_beans'], 'space_cookie', 4, quality=True, category='misc')
    shapeless('coca_tea', ['dried_coca_leaves', 'dried_coca_leaves', WATER_BOTTLE], 'coca_tea', 1, quality=True)
    shapeless('mushroom_tea', ['dried_magic_mushroom', 'dried_magic_mushroom', 'minecraft:sugar', WATER_BOTTLE], 'mushroom_tea', 1, quality=True)
    shapeless('detox_tea', [WATER_BOTTLE, 'minecraft:oxeye_daisy', 'minecraft:sweet_berries'], 'detox_tea', 1)
    shapeless('lsd_blotter', ['lsd_solution', 'minecraft:paper'], 'lsd_blotter', 6, quality=True)
    shapeless('morphine_syringe', ['morphine', 'empty_syringe'], 'morphine_syringe', 1, quality=True)
    shapeless('poppy_seeds', ['minecraft:poppy', 'minecraft:poppy'], 'poppy_seeds', 1)
    shapeless('mushroom_spores', ['minecraft:brown_mushroom', 'minecraft:glow_berries', 'minecraft:fermented_spider_eye'], 'mushroom_spores', 2)
    shapeless('fertilizer', ['minecraft:bone_meal', 'minecraft:rotten_flesh', 'minecraft:dirt'], 'fertilizer', 4)
    shapeless('fertilizer_compost', ['minecraft:bone_meal', 'cannabis_leaf', 'cannabis_leaf'], 'fertilizer', 2)
    shapeless('reagent', ['minecraft:glass_bottle', 'minecraft:redstone', 'minecraft:gunpowder', 'minecraft:sugar'], 'reagent', 2)
    shaped('solvent', [' I ', 'ICI', ' W '], {'I': 'minecraft:iron_ingot', 'C': '#minecraft:coals', 'W': 'minecraft:water_bucket'}, 'solvent', 4)
    shaped('empty_syringe', ['  N', ' G ', 'I  '], {'N': 'minecraft:iron_nugget', 'G': 'minecraft:glass', 'I': 'minecraft:iron_ingot'}, 'empty_syringe', 2)
    shaped('scoring_knife', [' I', 'S '], {'I': 'minecraft:iron_ingot', 'S': 'minecraft:stick'}, 'scoring_knife', 1, 'equipment')
    shaped('smoking_pipe', ['  P', 'SSP'], {'P': '#minecraft:planks', 'S': 'minecraft:stick'}, 'smoking_pipe', 1, 'equipment')
    shaped('lighter', ['F', 'I', 'D'], {'F': 'minecraft:flint', 'I': 'minecraft:iron_ingot', 'D': 'minecraft:red_dye'}, 'lighter', 1, 'equipment')
    shaped('drug_test', ['P', 'R', 'P'], {'P': 'minecraft:paper', 'R': 'minecraft:redstone'}, 'drug_test', 1, 'equipment')
    # Machines
    shaped('drying_rack', ['SSS', 'P P', 'P P'], {'S': 'minecraft:string', 'P': '#minecraft:planks'}, 'drying_rack', 1, 'building')
    shaped('grinder', ['IHI', 'SGS', 'SSS'], {'I': 'minecraft:iron_ingot', 'H': 'minecraft:hopper', 'G': 'minecraft:grindstone', 'S': 'minecraft:smooth_stone'}, 'grinder', 1, 'building')
    shaped('extractor', ['ICI', 'GBG', 'III'], {'I': 'minecraft:iron_ingot', 'C': 'minecraft:copper_ingot', 'G': 'minecraft:glass', 'B': 'minecraft:brewing_stand'}, 'extractor', 1, 'building')
    shaped('lab_station', ['FBF', 'QQQ', 'ICI'], {'F': 'minecraft:glass_bottle', 'B': 'minecraft:brewing_stand', 'Q': 'minecraft:white_concrete',
                                                    'I': 'minecraft:iron_ingot', 'C': 'minecraft:crafting_table'}, 'lab_station', 1, 'building')
    shaped('press', ['IPI', 'IBI', 'SSS'], {'I': 'minecraft:iron_ingot', 'P': 'minecraft:piston', 'B': 'minecraft:iron_block', 'S': 'minecraft:smooth_stone'}, 'press', 1, 'building')
    shaped('grow_lamp', ['IRI', 'GGG'], {'I': 'minecraft:iron_ingot', 'R': 'minecraft:redstone', 'G': 'minecraft:glowstone_dust'}, 'grow_lamp', 1, 'building')
    shaped('grow_tent', ['WWW', 'WLW', 'WDW'], {'W': 'minecraft:black_wool', 'L': 'grow_lamp', 'D': 'minecraft:dirt'}, 'grow_tent', 1, 'building')
    shaped('bong', [' G ', 'GNG', 'GGG'], {'G': 'minecraft:glass', 'N': 'minecraft:iron_nugget'}, 'bong', 1, 'building')
    shaped('precision_scale', ['HRH', 'III'], {'H': 'minecraft:heavy_weighted_pressure_plate', 'R': 'minecraft:redstone', 'I': 'minecraft:iron_ingot'}, 'precision_scale', 1, 'building')

# =====================================================================
# Machine recipes
# =====================================================================
def machine(kind, name, ingredients, result, count=1, time=None):
    r = {'type': f'{NS}:{kind}', 'ingredients': [ing(n, c) for n, c in ingredients], 'result': {'id': it(result), 'count': count}}
    if time: r['processing_time'] = time
    w(f'{NS}/recipe/{kind}/{name}.json', r)

def growing(name, seed, outputs, display, time, water):
    w(f'{NS}/recipe/growing/{name}.json', {'type': f'{NS}:growing', 'seed': ing(seed),
                                           'outputs': [{'item': {'id': it(i), 'count': c}, 'chance': ch} for i, c, ch in outputs],
                                           'display_block': it(display), 'growth_time': time, 'water': water})

def machines():
    machine('drying', 'cannabis_bud', [('cannabis_bud', 1)], 'dried_cannabis_bud', time=1200)
    machine('drying', 'tobacco_leaf', [('tobacco_leaf', 1)], 'dried_tobacco', time=1200)
    machine('drying', 'coca_leaves', [('coca_leaves', 1)], 'dried_coca_leaves', time=900)
    machine('drying', 'raw_opium', [('raw_opium', 1)], 'opium', time=1500)
    machine('drying', 'magic_mushroom', [('magic_mushroom', 1)], 'dried_magic_mushroom', time=900)
    machine('grinding', 'cannabis', [('dried_cannabis_bud', 1)], 'ground_cannabis', 2, 160)
    machine('grinding', 'tobacco', [('dried_tobacco', 1)], 'cut_tobacco', 2, 160)
    machine('grinding', 'poppy_pod', [('poppy_pod', 1)], 'poppy_seeds', 3, 100)
    machine('grinding', 'hemp_fiber', [('cannabis_leaf', 2)], 'hemp_fiber', 3, 120)
    machine('grinding', 'bone_meal', [('minecraft:bone', 1)], 'minecraft:bone_meal', 5, 80)
    machine('extracting', 'cannabis_resin', [('dried_cannabis_bud', 2), ('solvent', 1)], 'cannabis_resin', 3, 240)
    machine('extracting', 'cannabis_resin_leaves', [('cannabis_leaf', 4), ('solvent', 1)], 'cannabis_resin', 1, 240)
    machine('extracting', 'coca_paste', [('dried_coca_leaves', 4), ('solvent', 1)], 'coca_paste', 1, 300)
    machine('extracting', 'raw_opium', [('poppy_pod', 3), ('solvent', 1)], 'raw_opium', 1, 300)
    machine('lab', 'cocaine', [('coca_paste', 1), ('reagent', 1), ('solvent', 1)], 'cocaine', 2, 400)
    machine('lab', 'morphine', [('opium', 1), ('reagent', 1), ('minecraft:glass_bottle', 1)], 'morphine', 1, 360)
    machine('lab', 'lsd_solution', [('ergot', 2), ('reagent', 1), ('solvent', 1)], 'lsd_solution', 1, 480)
    machine('pressing', 'hashish', [('cannabis_resin', 2)], 'hashish', 1, 200)
    machine('pressing', 'paper', [('hemp_fiber', 3)], 'minecraft:paper', 4, 100)
    growing('cannabis', 'cannabis_seeds', [('cannabis_bud', 3, 1.0), ('cannabis_leaf', 2, 1.0), ('cannabis_seeds', 1, 1.0), ('cannabis_seeds', 1, 0.3)],
            'cannabis_crop', 3600, 500)
    growing('tobacco', 'tobacco_seeds', [('tobacco_leaf', 4, 1.0), ('tobacco_seeds', 1, 1.0), ('tobacco_seeds', 1, 0.3)], 'tobacco_crop', 2400, 250)
    growing('coca', 'coca_seeds', [('coca_leaves', 5, 1.0), ('coca_seeds', 1, 1.0), ('coca_seeds', 1, 0.2)], 'coca_bush', 3000, 300)
    growing('poppy', 'poppy_seeds', [('poppy_pod', 2, 1.0), ('poppy_seeds', 2, 1.0), ('raw_opium', 1, 0.4)], 'poppy_crop', 3000, 250)
    growing('magic_mushroom', 'mushroom_spores', [('magic_mushroom', 3, 1.0), ('mushroom_spores', 1, 1.0), ('mushroom_spores', 1, 0.3)],
            'magic_mushroom', 2400, 100)

# =====================================================================
# Loot tables
# =====================================================================
def cond_state(block, props):
    return {'condition': 'minecraft:block_state_property', 'block': it(block), 'properties': props}

QUALITY = {'function': f'{NS}:crop_quality'}
def count(lo, hi): return {'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}
FORTUNE = {'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:binomial_with_bonus_count',
           'parameters': {'extra': 2, 'probability': 0.5714286}}

def entry(item, funcs=None, conds=None):
    e = {'type': 'minecraft:item', 'name': it(item)}
    if funcs: e['functions'] = funcs
    if conds: e['conditions'] = conds
    return e

def pool(entries, conds=None, rolls=1):
    p = {'rolls': rolls, 'bonus_rolls': 0, 'entries': entries}
    if conds: p['conditions'] = conds
    return p

def table(name, pools):
    w(f'{NS}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': pools, 'random_sequence': f'{NS}:blocks/{name}'})

def self_drop(name):
    table(name, [pool([entry(name)], [{'condition': 'minecraft:survives_explosion'}])])

def loot():
    for b in ('drying_rack', 'grinder', 'extractor', 'lab_station', 'press', 'grow_tent', 'grow_lamp', 'bong', 'precision_scale'):
        self_drop(b)
    lower = {'half': 'lower'}
    mature_c = cond_state('cannabis_crop', {'age': '7', 'half': 'lower'})
    table('cannabis_crop', [
        pool([entry('cannabis_seeds')], [cond_state('cannabis_crop', lower)]),
        pool([entry('cannabis_bud', [count(2, 4), QUALITY])], [mature_c]),
        pool([entry('cannabis_leaf', [count(1, 3), QUALITY])], [mature_c]),
        pool([entry('cannabis_seeds', [FORTUNE])], [mature_c]),
    ])
    mature_t = cond_state('tobacco_crop', {'age': '7'})
    table('tobacco_crop', [
        pool([entry('tobacco_seeds')]),
        pool([entry('tobacco_leaf', [count(3, 5), QUALITY])], [mature_t]),
        pool([entry('tobacco_seeds', [FORTUNE])], [mature_t]),
    ])
    mature_p = cond_state('poppy_crop', {'age': '7'})
    table('poppy_crop', [
        pool([entry('poppy_seeds')]),
        pool([entry('poppy_pod', [count(1, 3), QUALITY])], [mature_p]),
        pool([entry('poppy_seeds', [FORTUNE])], [mature_p]),
        pool([entry('minecraft:red_dye')], [mature_p, {'condition': 'minecraft:random_chance', 'chance': 0.3}]),
    ])
    table('coca_bush', [
        pool([entry('coca_seeds')]),
        pool([entry('coca_leaves', [count(2, 4), QUALITY])], [cond_state('coca_bush', {'age': '4'})]),
        pool([entry('coca_leaves', [count(1, 2), QUALITY])], [cond_state('coca_bush', {'age': '3'})]),
    ])
    table('magic_mushroom', [
        pool([entry('mushroom_spores')]),
        pool([entry('magic_mushroom', [count(1, 2), QUALITY])], [cond_state('magic_mushroom', {'age': '2'})]),
    ])
    shears = {'condition': 'minecraft:match_tool', 'predicate': {'items': 'minecraft:shears'}}
    wild = {'wild_cannabis': [('cannabis_seeds', 1, 2), ('cannabis_leaf', 1, 2)], 'wild_tobacco': [('tobacco_seeds', 1, 2), ('tobacco_leaf', 1, 2)],
            'wild_coca': [('coca_seeds', 1, 1), ('coca_leaves', 1, 3)], 'wild_poppy': [('poppy_seeds', 1, 2), ('poppy_pod', 0, 1)]}
    for name, drops in wild.items():
        pools = [pool([{'type': 'minecraft:alternatives', 'children': [entry(name, conds=[shears]), entry(drops[0][0], [count(drops[0][1], drops[0][2])])]}])]
        pools.append(pool([entry(drops[1][0], [count(drops[1][1], drops[1][2])])], [{'condition': 'minecraft:inverted', 'term': shears}]))
        table(name, pools)
    # Seeds from grass
    w(f'{NS}/loot_table/inject/grass_seeds.json', {'type': 'minecraft:block', 'pools': [pool([
        {'type': 'minecraft:item', 'name': it('cannabis_seeds'), 'weight': 6},
        {'type': 'minecraft:item', 'name': it('tobacco_seeds'), 'weight': 5},
        {'type': 'minecraft:item', 'name': it('poppy_seeds'), 'weight': 3},
        {'type': 'minecraft:item', 'name': it('coca_seeds'), 'weight': 1},
    ])]})
    w('neoforge/loot_modifiers/global_loot_modifiers.json', {'replace': False, 'entries': [f'{NS}:grass_seeds', f'{NS}:tall_grass_seeds', f'{NS}:ergot_from_wheat']})
    # Ergot: rare fungal infection of mature wheat
    w(f'{NS}/loot_table/inject/ergot.json', {'type': 'minecraft:block', 'pools': [pool([entry('ergot', [count(1, 2)])])]})
    w(f'{NS}/loot_modifiers/ergot_from_wheat.json', {'type': 'neoforge:add_table', 'conditions': [
        {'condition': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:blocks/wheat'},
        {'condition': 'minecraft:block_state_property', 'block': 'minecraft:wheat', 'properties': {'age': '7'}},
        {'condition': 'minecraft:random_chance', 'chance': 0.05}], 'table': f'{NS}:inject/ergot'})
    no_shears = {'condition': 'minecraft:inverted', 'term': {'condition': 'minecraft:match_tool', 'predicate': {'items': 'minecraft:shears'}}}
    for name, table_id in (('grass_seeds', 'minecraft:blocks/short_grass'), ('tall_grass_seeds', 'minecraft:blocks/tall_grass')):
        w(f'{NS}/loot_modifiers/{name}.json', {'type': 'neoforge:add_table', 'conditions': [
            {'condition': 'neoforge:loot_table_id', 'loot_table_id': table_id}, no_shears,
            {'condition': 'minecraft:random_chance', 'chance': 0.06}], 'table': f'{NS}:inject/grass_seeds'})

# =====================================================================
# Tags
# =====================================================================
def tag(path, values, replace=False):
    full = os.path.join(DATA, path)
    if os.path.exists(full):  # several calls for the same tag in one run: merge
        old = json.load(open(full, encoding='utf-8'))['values']
        values = old + [v for v in values if v not in old]
    w(path, {'replace': replace, 'values': values})

def tags():
    tag('minecraft/tags/block/mineable/pickaxe.json', [it(b) for b in ('grinder', 'extractor', 'lab_station', 'press', 'grow_lamp', 'precision_scale')])
    tag('minecraft/tags/block/mineable/axe.json', [it('drying_rack')])
    tag('minecraft/tags/block/crops.json', [it(b) for b in ('cannabis_crop', 'tobacco_crop', 'poppy_crop', 'coca_bush')])
    tag('minecraft/tags/block/bee_growables.json', [it(b) for b in ('cannabis_crop', 'tobacco_crop', 'poppy_crop', 'coca_bush')])
    tag('minecraft/tags/block/maintains_farmland.json', [it(b) for b in ('cannabis_crop', 'tobacco_crop', 'poppy_crop', 'coca_bush')])
    tag('minecraft/tags/block/sword_efficient.json', [it(b) for b in ('cannabis_crop', 'tobacco_crop', 'poppy_crop', 'coca_bush', 'wild_cannabis', 'wild_tobacco', 'wild_coca', 'wild_poppy')])
    tag('minecraft/tags/block/replaceable_by_trees.json', [it(b) for b in ('wild_cannabis', 'wild_tobacco', 'wild_coca', 'wild_poppy')])
    tag('minecraft/tags/point_of_interest_type/acquirable_job_site.json', [f'{NS}:dealer', f'{NS}:grower', f'{NS}:chemist'])
    tag('c/tags/item/seeds.json', [it(i) for i in ('cannabis_seeds', 'tobacco_seeds', 'coca_seeds', 'poppy_seeds')])
    tag('c/tags/item/crops.json', [it(i) for i in ('cannabis_bud', 'tobacco_leaf', 'coca_leaves', 'poppy_pod')])
    tag(f'{NS}/tags/item/smokables.json', [it(i) for i in ('ground_cannabis', 'cut_tobacco', 'opium')])
    tag('minecraft/tags/damage_type/bypasses_armor.json', [f'{NS}:overdose'])
    tag('minecraft/tags/damage_type/bypasses_shield.json', [f'{NS}:overdose'])
    tag('minecraft/tags/damage_type/no_knockback.json', [f'{NS}:overdose'])
    tag('minecraft/tags/damage_type/bypasses_effects.json', [f'{NS}:overdose'])
    # Biomes
    biomes = {
        'spawns_wild_cannabis': ['minecraft:plains', 'minecraft:sunflower_plains', 'minecraft:meadow', 'minecraft:savanna', 'minecraft:forest'],
        'spawns_wild_tobacco': ['minecraft:savanna', 'minecraft:savanna_plateau', 'minecraft:plains', 'minecraft:wooded_badlands'],
        'spawns_wild_coca': ['#minecraft:is_jungle'],
        'spawns_wild_poppy': ['minecraft:flower_forest', 'minecraft:meadow', 'minecraft:windswept_hills', 'minecraft:cherry_grove'],
        'spawns_magic_mushrooms': ['minecraft:dark_forest', 'minecraft:swamp', 'minecraft:mangrove_swamp', 'minecraft:mushroom_fields',
                                   'minecraft:old_growth_spruce_taiga', 'minecraft:old_growth_pine_taiga'],
    }
    for k, v in biomes.items():
        tag(f'{NS}/tags/worldgen/biome/{k}.json', v)

# =====================================================================
# World generation
# =====================================================================
def worldgen():
    plants = {
        'wild_cannabis': ('spawns_wild_cannabis', 20, 24),
        'wild_tobacco': ('spawns_wild_tobacco', 24, 20),
        'wild_coca': ('spawns_wild_coca', 12, 24),
        'wild_poppy': ('spawns_wild_poppy', 16, 28),
    }
    for name, (biome_tag, rarity, tries) in plants.items():
        block_state = {'Name': it(name)}
        w(f'{NS}/worldgen/configured_feature/{name}_patch.json', {'type': 'minecraft:random_patch', 'config': {
            'tries': tries, 'xz_spread': 6, 'y_spread': 2,
            'feature': {'feature': {'type': 'minecraft:simple_block', 'config': {'to_place': {'type': 'minecraft:simple_state_provider', 'state': block_state}}},
                        'placement': [{'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
                            {'type': 'minecraft:matching_blocks', 'blocks': 'minecraft:air'},
                            {'type': 'minecraft:would_survive', 'state': block_state}]}}]}}})
        w(f'{NS}/worldgen/placed_feature/{name}_patch.json', {'feature': f'{NS}:{name}_patch', 'placement': [
            {'type': 'minecraft:rarity_filter', 'chance': rarity}, {'type': 'minecraft:in_square'},
            {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}, {'type': 'minecraft:biome'}]})
        w(f'{NS}/neoforge/biome_modifier/{name}.json', {'type': 'neoforge:add_features', 'biomes': f'#{NS}:{biome_tag}',
                                                         'features': f'{NS}:{name}_patch', 'step': 'vegetal_decoration'})
    mush = {'Name': it('magic_mushroom'), 'Properties': {'age': '2'}}
    w(f'{NS}/worldgen/configured_feature/magic_mushroom_patch.json', {'type': 'minecraft:random_patch', 'config': {
        'tries': 24, 'xz_spread': 5, 'y_spread': 2,
        'feature': {'feature': {'type': 'minecraft:simple_block', 'config': {'to_place': {'type': 'minecraft:weighted_state_provider', 'entries': [
            {'weight': 3, 'data': mush}, {'weight': 1, 'data': {'Name': it('magic_mushroom'), 'Properties': {'age': '1'}}}]}}},
                    'placement': [{'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
                        {'type': 'minecraft:matching_blocks', 'blocks': 'minecraft:air'},
                        {'type': 'minecraft:would_survive', 'state': mush}]}}]}}})
    w(f'{NS}/worldgen/placed_feature/magic_mushroom_patch.json', {'feature': f'{NS}:magic_mushroom_patch', 'placement': [
        {'type': 'minecraft:rarity_filter', 'chance': 10}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}, {'type': 'minecraft:biome'}]})
    w(f'{NS}/neoforge/biome_modifier/magic_mushrooms.json', {'type': 'neoforge:add_features', 'biomes': f'#{NS}:spawns_magic_mushrooms',
                                                              'features': f'{NS}:magic_mushroom_patch', 'step': 'vegetal_decoration'})

def misc():
    w(f'{NS}/damage_type/overdose.json', {'message_id': f'{NS}.overdose', 'exhaustion': 0.0, 'scaling': 'never', 'effects': 'hurt'})

# =====================================================================
# Advancements
# =====================================================================
def adv(name, icon, parent, criteria, frame='task', hidden=False, background=None):
    d = {'display': {'icon': {'id': it(icon)}, 'title': {'translate': f'advancements.{NS}.{name}.title'},
                     'description': {'translate': f'advancements.{NS}.{name}.description'}, 'frame': frame,
                     'show_toast': name != 'root', 'announce_to_chat': name != 'root', 'hidden': hidden},
         'criteria': criteria}
    if background: d['display']['background'] = background
    if parent: d['parent'] = f'{NS}:{parent}'
    w(f'{NS}/advancement/{name}.json', d)

def has_item(*items):
    return {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': [it(i) for i in items]}]}}

def advancements():
    adv('root', 'cannabis_leaf', None, {'seeds': has_item('cannabis_seeds', 'tobacco_seeds', 'coca_seeds', 'poppy_seeds', 'mushroom_spores')},
        background='minecraft:textures/block/moss_block.png')
    adv('first_seed', 'cannabis_seeds', 'root', {'seeds': has_item('cannabis_seeds')})
    adv('first_harvest', 'cannabis_bud', 'first_seed', {'bud': has_item('cannabis_bud')})
    adv('dried', 'dried_cannabis_bud', 'first_harvest', {'dried': has_item('dried_cannabis_bud')})
    adv('joint', 'joint', 'dried', {'joint': {'trigger': 'minecraft:consume_item', 'conditions': {'item': {'items': it('joint')}}}})
    adv('premium', 'dried_cannabis_bud', 'dried', {'q5': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [
        {'components': {f'{NS}:quality': 5}}]}}}, frame='goal')
    adv('lab', 'cocaine', 'first_harvest', {'cocaine': has_item('cocaine')}, frame='goal')
    adv('trip', 'magic_mushroom', 'root', {'trip': {'trigger': 'minecraft:effects_changed', 'conditions': {'effects': {f'{NS}:trip': {}}}}})
    adv('overdose', 'morphine_syringe', 'root', {'od': {'trigger': 'minecraft:effects_changed', 'conditions': {'effects': {f'{NS}:overdose': {}}}}},
        frame='challenge', hidden=True)
    adv('grow_tent', 'grow_tent', 'first_seed', {'tent': {'trigger': 'minecraft:placed_block', 'conditions': {'location': [
        {'condition': 'minecraft:block_state_property', 'block': it('grow_tent')}]}}}, frame='goal')
    adv('lsd', 'lsd_blotter', 'trip', {'acid': {'trigger': 'minecraft:effects_changed', 'conditions': {'effects': {f'{NS}:acid': {}}}}}, frame='goal')
    adv('detox', 'detox_tea', 'root', {'tea': {'trigger': 'minecraft:consume_item', 'conditions': {'item': {'items': it('detox_tea')}}}})

# =====================================================================
# Meth, ecstasy & drug labs
# =====================================================================
LAB_BLOCKS = ('chemical_reactor', 'crystallizer', 'pill_press', 'fume_hood', 'exhaust_fan', 'lab_tiles', 'lab_tile_stairs',
              'hazard_stripes', 'lab_counter', 'chemical_barrel', 'hazard_barrel', 'gas_cylinder', 'glassware_shelf',
              'plastic_sheeting', 'hazard_sign', 'lab_light')
PILLS = {'pink': 'minecraft:pink_dye', 'blue': 'minecraft:light_blue_dye', 'yellow': 'minecraft:yellow_dye', 'purple': 'minecraft:purple_dye'}

def lab_data():
    # ---- crafting: machines, lab furniture, equipment ----
    shaped('chemical_reactor', ['IGI', 'ICI', 'IFI'], {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass', 'C': 'minecraft:cauldron',
                                                      'F': 'minecraft:blast_furnace'}, 'chemical_reactor', 1, 'building')
    shaped('crystallizer', ['IGI', 'IPI', 'III'], {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass_pane', 'P': 'minecraft:packed_ice'},
           'crystallizer', 1, 'building')
    shaped('pill_press', ['IHI', 'IPI', 'SSS'], {'I': 'minecraft:iron_ingot', 'H': 'minecraft:hopper', 'P': 'minecraft:piston',
                                                'S': 'minecraft:smooth_stone'}, 'pill_press', 1, 'building')
    shaped('exhaust_fan', ['NBN', 'BRB', 'NBN'], {'N': 'minecraft:iron_nugget', 'B': 'minecraft:iron_bars', 'R': 'minecraft:redstone'},
           'exhaust_fan', 1, 'building')
    shaped('fume_hood', ['III', 'GFG', 'III'], {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass_pane', 'F': 'exhaust_fan'},
           'fume_hood', 1, 'building')
    shaped('lab_tiles', ['CC', 'CC'], {'C': 'minecraft:white_concrete'}, 'lab_tiles', 4, 'building')
    shaped('lab_tile_slab', ['TTT'], {'T': 'lab_tiles'}, 'lab_tile_slab', 6, 'building')
    shaped('lab_tile_stairs', ['T  ', 'TT ', 'TTT'], {'T': 'lab_tiles'}, 'lab_tile_stairs', 4, 'building')
    for name, res, n in (('lab_tile_slab_stonecutting', 'lab_tile_slab', 2), ('lab_tile_stairs_stonecutting', 'lab_tile_stairs', 1)):
        w(f'{NS}/recipe/{name}.json', {'type': 'minecraft:stonecutting', 'ingredient': ing('lab_tiles'), 'result': {'id': it(res), 'count': n}})
    shaped('hazard_stripes', ['YB', 'BY'], {'Y': 'minecraft:yellow_concrete', 'B': 'minecraft:black_concrete'}, 'hazard_stripes', 4, 'building')
    shaped('lab_counter', ['SSS', 'PCP', 'PPP'], {'S': 'minecraft:smooth_stone_slab', 'P': '#minecraft:planks', 'C': 'minecraft:chest'},
           'lab_counter', 2, 'building')
    shapeless('chemical_barrel', ['minecraft:barrel', 'minecraft:blue_dye', 'minecraft:iron_ingot'], 'chemical_barrel', 1, category='building')
    shapeless('hazard_barrel', ['minecraft:barrel', 'minecraft:yellow_dye', 'minecraft:black_dye', 'minecraft:iron_ingot'], 'hazard_barrel', 1,
              category='building')
    shaped('gas_cylinder', ['N', 'I', 'I'], {'N': 'minecraft:iron_nugget', 'I': 'minecraft:iron_ingot'}, 'gas_cylinder', 1, 'building')
    shaped('glassware_shelf', ['BBB', 'SSS'], {'B': 'minecraft:glass_bottle', 'S': '#minecraft:wooden_slabs'}, 'glassware_shelf', 1, 'building')
    shaped('plastic_sheeting', ['KKK', 'KKK'], {'K': 'minecraft:dried_kelp'}, 'plastic_sheeting', 16, 'building')
    shapeless('hazard_sign', ['minecraft:yellow_dye', 'minecraft:black_dye', 'minecraft:iron_ingot'], 'hazard_sign', 3, category='building')
    shaped('lab_light', ['NNN', 'GGG'], {'N': 'minecraft:iron_nugget', 'G': 'minecraft:glowstone_dust'}, 'lab_light', 3, 'building')
    shaped('gas_mask', ['LLL', 'GLG', 'CLC'], {'L': 'minecraft:leather', 'G': 'minecraft:glass_pane', 'C': 'minecraft:charcoal'},
           'gas_mask', 1, 'equipment')
    shaped('hazmat_suit', ['K K', 'YKY', 'YYY'], {'K': 'minecraft:dried_kelp', 'Y': 'minecraft:yellow_wool'}, 'hazmat_suit', 1, 'equipment')
    shaped('hazmat_pants', ['YYY', 'K K', 'Y Y'], {'K': 'minecraft:dried_kelp', 'Y': 'minecraft:yellow_wool'}, 'hazmat_pants', 1, 'equipment')
    shaped('hazmat_boots', ['K K', 'B B'], {'K': 'minecraft:dried_kelp', 'B': 'minecraft:black_wool'}, 'hazmat_boots', 1, 'equipment')
    shaped('glass_pipe', ['  B', ' G ', 'G  '], {'B': 'minecraft:glass_bottle', 'G': 'minecraft:glass_pane'}, 'glass_pipe', 1, 'equipment')
    shapeless('catalyst', ['minecraft:iron_nugget', 'minecraft:redstone', 'minecraft:glowstone_dust'], 'catalyst', 4)

    # ---- processing chain (fictional in-game chemistry) ----
    machine('lab', 'precursor', [('minecraft:sugar', 2), ('minecraft:gunpowder', 1), ('reagent', 1)], 'precursor', 2, 300)
    machine('extracting', 'aromatic_oil', [('#minecraft:spruce_logs', 2), ('solvent', 1)], 'aromatic_oil', 1, 280)
    machine('reacting', 'meth_batch', [('precursor', 2), ('catalyst', 1), ('solvent', 1)], 'meth_batch', 1, 400)
    machine('reacting', 'mdma_batch', [('aromatic_oil', 2), ('catalyst', 1), ('reagent', 1)], 'mdma_batch', 1, 440)
    machine('crystallizing', 'crystal_meth', [('meth_batch', 1)], 'crystal_meth', 4, 600)
    machine('crystallizing', 'mdma_crystals', [('mdma_batch', 1)], 'mdma_crystals', 4, 560)
    machine('grinding', 'mdma_powder', [('mdma_crystals', 1)], 'mdma_powder', 2, 120)
    for color, dye in PILLS.items():
        machine('pill_pressing', f'ecstasy_pill_{color}', [('mdma_powder', 1), ('minecraft:sugar', 1), (dye, 1)], f'ecstasy_pill_{color}', 4, 120)

    # ---- loot ----
    for b in LAB_BLOCKS:
        self_drop(b)
    table('lab_tile_slab', [pool([entry('lab_tile_slab', [{'function': 'minecraft:set_count', 'count': 2, 'add': False,
        'conditions': [cond_state('lab_tile_slab', {'type': 'double'})]}, {'function': 'minecraft:explosion_decay'}])])])
    def item_e(name, lo, hi, weight):
        return {'type': 'minecraft:item', 'name': it(name), 'weight': weight, 'functions': [count(lo, hi)]}
    w(f'{NS}/loot_table/chests/village_drug_lab.json', {'type': 'minecraft:chest', 'random_sequence': f'{NS}:chests/village_drug_lab', 'pools': [
        {'rolls': {'type': 'minecraft:uniform', 'min': 3, 'max': 6}, 'bonus_rolls': 0, 'entries': [
            item_e('precursor', 1, 3, 10), item_e('catalyst', 1, 4, 10), item_e('solvent', 1, 2, 8), item_e('reagent', 1, 2, 8),
            item_e('aromatic_oil', 1, 2, 5), item_e('mdma_powder', 1, 3, 4), item_e('crystal_meth', 1, 2, 3),
            item_e('ecstasy_pill_pink', 1, 4, 3), item_e('ecstasy_pill_blue', 1, 4, 3), item_e('ecstasy_pill_yellow', 1, 4, 2),
            item_e('ecstasy_pill_purple', 1, 4, 2), item_e('empty_syringe', 1, 2, 4), item_e('minecraft:glass_bottle', 2, 5, 8),
            item_e('minecraft:emerald', 1, 4, 6), item_e('minecraft:sugar', 2, 6, 6)]},
        {'rolls': 1, 'bonus_rolls': 0, 'entries': [
            item_e('gas_mask', 1, 1, 3), item_e('glass_pipe', 1, 1, 3), item_e('hazmat_boots', 1, 1, 1), item_e('hazmat_pants', 1, 1, 1),
            {'type': 'minecraft:empty', 'weight': 4}]}]})

    # ---- tags ----
    tag('minecraft/tags/block/mineable/pickaxe.json', [it(b) for b in ('chemical_reactor', 'crystallizer', 'pill_press', 'fume_hood',
        'exhaust_fan', 'lab_tiles', 'lab_tile_slab', 'lab_tile_stairs', 'hazard_stripes', 'lab_counter', 'chemical_barrel',
        'hazard_barrel', 'gas_cylinder', 'hazard_sign', 'lab_light')])
    tag('minecraft/tags/block/mineable/axe.json', [it('glassware_shelf')])
    tag('minecraft/tags/block/slabs.json', [it('lab_tile_slab')]); tag('minecraft/tags/item/slabs.json', [it('lab_tile_slab')])
    tag('minecraft/tags/block/stairs.json', [it('lab_tile_stairs')]); tag('minecraft/tags/item/stairs.json', [it('lab_tile_stairs')])
    tag('minecraft/tags/item/head_armor.json', [it('gas_mask')])
    tag('minecraft/tags/item/chest_armor.json', [it('hazmat_suit')])
    tag('minecraft/tags/item/leg_armor.json', [it('hazmat_pants')])
    tag('minecraft/tags/item/foot_armor.json', [it('hazmat_boots')])

    # ---- damage types ----
    w(f'{NS}/damage_type/toxic_fumes.json', {'message_id': f'{NS}.toxic_fumes', 'exhaustion': 0.0, 'scaling': 'never', 'effects': 'hurt'})
    w(f'{NS}/damage_type/lab_accident.json', {'message_id': f'{NS}.lab_accident', 'exhaustion': 0.1, 'scaling': 'never', 'effects': 'hurt'})

    # ---- advancements ----
    adv('crystal', 'crystal_meth', 'lab', {'crystal': has_item('crystal_meth')})
    adv('pill_press', 'ecstasy_pill_pink', 'lab', {'pill': has_item('ecstasy_pill_pink', 'ecstasy_pill_blue', 'ecstasy_pill_yellow', 'ecstasy_pill_purple')})
    adv('euphoria', 'ecstasy_pill_blue', 'pill_press', {'euphoria': {'trigger': 'minecraft:effects_changed', 'conditions': {'effects': {f'{NS}:euphoria': {}}}}})
    adv('safety_first', 'gas_mask', 'lab', {'suit': {'trigger': 'minecraft:inventory_changed', 'conditions': {'player': [
        {'condition': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'equipment': {
            'head': {'items': it('gas_mask')}, 'chest': {'items': it('hazmat_suit')},
            'legs': {'items': it('hazmat_pants')}, 'feet': {'items': it('hazmat_boots')}}}}]}}}, frame='goal')

    # ---- village drug lab structures ----
    import gen_structures
    gen_structures.run()

if __name__ == '__main__':
    for d in (os.path.join(DATA, NS), os.path.join(DATA, 'neoforge'), os.path.join(DATA, 'c'), os.path.join(DATA, 'minecraft')):
        if os.path.isdir(d):
            shutil.rmtree(d)
    crafting(); machines(); loot(); tags(); worldgen(); misc(); advancements(); lab_data()
    n = sum(len(f) for _, _, f in os.walk(DATA))
    print('data ok', n)
