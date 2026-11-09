package de.highcraft.devtest;

import de.highcraft.HighCraft;
import de.highcraft.block.entity.DryingRackBlockEntity;
import de.highcraft.block.entity.GrowTentBlockEntity;
import de.highcraft.compat.jei.HighCraftJeiPlugin;
import de.highcraft.registry.ModBlocks;
import de.highcraft.registry.ModCreativeTabs;
import de.highcraft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Development only: builds a test scene, takes screenshots of plants, machines, GUIs, JEI and effects
 * and quits the game. Only active with -Dhighcraft.devtest=true.
 */
@Mod(value = "highcraft_devtest", dist = Dist.CLIENT)
public class DevTest {
    private static final int Y = 120;
    private final List<Step> steps = new ArrayList<>();
    private int index = 0;
    private int wait = 0;
    private int ticks = 0;

    private record Step(int delay, Consumer<Minecraft> action, java.util.function.Predicate<Minecraft> until) {}
    private int waited = 0;

    public DevTest(IEventBus modBus) {
        if (!Boolean.getBoolean("highcraft.devtest")) return;
        buildPlan();
        NeoForge.EVENT_BUS.addListener(this::onTick);
    }

    private void step(int delay, Consumer<Minecraft> action) { steps.add(new Step(delay, action, null)); }

    /** Waits (max. 900 ticks) until the condition is met. */
    private void waitUntil(java.util.function.Predicate<Minecraft> cond) { steps.add(new Step(0, mc -> {}, cond)); }

    private void effectShot(String id, int index, String name) {
        step(5, mc -> cmd(mc, "effect clear Dev"));
        waitUntil(mc -> !de.highcraft.client.DrugVisuals.anyActive());
        step(10, mc -> cmd(mc, "effect give Dev highcraft:" + id + " 120 1 true"));
        waitUntil(mc -> de.highcraft.client.DrugVisuals.get(index, 1f) > 0.75f);
        step(30, mc -> {});
        step(20, mc -> {
            HighCraft.LOGGER.info("[DevTest] {} intensity={}", id, de.highcraft.client.DrugVisuals.get(index, 1f));
            shot(mc, name);
        });
    }

    private boolean worldRequested;

    /**
     * Fresh checkout: the quick-play world "hctest" doesn't exist yet (quick play then shows an error screen).
     * Create it through the normal client world creation - a flat creative world with cheats.
     */
    private void createTestWorld(Minecraft mc) {
        worldRequested = true;
        HighCraft.LOGGER.info("[DevTest] world 'hctest' missing - creating a flat test world");
        var rules = new net.minecraft.world.level.GameRules();
        var settings = new net.minecraft.world.level.LevelSettings("hctest", net.minecraft.world.level.GameType.CREATIVE, false,
                net.minecraft.world.Difficulty.PEACEFUL, true, rules, net.minecraft.world.level.WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("hctest", settings,
                new net.minecraft.world.level.levelgen.WorldOptions(1L, false, false),
                registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
                        .getHolderOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).value().createWorldDimensions(),
                new net.minecraft.client.gui.screens.TitleScreen());
    }

