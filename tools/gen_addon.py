"""Assets & data for the "HighCraft: Create" addon (highcraft_create)."""
import json, os, shutil
from px import *
import tex_items_hd as I

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'addon', 'resources')
NS = 'highcraft_create'
HC = 'highcraft'

def w(rel, obj):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)

def it(n):
    return n if ':' in n else f'{HC}:{n}'

def ing(n, count=None):
    d = {'tag': n[1:]} if n.startswith('#') else {'item': it(n)}
    return d

def res(n, count=1, chance=None):
    d = {'id': it(n)}
    if count != 1: d['count'] = count
    if chance is not None: d['chance'] = chance
    return d

def fluid(f, amount):
    return {'type': 'neoforge:single', 'amount': amount, 'fluid': f}

def recipe(kind, name, ingredients, results, time=None, heat=None, extra=None):
    """Create processing recipe. Repeat an ingredient in the list to require it several times."""
    r = {'type': f'create:{kind}', 'ingredients': ingredients, 'results': results}
    if time: r['processing_time'] = time
    if heat: r['heat_requirement'] = heat
    if extra: r.update(extra)
    w(f'data/{NS}/recipe/{kind}/{name}.json', r)

def many(n, k):
    return [ing(n)] * k

# =====================================================================
# Textures of the intermediate items (sequenced assembly)
# =====================================================================
def textures():
    for name, cv in I.addon_items().items():
        cv.save(os.path.join(ROOT, 'assets', NS, 'textures', 'item', name + '.png'))
        w(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{name}'}})

def lang():
    en = {
        'item.highcraft_create.incomplete_joint': 'Incomplete Joint',
        'item.highcraft_create.incomplete_spliff': 'Incomplete Spliff',
        'item.highcraft_create.incomplete_cigarette': 'Incomplete Cigarette',
        'item.highcraft_create.incomplete_cigar': 'Incomplete Cigar',
        'item.highcraft_create.incomplete_blotter_sheet': 'Incomplete Blotter Sheet',
        'item.highcraft_create.incomplete_syringe': 'Incomplete Syringe',
    }
    w(f'assets/{NS}/lang/en_us.json', en)

# =====================================================================
# Recipes
# =====================================================================
def recipes():
    # ---- Millstone ----
    recipe('milling', 'dried_cannabis_bud', [ing('dried_cannabis_bud')], [res('ground_cannabis', 2), res('ground_cannabis', 1, 0.25)], 120)
    recipe('milling', 'dried_tobacco', [ing('dried_tobacco')], [res('cut_tobacco', 2), res('cut_tobacco', 1, 0.25)], 120)
    recipe('milling', 'poppy_pod', [ing('poppy_pod')], [res('poppy_seeds', 3)], 100)
    recipe('milling', 'cannabis_leaf', [ing('cannabis_leaf')], [res('hemp_fiber', 1), res('hemp_fiber', 1, 0.5)], 100)
    # ---- Crushing wheels (more yield, by-products) ----
    recipe('crushing', 'dried_cannabis_bud', [ing('dried_cannabis_bud')], [res('ground_cannabis', 3), res('cannabis_seeds', 1, 0.1)], 150)
    recipe('crushing', 'dried_tobacco', [ing('dried_tobacco')], [res('cut_tobacco', 3)], 150)
    recipe('crushing', 'poppy_pod', [ing('poppy_pod')], [res('poppy_seeds', 3), res('raw_opium', 1, 0.25)], 150)
    recipe('crushing', 'cannabis_leaf', [ing('cannabis_leaf')], [res('hemp_fiber', 2), res('cannabis_resin', 1, 0.05)], 120)
    # ---- Compacting (basin + mechanical press) ----
    recipe('compacting', 'hashish', many('cannabis_resin', 2), [res('hashish')])
    recipe('compacting', 'paper_from_hemp', many('hemp_fiber', 3), [res('minecraft:paper', 3)])
    # ---- Mixer ----
    recipe('mixing', 'cannabis_resin', many('dried_cannabis_bud', 2) + [ing('solvent')], [res('cannabis_resin', 3)], heat='heated')
    recipe('mixing', 'cannabis_resin_from_leaves', many('cannabis_leaf', 4) + [ing('solvent')], [res('cannabis_resin')])
    recipe('mixing', 'coca_paste', many('dried_coca_leaves', 4) + [ing('solvent')], [res('coca_paste')], heat='heated')
    recipe('mixing', 'cocaine', [ing('coca_paste'), ing('reagent'), ing('solvent')], [res('cocaine', 2)], heat='superheated')
    recipe('mixing', 'morphine', [ing('opium'), ing('reagent'), ing('minecraft:glass_bottle')], [res('morphine')], heat='heated')
    recipe('mixing', 'lsd_solution', many('ergot', 2) + [ing('reagent'), ing('solvent')], [res('lsd_solution')], heat='superheated')
    recipe('mixing', 'raw_opium', many('poppy_pod', 3) + [ing('solvent')], [res('raw_opium')])
    recipe('mixing', 'solvent', [ing('#minecraft:coals'), ing('minecraft:iron_nugget'), ing('minecraft:iron_nugget'), fluid('minecraft:water', 500)],
           [res('solvent', 2)], heat='heated')
    recipe('mixing', 'reagent', [ing('minecraft:glass_bottle'), ing('minecraft:redstone'), ing('minecraft:gunpowder'), ing('minecraft:sugar')], [res('reagent', 2)])
    recipe('mixing', 'fertilizer', [ing('minecraft:bone_meal'), ing('minecraft:rotten_flesh'), ing('minecraft:dirt')], [res('fertilizer', 4)])
    recipe('mixing', 'coca_tea', many('dried_coca_leaves', 2) + [ing('minecraft:glass_bottle'), fluid('minecraft:water', 250)], [res('coca_tea')], heat='heated')
    recipe('mixing', 'mushroom_tea', many('dried_magic_mushroom', 2) + [ing('minecraft:sugar'), ing('minecraft:glass_bottle'), fluid('minecraft:water', 250)],
           [res('mushroom_tea')], heat='heated')
    recipe('mixing', 'detox_tea', [ing('minecraft:oxeye_daisy'), ing('minecraft:sweet_berries'), ing('minecraft:glass_bottle'), fluid('minecraft:water', 250)],
           [res('detox_tea')], heat='heated')
    recipe('mixing', 'space_cookie', [ing('ground_cannabis'), ing('minecraft:wheat'), ing('minecraft:wheat'), fluid('create:chocolate', 250)],
           [res('space_cookie', 4)])
    # ---- Meth & ecstasy (heated mixer = chemical reactor, crushing wheels, compacting = pill press) ----
    recipe('mixing', 'meth_batch', many('precursor', 2) + [ing('catalyst'), ing('solvent')], [res('meth_batch')], heat='superheated')
    recipe('mixing', 'mdma_batch', many('aromatic_oil', 2) + [ing('catalyst'), ing('reagent')], [res('mdma_batch')], heat='heated')
    recipe('mixing', 'precursor', many('minecraft:sugar', 2) + [ing('minecraft:gunpowder'), ing('reagent')], [res('precursor', 2)], heat='heated')
    recipe('crushing', 'mdma_crystals', [ing('mdma_crystals')], [res('mdma_powder', 2), res('mdma_powder', 1, 0.25)], 100)
    recipe('milling', 'mdma_crystals', [ing('mdma_crystals')], [res('mdma_powder', 2)], 120)
    for color, dye in (('pink', 'minecraft:pink_dye'), ('blue', 'minecraft:light_blue_dye'), ('yellow', 'minecraft:yellow_dye'),
                       ('purple', 'minecraft:purple_dye')):
        recipe('compacting', f'ecstasy_pill_{color}', [ing('mdma_powder'), ing('minecraft:sugar'), ing(dye)], [res(f'ecstasy_pill_{color}', 4)])
    # ---- Fan: haunting (soul fire) ----
    recipe('haunting', 'ergot_from_wheat', [ing('minecraft:wheat')], [res('ergot'), res('ergot', 1, 0.25)])
    recipe('haunting', 'poppy_seeds_from_poppy', [ing('minecraft:poppy')], [res('poppy_seeds', 2)])
    recipe('haunting', 'mushroom_spores_from_glow_lichen', [ing('minecraft:glow_lichen')], [res('mushroom_spores')])
    # ---- Fan: smoking = drying (vanilla smoking, also works in the smoker) ----
    for src, dst in (('cannabis_bud', 'dried_cannabis_bud'), ('tobacco_leaf', 'dried_tobacco'), ('coca_leaves', 'dried_coca_leaves'),
                     ('raw_opium', 'opium'), ('magic_mushroom', 'dried_magic_mushroom')):
        w(f'data/{NS}/recipe/smoking/{dst}.json', {'type': 'minecraft:smoking', 'category': 'food', 'ingredient': ing(src),
                                                  'result': {'id': it(dst)}, 'cookingtime': 300, 'experience': 0.1})
    # ---- Mechanical arm / deployer ----
    recipe('deploying', 'morphine_syringe', [ing('empty_syringe'), ing('morphine')], [res('morphine_syringe')])
    recipe('deploying', 'score_poppy_pod', [ing('poppy_pod'), ing('scoring_knife')], [res('raw_opium'), res('poppy_seeds', 1, 0.5)],
           extra={'keep_held_item': True})
    # ---- Sequenced assembly ----
    def seq(name, start, transitional, steps, results, loops=1):
        t = f'{NS}:{transitional}'
        sequence = []
        for kind, item in steps:
            step = {'type': f'create:{kind}', 'ingredients': [{'item': t}] + ([ing(item)] if item else []), 'results': [{'id': t}]}
            sequence.append(step)
        w(f'data/{NS}/recipe/sequenced_assembly/{name}.json', {
            'type': 'create:sequenced_assembly', 'ingredient': ing(start), 'loops': loops,
            'results': results, 'sequence': sequence, 'transitional_item': {'id': t}})
    seq('joint', 'rolling_paper', 'incomplete_joint', [('deploying', 'ground_cannabis'), ('deploying', 'ground_cannabis'), ('pressing', None)],
        [res('joint')])
    seq('spliff', 'rolling_paper', 'incomplete_spliff', [('deploying', 'ground_cannabis'), ('deploying', 'cut_tobacco'), ('pressing', None)],
        [res('spliff')])
    seq('cigarette', 'rolling_paper', 'incomplete_cigarette', [('deploying', 'cut_tobacco'), ('deploying', 'rolling_paper'), ('pressing', None)],
        [res('cigarette', 2)])
    seq('cigar', 'tobacco_leaf', 'incomplete_cigar', [('deploying', 'dried_tobacco'), ('pressing', None)], [res('cigar')], loops=3)
    seq('lsd_blotter', 'minecraft:paper', 'incomplete_blotter_sheet', [('deploying', 'lsd_solution'), ('pressing', None), ('cutting', None)],
        [res('lsd_blotter', 8)])
    seq('empty_syringe', 'minecraft:glass_pane', 'incomplete_syringe', [('deploying', 'minecraft:iron_nugget'), ('deploying', 'minecraft:iron_nugget'),
                                                                        ('pressing', None)], [res('empty_syringe', 3)])

def tags():
    # Bottles & vials stand upright on the belt
    w('data/create/tags/item/upright_on_belt.json', {'replace': False, 'values': [it(n) for n in (
        'coca_tea', 'mushroom_tea', 'detox_tea', 'lsd_solution', 'morphine', 'reagent', 'solvent', 'aromatic_oil', 'meth_batch',
        'mdma_batch', 'precursor')]})

if __name__ == '__main__':
    for d in ('assets', 'data'):
        p = os.path.join(ROOT, d)
        if os.path.isdir(p): shutil.rmtree(p)
    textures(); lang(); recipes(); tags()
    n = sum(len(f) for _, _, f in os.walk(ROOT))
    print('addon ok', n)
