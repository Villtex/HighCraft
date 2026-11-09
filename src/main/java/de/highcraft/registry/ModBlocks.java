package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HighCraft.MODID);

    private static BlockBehaviour.Properties crop() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
                .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties wild() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties machine(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(3.0f, 6.0f).requiresCorrectToolForDrops()
                .sound(SoundType.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .lightLevel(s -> s.getValue(MachineBlock.LIT) ? 7 : 0);
    }

    // ---------------- Plants ----------------
    public static final DeferredBlock<CannabisCropBlock> CANNABIS_CROP = BLOCKS.register("cannabis_crop",
            () -> new CannabisCropBlock(crop()));
    public static final DeferredBlock<TobaccoCropBlock> TOBACCO_CROP = BLOCKS.register("tobacco_crop",
            () -> new TobaccoCropBlock(crop()));
    public static final DeferredBlock<PoppyCropBlock> POPPY_CROP = BLOCKS.register("poppy_crop",
            () -> new PoppyCropBlock(crop()));
    public static final DeferredBlock<CocaBushBlock> COCA_BUSH = BLOCKS.register("coca_bush",
            () -> new CocaBushBlock(crop().sound(SoundType.SWEET_BERRY_BUSH)));
    public static final DeferredBlock<MagicMushroomBlock> MAGIC_MUSHROOM = BLOCKS.register("magic_mushroom",
            () -> new MagicMushroomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission()
                    .randomTicks().instabreak().sound(SoundType.FUNGUS).lightLevel(s -> s.getValue(MagicMushroomBlock.AGE) == 2 ? 3 : 1)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<WildPlantBlock> WILD_CANNABIS = BLOCKS.register("wild_cannabis", () -> new WildPlantBlock(wild()));
    public static final DeferredBlock<WildPlantBlock> WILD_TOBACCO = BLOCKS.register("wild_tobacco", () -> new WildPlantBlock(wild()));
    public static final DeferredBlock<WildPlantBlock> WILD_COCA = BLOCKS.register("wild_coca", () -> new WildPlantBlock(wild()));
    public static final DeferredBlock<WildPlantBlock> WILD_POPPY = BLOCKS.register("wild_poppy", () -> new WildPlantBlock(wild()));

    // ---------------- Machines & equipment ----------------
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.register("drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<MachineBlock> GRINDER = BLOCKS.register("grinder",
            () -> new MachineBlock(machine(MapColor.STONE).sound(SoundType.STONE), MachineType.GRINDER));
    public static final DeferredBlock<MachineBlock> EXTRACTOR = BLOCKS.register("extractor",
            () -> new MachineBlock(machine(MapColor.METAL).noOcclusion(), MachineType.EXTRACTOR));
    public static final DeferredBlock<MachineBlock> LAB_STATION = BLOCKS.register("lab_station",
            () -> new MachineBlock(machine(MapColor.COLOR_LIGHT_GRAY).noOcclusion(), MachineType.LAB));
    public static final DeferredBlock<MachineBlock> PRESS = BLOCKS.register("press",
            () -> new MachineBlock(machine(MapColor.METAL).noOcclusion(), MachineType.PRESS));
    /** Two blocks tall (lower half: block entity, upper half: roof + lamp). */
    public static final DeferredBlock<GrowTentBlock> GROW_TENT = BLOCKS.register("grow_tent",
            () -> new GrowTentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0f)
                    .sound(SoundType.WOOL).noOcclusion().lightLevel(s -> s.getValue(MachineBlock.LIT) ? 10 : 0)));
    public static final DeferredBlock<GrowLampBlock> GROW_LAMP = BLOCKS.register("grow_lamp",
            () -> new GrowLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0f)
                    .sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()));
    public static final DeferredBlock<BongBlock> BONG = BLOCKS.register("bong",
            () -> new BongBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3f)
                    .sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<ScaleBlock> PRECISION_SCALE = BLOCKS.register("precision_scale",
            () -> new ScaleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0f)
                    .sound(SoundType.METAL).noOcclusion()));

    // ---------------- Drug lab ----------------
    private static BlockBehaviour.Properties labMetal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5f, 6.0f).requiresCorrectToolForDrops()
                .sound(SoundType.METAL).noOcclusion();
    }

    private static double[] b(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[]{x1, y1, z1, x2, y2, z2};
    }

    public static final DeferredBlock<MachineBlock> CHEMICAL_REACTOR = BLOCKS.register("chemical_reactor",
            () -> new MachineBlock(machine(MapColor.COLOR_LIGHT_GRAY).noOcclusion(), MachineType.REACTOR));
    public static final DeferredBlock<MachineBlock> CRYSTALLIZER = BLOCKS.register("crystallizer",
            () -> new MachineBlock(machine(MapColor.COLOR_LIGHT_BLUE).noOcclusion(), MachineType.CRYSTALLIZER));
    public static final DeferredBlock<MachineBlock> PILL_PRESS = BLOCKS.register("pill_press",
            () -> new MachineBlock(machine(MapColor.COLOR_GRAY).noOcclusion(), MachineType.PILL_PRESS));

    public static final DeferredBlock<LabDecorBlock> FUME_HOOD = BLOCKS.register("fume_hood",
            () -> new LabDecorBlock(labMetal(), false, b(0, 0, 0, 16, 16, 16)));
    public static final DeferredBlock<LabDecorBlock> EXHAUST_FAN = BLOCKS.register("exhaust_fan",
            () -> new LabDecorBlock(labMetal(), true, b(0, 0, 12, 16, 16, 16)));
    public static final DeferredBlock<Block> LAB_TILES = BLOCKS.register("lab_tiles",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> LAB_TILE_SLAB = BLOCKS.register("lab_tile_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(LAB_TILES.get())));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> LAB_TILE_STAIRS = BLOCKS.register("lab_tile_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(LAB_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LAB_TILES.get())));
    public static final DeferredBlock<Block> HAZARD_STRIPES = BLOCKS.register("hazard_stripes",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<LabDecorBlock> LAB_COUNTER = BLOCKS.register("lab_counter",
            () -> new LabDecorBlock(labMetal().noOcclusion(), false, b(0, 0, 0, 16, 16, 16)));
    public static final DeferredBlock<ShapedDecorBlock> CHEMICAL_BARREL = BLOCKS.register("chemical_barrel",
            () -> new ShapedDecorBlock(labMetal(), b(1.5, 0, 1.5, 14.5, 16, 14.5)));
    public static final DeferredBlock<ShapedDecorBlock> HAZARD_BARREL = BLOCKS.register("hazard_barrel",
            () -> new ShapedDecorBlock(labMetal(), b(1.5, 0, 1.5, 14.5, 16, 14.5)));
    public static final DeferredBlock<ShapedDecorBlock> GAS_CYLINDER = BLOCKS.register("gas_cylinder",
            () -> new ShapedDecorBlock(labMetal(), b(4.5, 0, 4.5, 11.5, 15, 11.5), b(6.5, 15, 6.5, 9.5, 17, 9.5)));
    public static final DeferredBlock<LabDecorBlock> GLASSWARE_SHELF = BLOCKS.register("glassware_shelf",
            () -> new LabDecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD).noOcclusion(),
                    true, b(0, 2, 8, 16, 15, 16)));
    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> PLASTIC_SHEETING = BLOCKS.register("plastic_sheeting",
            () -> new net.minecraft.world.level.block.IronBarsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.4f)
                    .sound(SoundType.WOOL).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<LabDecorBlock> HAZARD_SIGN = BLOCKS.register("hazard_sign",
            () -> new LabDecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.0f).sound(SoundType.METAL)
                    .noOcclusion().noCollission(), true, b(1, 1, 15, 15, 15, 16)));
    public static final DeferredBlock<ShapedDecorBlock> LAB_LIGHT = BLOCKS.register("lab_light",
            () -> new ShapedDecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.6f).sound(SoundType.GLASS)
                    .lightLevel(s -> 15).noOcclusion(), b(1, 14, 3, 15, 16, 13)));

    private ModBlocks() {}

    @SuppressWarnings("unused")
    private static Block unused() { return null; }
}
