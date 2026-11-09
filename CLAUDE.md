# HighCraft - notes for Claude

Drug-themed mod for **NeoForge 1.21.1** (Java 21, ModDevGradle, mojmap). Mod id `highcraft`, package `de.highcraft`.
A second jar is the Create add-on `highcraft_create` (needs Create 6.0.x). Everything is **English only** (code, comments,
lang, docs). All drug "chemistry" uses invented in-game ingredients - keep it that way (no real-world synthesis details).
See `README.md` for features and the changelog. Current version: see `gradle.properties` (`mod_version`).

## Layout
- `src/main/java/de/highcraft/` - mod code: `registry/` (DeferredRegisters), `block/` + `block/entity/` (crops, machines),
  `recipe/` (MachineRecipe with Kind enum, GrowingRecipe, QualityShapelessRecipe), `drug/` (DrugType, DrugDoses, DrugHandler,
  PlayerDrugData attachment, Quality data component), `effect/`, `lab/LabSafety` (ventilation, fumes, coolant, hazmat),
  `world/VillageLabs` (adds lab houses to vanilla village pools via reflection on ServerAboutToStartEvent),
  `client/` (DrugVisuals intensities, DrugPostEffect post shader, HUD vignette, camera/FOV/particles), `compat/jei/`.
- `src/addon/` - Create add-on (only registers SequencedAssemblyItems; recipes are data).
- `src/devtest/` - dev-only automated screenshot/test mod (`DevTest.java`), never shipped.
- `src/main/resources/assets|data` - **generated**, do not hand-edit. Exception: `shaders/program/drugs.fsh` is hand-written.
- `tools/` - Python generators (need Pillow + NumPy):
  - `gen_assets.py` (textures, models, blockstates; uses `tex_plants.py`, `tex_blocks.py`, `tex_lab.py`, `models3d.py`,
    and `tex_items_hd.py` + `hd.py` for the 32x32 item icons), `gen_lang.py` (en_us), `gen_data.py` (recipes, loot, tags,
    worldgen, advancements; also calls `gen_structures.py` for the village lab NBTs via `nbt.py`), `gen_addon.py`.
  - Regenerate all: `python3 tools/gen_assets.py && python3 tools/gen_lang.py && python3 tools/gen_data.py && python3 tools/gen_addon.py`
  - `gen_data.py` and `gen_assets.py` wipe their output folders first - always regenerate rather than patch outputs.

## Build & test
- `./gradlew build` -> `build/libs/highcraft-neoforge-1.21.1-<v>.jar` and `highcraft_create-neoforge-1.21.1-<v>.jar`.
- Headless client: `Xvfb :99 &`, `DISPLAY=:99 LIBGL_ALWAYS_SOFTWARE=1 ./gradlew runClient -Pdevtest -PdevtestOnly=<mode>`;
  modes: `all`, `effects`, `items`, `iris`, `create` (needs `-Puse_create_in_dev=true`), `lab`, `tent`, `icons`,
  `structures` (village lab templates side by side + labs in real villages), `villages` (only the real villages).
  Fresh checkout: DevTest creates the flat quick-play world `run/saves/hctest` itself and skips the first-start
  accessibility screen (no dedicated server / EULA file needed). Screenshots land in
  `run/screenshots/hc_*.png`, results in `run/logs/latest.log` (grep `[DevTest]`). In DevTest, `step(delay, action)` runs the
  action and THEN waits `delay` ticks.
- Optional dev mods via gradle.properties flags: `use_sodium_in_dev`, `use_iris_in_dev`, `use_create_in_dev`.

## Pitfalls already hit
- Use `ModEffects.X.getDelegate()` (not the DeferredHolder) for effect lookups - hashCode differs, map lookups fail.
- Block shapes must be valid for EVERY block state: mods like More Culling query all states while loading; an invalid box
  (min > max) aborts the resource reload and the client hangs on the loading screen (fixed in CannabisCropBlock 1.0.1).
- Item textures are 32x32 (blocks stay 16x16). Item models are written by `gen_items()`; 3D held models use
  `neoforge:separate_transforms` (GUI keeps the 2D icon).
- New MachineType entries must be added to: `MachineType`, `MachineRecipe.Kind`, `ModRecipes`, `ModBlocks`, `ModBlockEntities`
  (switch), `MachineBlock` (shape/BE/particles), `ModMenus`, `HighCraftClient` screens, `ModCapabilities`, the JEI plugin, and
  `MACHINE_LAYOUT` in `tools/tex_blocks.py` (GUI texture).
- Village pieces are placed with "known shape" (no neighbour updates): every pane/stair/fence state in `gen_structures.py` must
  already be final (`/place template` does update shapes, so it can look better than a real village - check `s_village_*` shots).
- Per-tick block entity state (progress, energy, burn time) uses `markUnsaved()` (`level.blockEntityChanged`), not `setChanged()`:
  NeoForge's `setChanged` -> `updateNeighbourForOutputSignal` calls `onNeighborChange` on all 6 neighbours. Inventory changes keep
  `setChanged()` (comparators).
- NeoForge's `SlotItemHandler` doesn't forward `setChanged()` to the handler, so shift-click merges (in-place `grow`) are invisible
  to `onContentsChanged`. `MachineMenu.MachineSlot` fixes that - caches in machine BEs rely on it; `craft()` re-validates anyway.
- The grow tent is 2 blocks tall (`GrowTentBlock`, `half` property): only the lower half has the BE; the upper half forwards
  clicks/comparator/capabilities (`ModCapabilities` registerBlock) and must `invalidateCapabilities` on place/remove.
- `gen_data.py` rewrites the village lab `.nbt` files with a new gzip timestamp - if their content didn't change, `git checkout` them.
- The user's target is a large Fantasy modpack (~200 mods: Create 6.0.10, Sodium 0.8, Iris, ModernFix, Sinytra Connector,
  More Culling, Sable/Veil, FancyMenu). Test risky changes against such mods.
