package de.highcraft.registry;

import de.highcraft.HighCraft;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/** Lab protective gear: gas mask and hazmat suit. */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, HighCraft.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK = MATERIALS.register("gas_mask", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.HELMET, 1); m.put(ArmorItem.Type.CHESTPLATE, 1); m.put(ArmorItem.Type.LEGGINGS, 1);
                m.put(ArmorItem.Type.BOOTS, 1); m.put(ArmorItem.Type.BODY, 1);
            }), 8, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER),
            List.of(new ArmorMaterial.Layer(HighCraft.id("gas_mask"))), 0.0f, 0.0f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = MATERIALS.register("hazmat", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.HELMET, 1); m.put(ArmorItem.Type.CHESTPLATE, 4); m.put(ArmorItem.Type.LEGGINGS, 3);
                m.put(ArmorItem.Type.BOOTS, 1); m.put(ArmorItem.Type.BODY, 4);
            }), 10, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER),
            List.of(new ArmorMaterial.Layer(HighCraft.id("hazmat"))), 0.0f, 0.0f));

    private ModArmorMaterials() {}
}