    private void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null && !worldRequested && mc.getOverlay() == null && mc.screen != null) {
            if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
                // First launch of a fresh run dir: the onboarding screen blocks quick play - skip it and open the world
                mc.options.onboardAccessibility = false;
                mc.options.save();
                if (mc.getLevelSource().levelExists("hctest")) {
                    worldRequested = true;
                    mc.createWorldOpenFlows().openWorld("hctest", () -> mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen()));
                    return;
                }
            }
            if (!mc.getLevelSource().levelExists("hctest")) {
                createTestWorld(mc);
                return;
            }
        }
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        if (++ticks < 60) return;
        if (wait > 0) { wait--; return; }
        if (index >= steps.size()) return;
        Step s = steps.get(index);
        if (s.until != null) {
            if (!s.until.test(mc) && ++waited < 900) return;
            waited = 0;
        }
        index++;
        try {
            s.action.accept(mc);
        } catch (Throwable t) {
            HighCraft.LOGGER.error("[DevTest] step {} failed", index, t);
        }
        wait = s.delay;
    }

    // ------------------------------------------------------------------
    private static void cmd(Minecraft mc, String c) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4).withSuppressedOutput(), c));
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "hc_" + name + ".png", mc.getMainRenderTarget(), msg -> HighCraft.LOGGER.info("[DevTest] Screenshot {}", name));
    }

    private static void tp(Minecraft mc, double x, double y, double z, float yaw, float pitch) {
        cmd(mc, String.format(java.util.Locale.ROOT, "tp Dev %.2f %.2f %.2f %.1f %.1f", x, y, z, yaw, pitch));
    }

    private static String setb(int x, int y, int z, String state) {
        return "setblock " + x + " " + y + " " + z + " " + state;
    }

    private void buildPlan() {
        boolean onlyEffects = "effects".equals(System.getProperty("highcraft.devtest.only"));
        // --- Prepare the world ---
        step(5, mc -> {
            for (String c : new String[]{"gamerule doDaylightCycle false", "gamerule doWeatherCycle false", "gamerule randomTickSpeed 0",
                    "gamerule doMobSpawning false", "time set 6000", "weather clear", "gamemode creative Dev",
                    "fill -4 " + (Y - 1) + " -4 40 " + (Y - 1) + " 40 minecraft:smooth_stone",
                    "fill -4 " + Y + " -4 40 " + Y + " 40 minecraft:grass_block",
                    "fill -4 " + (Y + 1) + " -4 40 " + (Y + 8) + " 40 minecraft:air"}) cmd(mc, c);
        });
        step(5, mc -> {
            List<String> c = new ArrayList<>();
            // Row 1: cannabis 0..7
            c.add("fill 0 " + Y + " 0 15 " + Y + " 0 minecraft:farmland[moisture=7]");
            for (int a = 0; a < 8; a++) {
                c.add(setb(a * 2, Y + 1, 0, "highcraft:cannabis_crop[age=" + a + ",half=lower]"));
                if (a >= 4) c.add(setb(a * 2, Y + 2, 0, "highcraft:cannabis_crop[age=" + a + ",half=upper]"));
            }
            // Row 2: tobacco
            c.add("fill 0 " + Y + " 3 15 " + Y + " 3 minecraft:farmland[moisture=7]");
            for (int a = 0; a < 8; a++) c.add(setb(a * 2, Y + 1, 3, "highcraft:tobacco_crop[age=" + a + "]"));
            // Row 3: poppy
            c.add("fill 0 " + Y + " 6 15 " + Y + " 6 minecraft:farmland[moisture=7]");
            for (int a = 0; a < 8; a++) c.add(setb(a * 2, Y + 1, 6, "highcraft:poppy_crop[age=" + a + "]"));
            // Row 4: coca + mushrooms + wild plants
            for (int a = 0; a < 5; a++) c.add(setb(a * 2, Y + 1, 9, "highcraft:coca_bush[age=" + a + "]"));
            c.add("fill 10 " + Y + " 9 15 " + Y + " 9 minecraft:mycelium");
            for (int a = 0; a < 3; a++) c.add(setb(10 + a * 2, Y + 1, 9, "highcraft:magic_mushroom[age=" + a + "]"));
            c.add(setb(0, Y + 1, 12, "highcraft:wild_cannabis")); c.add(setb(2, Y + 1, 12, "highcraft:wild_tobacco"));
            c.add(setb(4, Y + 1, 12, "highcraft:wild_coca")); c.add(setb(6, Y + 1, 12, "highcraft:wild_poppy"));
            // Row 5: machines
            int z = 16;
            c.add(setb(0, Y + 1, z, "highcraft:drying_rack[facing=north]"));
            c.add(setb(2, Y + 1, z, "highcraft:grinder[facing=north,lit=true]"));
            c.add(setb(4, Y + 1, z, "highcraft:extractor[facing=north,lit=true]"));
            c.add(setb(6, Y + 1, z, "highcraft:lab_station[facing=north,lit=true]"));
            c.add(setb(8, Y + 1, z, "highcraft:press[facing=north,lit=false]"));
            c.add(setb(10, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=true]"));
            c.add(setb(10, Y + 2, z, "highcraft:grow_tent[facing=north,half=upper,lit=true]"));
            c.add(setb(12, Y + 1, z, "highcraft:bong[facing=north,water=true,load=cannabis]"));
            c.add(setb(14, Y + 1, z, "highcraft:precision_scale[facing=north]"));
            c.add(setb(2, Y + 1, z + 3, "highcraft:grinder[facing=north,lit=false]"));
            c.add(setb(4, Y + 1, z + 3, "highcraft:extractor[facing=north,lit=false]"));
            c.add(setb(6, Y + 1, z + 3, "highcraft:lab_station[facing=north,lit=false]"));
            c.add(setb(8, Y + 1, z + 3, "highcraft:press[facing=north,lit=true]"));
            c.add(setb(10, Y + 1, z + 3, "highcraft:grow_tent[facing=north,half=lower,lit=false]"));
            c.add(setb(10, Y + 2, z + 3, "highcraft:grow_tent[facing=north,half=upper,lit=false]"));
            c.add(setb(12, Y + 1, z + 3, "highcraft:bong[facing=north,water=false,load=empty]"));
            // Grow lamp above the cannabis
            c.add(setb(14, Y + 4, 0, "highcraft:grow_lamp"));
            // Villager
            c.add("summon minecraft:villager 18 " + (Y + 1) + " 16 {NoAI:1b,Rotation:[180f,0f],VillagerData:{profession:\"highcraft:dealer\",level:3,type:\"minecraft:plains\"}}");
            c.add("summon minecraft:villager 20 " + (Y + 1) + " 16 {NoAI:1b,Rotation:[180f,0f],VillagerData:{profession:\"highcraft:grower\",level:3,type:\"minecraft:savanna\"}}");
            c.add("summon minecraft:villager 22 " + (Y + 1) + " 16 {NoAI:1b,Rotation:[180f,0f],VillagerData:{profession:\"highcraft:dealer\",level:1,type:\"minecraft:taiga\"}}");
            // Item wall with invisible frames (shows the 3D item models)
            c.add("fill 0 " + (Y + 1) + " 25 15 " + (Y + 4) + " 25 minecraft:smooth_quartz");
            String[] items = {"joint", "spliff", "cigarette", "cigar", "smoking_pipe", "lighter", "empty_syringe", "morphine_syringe",
                    "cannabis_bud", "dried_cannabis_bud", "magic_mushroom", "poppy_pod", "hashish", "lsd_blotter", "solvent", "coca_tea"};
            for (int i = 0; i < items.length; i++) {
                int x = 2 + (i % 8) * 1;
                int y = Y + 3 - (i / 8);
                c.add("summon minecraft:item_frame " + (x + 2) + " " + y + " 24 {Facing:2b,Fixed:1b,Invisible:1b,Item:{id:\"highcraft:" + items[i] + "\",count:1}}");
            }
            for (String s : c) cmd(mc, s);
        });
        // Fill drying rack + grow tent (server side)
        step(10, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            if (level.getBlockEntity(new BlockPos(0, Y + 1, 16)) instanceof DryingRackBlockEntity rack) {
                rack.addItem(new ItemStack(ModItems.CANNABIS_BUD.get()));
                rack.addItem(new ItemStack(ModItems.TOBACCO_LEAF.get()));
                rack.addItem(new ItemStack(ModItems.COCA_LEAVES.get()));
                rack.addItem(new ItemStack(ModItems.MAGIC_MUSHROOM.get()));
            }
        }));
        step(5, mc -> {
            // Client side: force the grow tent rendering
            setTent(mc, new BlockPos(10, Y + 1, 16), ModBlocks.CANNABIS_CROP.get(), 0.95f);
            setTent(mc, new BlockPos(10, Y + 1, 19), ModBlocks.POPPY_CROP.get(), 0.7f);
            mc.options.hideGui = true;
        });
        if (onlyEffects) { effects(); return; }
        if ("items".equals(System.getProperty("highcraft.devtest.only"))) { handTest(); return; }
        if ("iris".equals(System.getProperty("highcraft.devtest.only"))) { irisTest(); return; }
        if ("create".equals(System.getProperty("highcraft.devtest.only"))) { createTest(); return; }
        if ("lab".equals(System.getProperty("highcraft.devtest.only"))) { labTest(); return; }
        if ("tent".equals(System.getProperty("highcraft.devtest.only"))) { tentTest(); return; }
        step(140, mc -> tp(mc, 7.5, Y + 6.5, -7.0, 0f, 32f));
        waitUntil(mc -> mc.player.getY() > Y + 2);
        step(60, mc -> {});
        step(20, mc -> shot(mc, "01_overview_crops"));
        step(40, mc -> tp(mc, 3.5, Y + 2.6, -2.8, -10f, 22f));
        step(20, mc -> shot(mc, "02_cannabis_close"));
        step(40, mc -> tp(mc, 12.0, Y + 2.9, -2.3, 20f, 30f));
        step(20, mc -> shot(mc, "03_cannabis_mature"));
        step(40, mc -> tp(mc, 7.5, Y + 2.4, 1.6, 0f, 25f));
        step(20, mc -> shot(mc, "04_tobacco"));
        step(40, mc -> tp(mc, 7.5, Y + 2.4, 4.6, 0f, 28f));
        step(20, mc -> shot(mc, "05_poppy"));
        step(40, mc -> tp(mc, 7.5, Y + 2.6, 7.2, 0f, 30f));
        step(20, mc -> shot(mc, "06_coca_mushroom"));
        step(40, mc -> tp(mc, 7.5, Y + 3.2, 11.8, 0f, 30f));
        step(20, mc -> shot(mc, "07_machines"));
        step(40, mc -> tp(mc, 5.5, Y + 2.2, 13.8, 0f, 22f));
        step(20, mc -> shot(mc, "08_machines_close"));
        step(40, mc -> tp(mc, 11.5, Y + 2.2, 13.8, 0f, 22f));
        step(20, mc -> shot(mc, "09_tent_bong_scale"));
        step(40, mc -> tp(mc, 20.0, Y + 2.0, 13.0, 0f, 10f));
        step(20, mc -> shot(mc, "10_villagers"));
        // --- 3D items ---
        step(40, mc -> tp(mc, 8.0, Y + 2.0, 20.6, 0f, 8f));
        step(30, mc -> shot(mc, "30_items_wall"));
        step(5, mc -> { mc.options.hideGui = false; tp(mc, 7.5, Y + 1.0, 14.0, 0f, 15f);
            cmd(mc, "item replace entity Dev weapon.mainhand with highcraft:joint");
            cmd(mc, "item replace entity Dev weapon.offhand with highcraft:lighter"); });
        step(40, mc -> { HighCraft.LOGGER.info("[DevTest] main={} strength={}", mc.player.getMainHandItem(), mc.player.getAttackStrengthScale(0f)); shot(mc, "31_hand_joint"); });
        step(5, mc -> cmd(mc, "item replace entity Dev weapon.mainhand with highcraft:cannabis_bud"));
        step(40, mc -> shot(mc, "32_hand_bud"));
        step(5, mc -> cmd(mc, "item replace entity Dev weapon.mainhand with highcraft:morphine_syringe"));
        step(40, mc -> shot(mc, "33_hand_syringe"));
        step(5, mc -> { cmd(mc, "item replace entity Dev weapon.mainhand with highcraft:joint");
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT); });
        step(40, mc -> shot(mc, "34_thirdperson_joint"));
        step(5, mc -> { mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            cmd(mc, "item replace entity Dev weapon.mainhand with minecraft:air"); cmd(mc, "item replace entity Dev weapon.offhand with minecraft:air"); });
        // --- GUIs ---
        step(10, mc -> { mc.options.hideGui = false; tp(mc, 6.5, Y + 1.0, 14.5, 0f, 30f); });
        step(30, mc -> openBlock(mc, new BlockPos(6, Y + 1, 16)));
        step(30, mc -> shot(mc, "11_gui_lab"));
        step(5, mc -> mc.player.closeContainer());
        step(10, mc -> openBlock(mc, new BlockPos(10, Y + 1, 16)));
        step(30, mc -> shot(mc, "12_gui_tent"));
        step(5, mc -> mc.player.closeContainer());
        step(10, mc -> {
            CreativeModeInventoryScreen screen = new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true);
            mc.setScreen(screen);
        });
        step(10, mc -> {
            if (mc.screen instanceof CreativeModeInventoryScreen cs) {
                try {
                    var m = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", net.minecraft.world.item.CreativeModeTab.class);
                    m.setAccessible(true);
                    m.invoke(cs, ModCreativeTabs.MAIN.get());
                } catch (Exception e) { HighCraft.LOGGER.error("selectTab", e); }
            }
        });
        step(30, mc -> shot(mc, "13_creative_tab"));
        step(5, mc -> mc.setScreen(null));
        // --- JEI ---
        step(10, mc -> showJei(HighCraftJeiPlugin.GROWING));
        step(30, mc -> shot(mc, "14_jei_growing"));
        step(5, mc -> showJei(HighCraftJeiPlugin.LAB));
        step(30, mc -> shot(mc, "15_jei_lab"));
        step(5, mc -> showJei(HighCraftJeiPlugin.EXTRACTING));
        step(30, mc -> shot(mc, "16_jei_extracting"));
        step(5, mc -> showJei(HighCraftJeiPlugin.DRYING));
        step(30, mc -> shot(mc, "17_jei_drying"));
        step(5, mc -> mc.setScreen(null));
        effects();
    }

    /** With Iris + shader pack: plants, machines and drug shaders after the shader pack. */
    private void irisTest() {
        step(200, mc -> tp(mc, 7.5, Y + 2.6, -2.8, -10f, 22f));
        waitUntil(mc -> mc.player.getY() > Y + 2);
        step(200, mc -> {});
        step(20, mc -> { HighCraft.LOGGER.info("[DevTest] iris shaderpack active={}", de.highcraft.client.IrisCompat.isShaderPackActive()); shot(mc, "50_iris_cannabis"); });
        step(10, mc -> tp(mc, 7.5, Y + 2.6, 7.2, 0f, 30f));
        step(160, mc -> shot(mc, "51_iris_coca_mushroom"));
        step(10, mc -> tp(mc, 5.5, Y + 2.2, 13.8, 0f, 22f));
        step(160, mc -> shot(mc, "52_iris_machines"));
        step(10, mc -> { mc.options.hideGui = false; tp(mc, 7.5, Y + 3.0, -4.0, 0f, 20f); });
        step(120, mc -> {});
        effectShot("trip", de.highcraft.client.DrugVisuals.TRIP, "53_iris_trip");
        effectShot("acid", de.highcraft.client.DrugVisuals.ACID, "54_iris_lsd");
        effectShot("sedated", de.highcraft.client.DrugVisuals.SEDATED, "55_iris_sedated");
        step(20, mc -> mc.stop());
    }

    /** With Create + addon: count recipes and show them in JEI. */
    private void createTest() {
        step(40, mc -> {
            var counts = new java.util.TreeMap<String, Integer>();
            for (var holder : mc.level.getRecipeManager().getRecipes()) {
                if (holder.id().getNamespace().equals("highcraft_create")) {
                    counts.merge(net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType()).toString(), 1, Integer::sum);
                }
            }
            HighCraft.LOGGER.info("[DevTest] create recipes: {}", counts);
            for (String id : new String[]{"incomplete_joint", "incomplete_spliff", "incomplete_cigarette", "incomplete_cigar", "incomplete_blotter_sheet", "incomplete_syringe"}) {
                var rl = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("highcraft_create", id);
                HighCraft.LOGGER.info("[DevTest] item {} registered={}", rl, net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(rl));
            }
        });
        // Creative tab (icons + names in the pack's language setting)
        step(10, mc -> mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true)));
        step(10, mc -> {
            if (mc.screen instanceof CreativeModeInventoryScreen cs) {
                try {
                    var m = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", net.minecraft.world.item.CreativeModeTab.class);
                    m.setAccessible(true);
                    m.invoke(cs, ModCreativeTabs.MAIN.get());
                } catch (Exception e) { HighCraft.LOGGER.error("selectTab", e); }
            }
        });
        step(30, mc -> shot(mc, "59_creative_tab"));
        step(5, mc -> mc.setScreen(null));
        // Functional test: creative motor + millstone grinds cannabis leaves (addon recipe milling/cannabis_leaf)
        final BlockPos mill = new BlockPos(2, Y + 2, -12);
        step(10, mc -> {
            cmd(mc, setb(mill.getX(), mill.getY() - 1, mill.getZ(), "create:creative_motor[facing=up]"));
            cmd(mc, setb(mill.getX(), mill.getY(), mill.getZ(), "create:millstone"));
        });
        step(40, mc -> millstone(mc, mill, true));
        step(400, mc -> {});
        step(5, mc -> millstone(mc, mill, false));
        // Go through all of Create's JEI categories and only show the addon recipes
        final java.util.List<String> shown = new java.util.ArrayList<>();
        step(20, mc -> {
            var rt = HighCraftJeiPlugin.runtime;
            var rm = rt.getRecipeManager();
            rm.createRecipeCategoryLookup().get().forEach(cat -> {
                var uid = cat.getRecipeType().getUid();
                if (!uid.getNamespace().equals("create")) return;
                long ours = rm.createRecipeLookup(cat.getRecipeType()).get()
                        .filter(r -> r instanceof net.minecraft.world.item.crafting.RecipeHolder<?> h && h.id().getNamespace().equals("highcraft_create")).count();
                if (ours > 0) shown.add(uid.toString());
                HighCraft.LOGGER.info("[DevTest] JEI {} -> {} addon recipes", uid, ours);
            });
        });
        for (int i = 0; i < 9; i++) {
            final int k = i;
            step(5, mc -> { if (k < shown.size()) showOurs(shown.get(k)); });
            step(40, mc -> { if (k < shown.size()) shot(mc, (60 + k) + "_jei_" + shown.get(k).replace(':', '_')); });
        }
        step(5, mc -> mc.setScreen(null));
        step(20, mc -> mc.stop());
    }

    private static void millstone(Minecraft mc, BlockPos pos, boolean insert) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerLevel lvl = server.overworld();
            var h = lvl.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
            if (h == null) { HighCraft.LOGGER.warn("[DevTest] millstone: no item handler at {} ({})", pos, lvl.getBlockState(pos)); return; }
            if (insert) {
                ItemStack rest = h.insertItem(0, new ItemStack(ModItems.CANNABIS_LEAF.get(), 3), false);
                HighCraft.LOGGER.info("[DevTest] millstone: inserted 3 cannabis leaves, remainder={}", rest);
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (!s.isEmpty()) sb.append(" [").append(i).append("] ").append(s);
            }
            HighCraft.LOGGER.info("[DevTest] millstone result:{}", sb);
        });
    }


    // ------------------------------------------------------------------ drug lab test
    private static final BlockPos REACTOR_A = new BlockPos(22, Y + 1, 14), REACTOR_B = new BlockPos(22, Y + 1, 20),
            CRYSTALLIZER = new BlockPos(26, Y + 1, 14), PILL_PRESS = new BlockPos(29, Y + 1, 14);

    private static de.highcraft.block.entity.AbstractMachineBlockEntity machineAt(MinecraftServer server, BlockPos pos) {
        return (de.highcraft.block.entity.AbstractMachineBlockEntity) server.overworld().getBlockEntity(pos);
    }

    private static void fill(MinecraftServer server, BlockPos pos, ItemStack... stacks) {
        var be = machineAt(server, pos);
        for (int i = 0; i < stacks.length; i++) if (!stacks[i].isEmpty()) be.inventory().setStackInSlot(i, stacks[i]);
    }

    private static ItemStack st(net.minecraft.world.level.ItemLike item, int n) { return new ItemStack(item, n); }

    private static String outputs(MinecraftServer server, BlockPos pos) {
        var be = machineAt(server, pos);
        StringBuilder sb = new StringBuilder();
        for (int i = be.machine().firstOutputSlot(); i < be.machine().slotCount(); i++) {
            ItemStack s = be.inventory().getStackInSlot(i);
            if (!s.isEmpty()) sb.append(s).append(" q=").append(de.highcraft.drug.Quality.get(s)).append(' ');
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    private void labTest() {
        step(10, mc -> {
            List<String> c = new ArrayList<>();
            c.add("effect clear Dev");
            c.add("fill 16 " + Y + " 10 36 " + Y + " 30 highcraft:lab_tiles");
            c.add(setb(22, Y + 1, 14, "highcraft:chemical_reactor[facing=south]"));
            c.add(setb(22, Y + 1, 20, "highcraft:chemical_reactor[facing=south]"));
            c.add(setb(23, Y + 1, 20, "highcraft:fume_hood[facing=south]"));
            c.add(setb(26, Y, 14, "minecraft:packed_ice"));
            c.add(setb(26, Y + 1, 14, "highcraft:crystallizer[facing=south]"));
            c.add(setb(29, Y + 1, 14, "highcraft:pill_press[facing=south]"));
            // showroom row of all lab blocks
            String[] row = {"highcraft:lab_counter[facing=south]", "highcraft:chemical_barrel", "highcraft:hazard_barrel", "highcraft:gas_cylinder",
                    "highcraft:fume_hood[facing=south]", "highcraft:hazard_stripes", "highcraft:lab_tiles", "highcraft:lab_tile_stairs[facing=west]",
                    "highcraft:lab_tile_slab"};
            for (int i = 0; i < row.length; i++) c.add(setb(18 + i * 2, Y + 1, 26, row[i]));
            c.add("fill 18 " + (Y + 1) + " 27 35 " + (Y + 3) + " 27 minecraft:smooth_quartz");
            c.add(setb(19, Y + 2, 26, "highcraft:glassware_shelf[facing=north]"));
            c.add(setb(25, Y + 2, 26, "highcraft:exhaust_fan[facing=north]"));
            c.add(setb(29, Y + 2, 26, "highcraft:hazard_sign[facing=north]"));
            c.add("fill 33 " + (Y + 1) + " 26 35 " + (Y + 1) + " 26 highcraft:plastic_sheeting");
            c.add(setb(23, Y + 4, 25, "minecraft:smooth_quartz")); c.add(setb(23, Y + 3, 25, "highcraft:lab_light"));
            c.add(setb(31, Y + 1, 24, "highcraft:chemical_barrel"));
            for (String x : c) cmd(mc, x);
        });
        step(10, mc -> {
            MinecraftServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                var r = de.highcraft.registry.ModItems.class;
                fill(server, REACTOR_A, st(ModItems.PRECURSOR.get(), 2), st(ModItems.CATALYST.get(), 1), st(ModItems.SOLVENT.get(), 1), st(net.minecraft.world.item.Items.COAL, 4));
                fill(server, REACTOR_B, st(ModItems.PRECURSOR.get(), 2), st(ModItems.CATALYST.get(), 1), st(ModItems.SOLVENT.get(), 1), st(net.minecraft.world.item.Items.COAL, 4));
                fill(server, CRYSTALLIZER, de.highcraft.drug.Quality.set(st(ModItems.METH_BATCH.get(), 1), 3));
                fill(server, PILL_PRESS, st(ModItems.MDMA_POWDER.get(), 1), st(net.minecraft.world.item.Items.SUGAR, 1),
                        st(net.minecraft.world.item.Items.PINK_DYE, 1), st(net.minecraft.world.item.Items.COAL, 2));
                HighCraft.LOGGER.info("[DevTest] lab machines filled");
            });
            cmd(mc, "gamemode survival Dev");
            cmd(mc, "effect give Dev minecraft:resistance 600 4 true");
            tp(mc, 22.5, Y + 1.0, 16.5, 180f, 20f);
        });
        step(160, mc -> {});
        step(5, mc -> HighCraft.LOGGER.info("[DevTest] fumes on player (no mask) = {}", mc.player.hasEffect(de.highcraft.registry.ModEffects.TOXIC_FUMES.getDelegate())));
        step(5, mc -> { cmd(mc, "effect clear Dev highcraft:toxic_fumes"); cmd(mc, "effect clear Dev minecraft:nausea");
            cmd(mc, "item replace entity Dev armor.head with highcraft:gas_mask"); });
        step(120, mc -> {});
        step(5, mc -> HighCraft.LOGGER.info("[DevTest] fumes on player (gas mask) = {}", mc.player.hasEffect(de.highcraft.registry.ModEffects.TOXIC_FUMES.getDelegate())));
        // GUIs (status labels)
        step(10, mc -> { cmd(mc, "gamemode creative Dev"); openBlock(mc, REACTOR_A); });
        step(30, mc -> shot(mc, "80_gui_reactor_unventilated"));
        step(5, mc -> mc.player.closeContainer());
        step(10, mc -> openBlock(mc, REACTOR_B));
        step(30, mc -> shot(mc, "81_gui_reactor_ventilated"));
        step(5, mc -> mc.player.closeContainer());
        step(10, mc -> openBlock(mc, CRYSTALLIZER));
        step(30, mc -> shot(mc, "82_gui_crystallizer"));
        step(5, mc -> mc.player.closeContainer());
        step(10, mc -> tp(mc, 29.5, Y + 1.0, 16.5, 180f, 20f));
        step(10, mc -> openBlock(mc, PILL_PRESS));
        step(30, mc -> shot(mc, "83_gui_pill_press"));
        step(5, mc -> mc.player.closeContainer());
        step(5, mc -> { mc.options.hideGui = true; tp(mc, 25.5, Y + 3.0, 19.5, 160f, 28f); });
        step(40, mc -> shot(mc, "84_lab_machines_running"));
        step(5, mc -> tp(mc, 26.5, Y + 2.4, 21.0, 0f, 15f));
        step(40, mc -> shot(mc, "85_lab_blocks"));
        // results after the batches are done
        step(400, mc -> {});
        step(5, mc -> {
            MinecraftServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                HighCraft.LOGGER.info("[DevTest] reactor A (no ventilation): {}", outputs(server, REACTOR_A));
                HighCraft.LOGGER.info("[DevTest] reactor B (fume hood): {}", outputs(server, REACTOR_B));
                HighCraft.LOGGER.info("[DevTest] crystallizer (packed ice): {}", outputs(server, CRYSTALLIZER));
                HighCraft.LOGGER.info("[DevTest] pill press: {}", outputs(server, PILL_PRESS));
            });
        });
        // protective gear (third person)
        step(5, mc -> {
            for (String[] a : new String[][]{{"armor.head", "gas_mask"}, {"armor.chest", "hazmat_suit"}, {"armor.legs", "hazmat_pants"}, {"armor.feet", "hazmat_boots"}})
                cmd(mc, "item replace entity Dev " + a[0] + " with highcraft:" + a[1]);
            tp(mc, 19.5, Y + 1.0, 17.5, 0f, 0f);
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
        });
        step(40, mc -> shot(mc, "86_hazmat_suit"));
        step(5, mc -> { mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            for (String a : new String[]{"armor.head", "armor.chest", "armor.legs", "armor.feet"}) cmd(mc, "item replace entity Dev " + a + " with minecraft:air"); });
        // effects
        step(10, mc -> { mc.options.hideGui = false; tp(mc, 25.5, Y + 1.0, 20.5, 180f, 12f); cmd(mc, "summon minecraft:villager 25 " + (Y + 1) + " 17 {NoAI:1b}"); });
        effectShot("meth_high", de.highcraft.client.DrugVisuals.METH, "87_effect_meth");
        effectShot("euphoria", de.highcraft.client.DrugVisuals.ECSTASY, "88_effect_ecstasy");
        step(5, mc -> cmd(mc, "effect clear Dev"));
        // village lab template
        step(10, mc -> { mc.options.hideGui = true; cmd(mc, "place template highcraft:village/plains/drug_lab 2 " + Y + " 28"); });
        step(20, mc -> tp(mc, -3.0, Y + 1.0, 32.5, -90f, 5f));
        waitUntil(mc -> mc.level.getBlockState(new BlockPos(10, Y + 1, 30)).is(ModBlocks.CHEMICAL_REACTOR.get()));
        step(60, mc -> shot(mc, "89_village_lab_outside"));
        step(5, mc -> tp(mc, 3.6, Y + 1.0, 32.5, -90f, 12f));
        step(60, mc -> shot(mc, "90_village_lab_inside"));
        // real villages: how often does a lab spawn?
        final int[] found = new int[2];
        final String[] types = {"plains", "desert", "savanna", "snowy", "taiga"};
        final List<BlockPos> labs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int k = i;
            final int vx = 3000 + (k % 5) * 400, vz = 3000 + (k / 5) * 400;
            step(220, mc -> cmd(mc, "forceload add " + (vx - 96) + " " + (vz - 96) + " " + (vx + 96) + " " + (vz + 96)));
            step(60, mc -> cmd(mc, "place structure minecraft:village_" + types[k % 5] + " " + vx + " 64 " + vz));
            step(5, mc -> {
                MinecraftServer server = mc.getSingleplayerServer();
                server.execute(() -> {
                    ServerLevel lvl = server.overworld();
                    int count = 0, bells = 0;
                    BlockPos first = null;
                    for (int cx = (vx - 112) >> 4; cx <= (vx + 112) >> 4; cx++)
                        for (int cz = (vz - 112) >> 4; cz <= (vz + 112) >> 4; cz++) {
                            var chunk = lvl.getChunk(cx, cz);
                            for (int x = 0; x < 16; x++)
                                for (int z = 0; z < 16; z++) {
                                    int top = chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
                                    for (int y = top - 9; y <= top + 1; y++) {
                                        BlockPos bp = new BlockPos(cx * 16 + x, y, cz * 16 + z);
                                        var bs = chunk.getBlockState(bp);
                                        if (bs.is(ModBlocks.CHEMICAL_REACTOR.get())) { count++; if (first == null) first = bp; }
                                        else if (bs.is(net.minecraft.world.level.block.Blocks.BELL)) bells++;
                                    }
                                }
                        }
                    found[0]++;
                    if (count > 0) { found[1]++; labs.add(first); }
                    HighCraft.LOGGER.info("[DevTest] village {} ({}) at {} {}: bells={} labs={}{}", k, types[k % 5], vx, vz, bells, count,
                            first != null ? " first at " + first.toShortString() : "");
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                            "forceload remove " + (vx - 96) + " " + (vz - 96) + " " + (vx + 96) + " " + (vz + 96));
                });
            });
        }
        step(10, mc -> HighCraft.LOGGER.info("[DevTest] villages with a drug lab: {} of {}", found[1], found[0]));
        for (int i = 0; i < 3; i++) {
            final int k = i;
            step(10, mc -> {
                if (labs.size() <= k) return;
                BlockPos p = labs.get(k);
                cmd(mc, "gamemode spectator Dev");
                cmd(mc, "forceload add " + (p.getX() - 32) + " " + (p.getZ() - 32) + " " + (p.getX() + 32) + " " + (p.getZ() + 32));
                cmd(mc, "tp Dev " + (p.getX() - 11) + " " + (p.getY() + 11) + " " + (p.getZ() - 11) + " -45 38");
            });
            step(300, mc -> {});
            step(5, mc -> { if (labs.size() > k) shot(mc, (91 + k) + "_village_with_lab"); });
        }
        step(20, mc -> mc.stop());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void showOurs(String uid) {
        var rm = HighCraftJeiPlugin.runtime.getRecipeManager();
        var type = rm.getRecipeType(net.minecraft.resources.ResourceLocation.parse(uid)).orElse(null);
        if (type == null) return;
        mezz.jei.api.recipe.category.IRecipeCategory cat = rm.getRecipeCategory((mezz.jei.api.recipe.RecipeType) type);
        java.util.List list = rm.createRecipeLookup((mezz.jei.api.recipe.RecipeType) type).get()
                .filter(r -> r instanceof net.minecraft.world.item.crafting.RecipeHolder<?> h && h.id().getNamespace().equals("highcraft_create"))
                .toList();
        HighCraftJeiPlugin.runtime.getRecipesGui().showRecipes(cat, list, java.util.List.of());
    }

    private static void showFocus(String role, String id) {
        var rt = HighCraftJeiPlugin.runtime;
        if (rt == null) return;
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(id));
        var focus = rt.getJeiHelpers().getFocusFactory().createFocus(
                "INPUT".equals(role) ? mezz.jei.api.recipe.RecipeIngredientRole.INPUT : mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT,
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(item));
        rt.getRecipesGui().show(focus);
    }

    private static void check(String what, boolean ok, Object detail) {
        HighCraft.LOGGER.info("[DevTest] {} {} ({})", ok ? "PASS" : "FAIL", what, detail);
    }

    private static int progressOf(Object be) {
        try {
            Field f = de.highcraft.block.entity.AbstractMachineBlockEntity.class.getDeclaredField("progress");
            f.setAccessible(true);
            return f.getInt(be);
        } catch (Exception e) { return -1; }
    }

    private static int countItems(ServerLevel level, BlockPos around, net.minecraft.world.item.Item item) {
        int n = 0;
        for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(around).inflate(3))) {
            if (e.getItem().is(item)) n += e.getItem().getCount();
        }
        return n;
    }

    /** Two-block grow tent + machine notification fixes (mode "tent"). */
    private void tentTest() {
        final int z = 32;
        final BlockPos lit = new BlockPos(30, Y + 1, z), dark = new BlockPos(32, Y + 1, z), legacy = new BlockPos(34, Y + 1, z),
                blocked = new BlockPos(36, Y + 1, z), work = new BlockPos(38, Y + 1, z), press = new BlockPos(30, Y + 1, z + 5),
                control = new BlockPos(32, Y + 1, z + 5);
        step(10, mc -> {
            for (String c : new String[]{
                    setb(30, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=true]"),
                    setb(30, Y + 2, z, "highcraft:grow_tent[facing=north,half=upper,lit=true]"),
                    setb(32, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=false]"),
                    setb(32, Y + 2, z, "highcraft:grow_tent[facing=north,half=upper,lit=false]"),
                    setb(34, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=false]"),   // pre-1.2.1 tent: gets its roof
                    setb(36, Y + 2, z, "minecraft:stone"),
                    setb(36, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=false]"),   // pre-1.2.1 tent without room
                    setb(38, Y + 1, z, "highcraft:grow_tent[facing=north,half=lower,lit=false]"),
                    setb(38, Y + 2, z, "highcraft:grow_tent[facing=north,half=upper,lit=false]"),
                    setb(30, Y + 1, z + 5, "highcraft:press[facing=north,lit=false]"),
                    setb(32, Y + 1, z + 5, "highcraft:press[facing=north,lit=false]")}) cmd(mc, c);
            tp(mc, 31.5, Y + 1, z + 3.0, 0f, 20f); // within reach of the presses, else stillValid closes the menu
        });
        step(40, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            check("legacy tent got its roof", de.highcraft.block.GrowTentBlock.hasUpperHalf(level, legacy), level.getBlockState(legacy.above()));
            check("legacy tent without room survives", level.getBlockState(blocked).is(ModBlocks.GROW_TENT.get())
                    && level.getBlockState(blocked.above()).is(net.minecraft.world.level.block.Blocks.STONE), level.getBlockState(blocked));
            check("upper half has no block entity", level.getBlockEntity(work.above()) == null, level.getBlockEntity(work.above()));
            var items = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, work.above(), Direction.UP);
            var energy = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, work.above(), Direction.UP);
            check("item capability on the roof", items != null, items);
            check("energy capability on the roof", energy != null, energy);
            if (items != null && energy != null) {
                ItemStack rest = items.insertItem(GrowTentBlockEntity.SLOT_SEED, new ItemStack(ModItems.CANNABIS_SEEDS.get(), 4), false);
                items.insertItem(GrowTentBlockEntity.SLOT_WATER, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET), false);
                for (int i = 0; i < 50; i++) energy.receiveEnergy(400, false);
                check("seeds inserted through the roof", rest.isEmpty(), rest);
            }
            // Press: 1 resin (recipe needs 2), then shift-click more in -> must start (in-place merge notification)
            if (level.getBlockEntity(control) instanceof de.highcraft.block.entity.AbstractMachineBlockEntity c) {
                c.inventory().setStackInSlot(0, new ItemStack(ModItems.CANNABIS_RESIN.get(), 1));
                for (int i = 0; i < 10; i++) c.energyStorage().receiveEnergy(400, false);
            }
            if (level.getBlockEntity(press) instanceof de.highcraft.block.entity.AbstractMachineBlockEntity be) {
                be.inventory().setStackInSlot(0, new ItemStack(ModItems.CANNABIS_RESIN.get(), 1));
                for (int i = 0; i < 10; i++) be.energyStorage().receiveEnergy(400, false);
                var sp = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                sp.getInventory().setItem(0, new ItemStack(ModItems.CANNABIS_RESIN.get(), 5));
                sp.openMenu(be, buf -> buf.writeBlockPos(press));
            }
        }));
        step(40, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            var tent = level.getBlockEntity(work);
            check("tent grows (powered)", progressOf(tent) > 0, progressOf(tent));
            check("lamp on in the lower half", level.getBlockState(work).getValue(de.highcraft.block.MachineBlock.LIT), level.getBlockState(work));
            check("roof mirrors the lamp", level.getBlockState(work.above()).getValue(de.highcraft.block.MachineBlock.LIT), level.getBlockState(work.above()));
            var pressBe = level.getBlockEntity(press);
            check("press idle with 1 resin", progressOf(pressBe) == 0, progressOf(pressBe));
            var sp = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
            check("press menu open", sp.containerMenu instanceof de.highcraft.menu.MachineMenu, sp.containerMenu);
            if (sp.containerMenu instanceof de.highcraft.menu.MachineMenu menu) {
                menu.quickMoveStack(sp, menu.machineSlotCount() + 27); // hotbar slot 0 -> merges into the input stack
            }
            // Control: the same in-place merge through a plain NeoForge SlotItemHandler (= behaviour before the fix)
            var c = (de.highcraft.block.entity.AbstractMachineBlockEntity) level.getBlockEntity(control);
            var plain = new net.neoforged.neoforge.items.SlotItemHandler(c.inventory(), 0, 0, 0);
            plain.getItem().grow(4);
            plain.setChanged();
        }));
        step(40, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            var pressBe = (de.highcraft.block.entity.AbstractMachineBlockEntity) level.getBlockEntity(press);
            check("press runs after shift-click merge", progressOf(pressBe) > 0,
                    "progress=" + progressOf(pressBe) + " input=" + pressBe.inventory().getStackInSlot(0));
            var c = (de.highcraft.block.entity.AbstractMachineBlockEntity) level.getBlockEntity(control);
            check("control: plain SlotItemHandler merge is not seen (the old bug)", progressOf(c) == 0,
                    "progress=" + progressOf(c) + " input=" + c.inventory().getStackInSlot(0));
            mc.getSingleplayerServer().getPlayerList().getPlayers().get(0).closeContainer();
        }));
        // Screenshots: client side plant display, then front view and close-up
        step(10, mc -> {
            setTent(mc, lit, ModBlocks.CANNABIS_CROP.get(), 0.95f);
            setTent(mc, dark, ModBlocks.TOBACCO_CROP.get(), 0.9f);
            setTent(mc, legacy, ModBlocks.POPPY_CROP.get(), 0.6f);
            mc.options.hideGui = true;
            tp(mc, 34.5, Y + 2.2, z - 6.5, 0f, 6f);
        });
        waitUntil(mc -> mc.player.getZ() > z - 8 && mc.player.getZ() < z - 5);
        step(60, mc -> {});
        step(20, mc -> shot(mc, "70_tents_front"));
        step(30, mc -> tp(mc, 30.5, Y + 1.5, z - 2.4, 0f, 12f));
        step(30, mc -> shot(mc, "71_tent_cannabis_close"));
        step(30, mc -> tp(mc, 27.5, Y + 3.6, z - 3.5, -35f, 30f));
        step(30, mc -> shot(mc, "72_tents_above"));
        // Item: hotbar icon + first person, third person
        step(10, mc -> { mc.options.hideGui = false; tp(mc, 33.5, Y + 1.0, z - 9.0, 0f, 10f);
            cmd(mc, "item replace entity Dev weapon.mainhand with highcraft:grow_tent"); });
        step(40, mc -> shot(mc, "73_tent_item_hand"));
        step(5, mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
        step(40, mc -> shot(mc, "74_tent_item_thirdperson"));
        step(5, mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
        // Breaking: survival on the roof drops exactly one tent + the contents; creative drops nothing
        step(10, mc -> { cmd(mc, "gamemode survival Dev"); cmd(mc, "kill @e[type=item]"); });
        step(10, mc -> mc.getSingleplayerServer().execute(() -> {
            var sp = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
            sp.gameMode.destroyBlock(work.above());
        }));
        step(10, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            check("survival: both halves gone", level.getBlockState(work).isAir() && level.getBlockState(work.above()).isAir(),
                    level.getBlockState(work) + " / " + level.getBlockState(work.above()));
            int tents = countItems(level, work, ModItems.GROW_TENT.get());
            check("survival: exactly one tent dropped", tents == 1, tents);
            check("survival: contents dropped", countItems(level, work, ModItems.CANNABIS_SEEDS.get()) >= 3,
                    countItems(level, work, ModItems.CANNABIS_SEEDS.get()));
        }));
        step(10, mc -> { cmd(mc, "gamemode creative Dev"); cmd(mc, "kill @e[type=item]"); });
        step(10, mc -> mc.getSingleplayerServer().execute(() -> {
            var sp = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
            sp.gameMode.destroyBlock(dark.above());
        }));
        step(10, mc -> mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            check("creative: both halves gone", level.getBlockState(dark).isAir() && level.getBlockState(dark.above()).isAir(),
                    level.getBlockState(dark) + " / " + level.getBlockState(dark.above()));
            int tents = countItems(level, dark, ModItems.GROW_TENT.get());
            check("creative: no tent dropped", tents == 0, tents);
        }));
        step(20, mc -> { HighCraft.LOGGER.info("[DevTest] tent test done - quitting"); mc.stop(); });
    }

    private void handTest() {
        String[] items = {"minecraft:diamond_sword", "highcraft:cannabis_leaf", "highcraft:joint", "highcraft:cannabis_bud", "highcraft:lighter"};
        step(60, mc -> { mc.options.hideGui = false; tp(mc, 7.5, Y + 1.0, 14.0, 0f, 15f); });
        for (String id : items) {
            step(5, mc -> cmd(mc, "item replace entity Dev weapon.mainhand with " + id));
            step(40, mc -> {
                var r = mc.getEntityRenderDispatcher().getItemInHandRenderer();
                HighCraft.LOGGER.info("[DevTest] hand {} main={} strength={} using={}", id, mc.player.getMainHandItem(), mc.player.getAttackStrengthScale(0f), mc.player.isUsingItem());
                shot(mc, "40_hand_" + id.replace(':', '_'));
            });
        }
        step(20, mc -> mc.stop());
    }

    private void effects() {
        // --- Effects ---
        step(10, mc -> { mc.options.hideGui = false; tp(mc, 7.5, Y + 3.0, -4.0, 0f, 20f); });
        effectShot("trip", de.highcraft.client.DrugVisuals.TRIP, "20_effect_trip");
        effectShot("stoned", de.highcraft.client.DrugVisuals.STONED, "21_effect_stoned");
        effectShot("sedated", de.highcraft.client.DrugVisuals.SEDATED, "22_effect_sedated");
        effectShot("overdose", de.highcraft.client.DrugVisuals.OVERDOSE, "23_effect_overdose");
        effectShot("rush", de.highcraft.client.DrugVisuals.RUSH, "24_effect_rush");
        effectShot("withdrawal", de.highcraft.client.DrugVisuals.WITHDRAWAL, "25_effect_withdrawal");
        effectShot("acid", de.highcraft.client.DrugVisuals.ACID, "26_effect_lsd");
        step(40, mc -> {
            HighCraft.LOGGER.info("[DevTest] done - quitting the game");
            mc.stop();
        });
    }

    private static void openBlock(Minecraft mc, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }

    private static void showJei(mezz.jei.api.recipe.RecipeType<?> type) {
        if (HighCraftJeiPlugin.runtime != null) {
            HighCraftJeiPlugin.runtime.getRecipesGui().showTypes(List.of(type));
        } else {
            HighCraft.LOGGER.warn("[DevTest] JEI runtime not available");
        }
    }

    private static void setTent(Minecraft mc, BlockPos pos, Block block, float progress) {
        if (mc.level.getBlockEntity(pos) instanceof GrowTentBlockEntity be) {
            try {
                Field f1 = GrowTentBlockEntity.class.getDeclaredField("displayBlock");
                Field f2 = GrowTentBlockEntity.class.getDeclaredField("displayProgress");
                f1.setAccessible(true); f2.setAccessible(true);
                f1.set(be, block); f2.setFloat(be, progress);
            } catch (Exception e) { HighCraft.LOGGER.error("setTent", e); }
        }
    }
}
