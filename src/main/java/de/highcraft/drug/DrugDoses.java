package de.highcraft.drug;

import de.highcraft.registry.ModEffects;
import net.minecraft.world.effect.MobEffects;

import java.util.function.Supplier;

/**
 * All dose definitions. Lazy (supplier) because the effect holders are only bound after registration.
 */
public final class DrugDoses {
    private DrugDoses() {}

    // ---------- Cannabis ----------
    public static final Supplier<DrugDose> JOINT = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(18).addiction(4)
            .effect(ModEffects.STONED.getDelegate(), 150, 0)
            .effect(ModEffects.MUNCHIES.getDelegate(), 120, 0)
            .build());
    public static final Supplier<DrugDose> SPLIFF = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(22).addiction(6)
            .effect(ModEffects.STONED.getDelegate(), 160, 0)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 60, 0)
            .effect(ModEffects.MUNCHIES.getDelegate(), 120, 0)
            .build());
    public static final Supplier<DrugDose> HASHISH = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(28).addiction(6)
            .effect(ModEffects.STONED.getDelegate(), 240, 1)
            .effect(ModEffects.MUNCHIES.getDelegate(), 160, 1)
            .build());
    public static final Supplier<DrugDose> SPACE_COOKIE = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(30).addiction(3)
            .effect(ModEffects.STONED.getDelegate(), 420, 1)
            .effect(ModEffects.MUNCHIES.getDelegate(), 300, 1)
            .build());
    public static final Supplier<DrugDose> PIPE_CANNABIS = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(16).addiction(4)
            .effect(ModEffects.STONED.getDelegate(), 130, 0)
            .effect(ModEffects.MUNCHIES.getDelegate(), 100, 0)
            .build());
    public static final Supplier<DrugDose> BONG_CANNABIS = memo(() -> DrugDose.builder(DrugType.CANNABIS)
            .intoxication(32).addiction(7)
            .effect(ModEffects.STONED.getDelegate(), 260, 1)
            .effect(ModEffects.MUNCHIES.getDelegate(), 200, 1)
            .build());

    // ---------- Tobacco ----------
    public static final Supplier<DrugDose> CIGARETTE = memo(() -> DrugDose.builder(DrugType.NICOTINE)
            .intoxication(4).addiction(5)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 90, 0)
            .build());
    public static final Supplier<DrugDose> CIGAR = memo(() -> DrugDose.builder(DrugType.NICOTINE)
            .intoxication(8).addiction(7)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 240, 1)
            .effect(MobEffects.LUCK, 120, 0)
            .build());
    public static final Supplier<DrugDose> PIPE_TOBACCO = memo(() -> DrugDose.builder(DrugType.NICOTINE)
            .intoxication(5).addiction(5)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 150, 0)
            .build());
    public static final Supplier<DrugDose> BONG_TOBACCO = memo(() -> DrugDose.builder(DrugType.NICOTINE)
            .intoxication(8).addiction(8)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 120, 1)
            .build());

    // ---------- Coca ----------
    public static final Supplier<DrugDose> COCA_TEA = memo(() -> DrugDose.builder(DrugType.COCAINE)
            .intoxication(4).addiction(1)
            .effect(ModEffects.NICOTINE_BUZZ.getDelegate(), 120, 0)
            .effect(MobEffects.MOVEMENT_SPEED, 120, 0)
            .build());
    public static final Supplier<DrugDose> COCAINE = memo(() -> DrugDose.builder(DrugType.COCAINE)
            .intoxication(35).addiction(9)
            .effect(ModEffects.RUSH.getDelegate(), 90, 1)
            .build());

    // ---------- Poppy ----------
    public static final Supplier<DrugDose> PIPE_OPIUM = memo(() -> DrugDose.builder(DrugType.OPIATES)
            .intoxication(30).addiction(8)
            .effect(ModEffects.SEDATED.getDelegate(), 200, 0)
            .effect(MobEffects.REGENERATION, 20, 0)
            .build());
    public static final Supplier<DrugDose> BONG_OPIUM = memo(() -> DrugDose.builder(DrugType.OPIATES)
            .intoxication(40).addiction(10)
            .effect(ModEffects.SEDATED.getDelegate(), 260, 1)
            .effect(MobEffects.REGENERATION, 30, 0)
            .build());
    public static final Supplier<DrugDose> MORPHINE = memo(() -> DrugDose.builder(DrugType.OPIATES)
            .intoxication(38).addiction(12).heal(8)
            .effect(ModEffects.SEDATED.getDelegate(), 150, 1)
            .effect(MobEffects.REGENERATION, 12, 1)
            .effect(MobEffects.DAMAGE_RESISTANCE, 60, 0)
            .build());

    // ---------- Magic mushrooms ----------
    public static final Supplier<DrugDose> MAGIC_MUSHROOM = memo(() -> DrugDose.builder(DrugType.PSILOCYBIN)
            .intoxication(25).addiction(0.5f)
            .effect(ModEffects.TRIP.getDelegate(), 240, 0)
            .build());
    public static final Supplier<DrugDose> DRIED_MAGIC_MUSHROOM = memo(() -> DrugDose.builder(DrugType.PSILOCYBIN)
            .intoxication(35).addiction(0.5f)
            .effect(ModEffects.TRIP.getDelegate(), 360, 1)
            .effect(MobEffects.NIGHT_VISION, 360, 0)
            .build());
    public static final Supplier<DrugDose> MUSHROOM_TEA = memo(() -> DrugDose.builder(DrugType.PSILOCYBIN)
            .intoxication(40).addiction(0.5f)
            .effect(ModEffects.TRIP.getDelegate(), 480, 1)
            .effect(MobEffects.NIGHT_VISION, 480, 0)
            .build());

    // ---------- LSD ----------
    public static final Supplier<DrugDose> LSD_BLOTTER = memo(() -> DrugDose.builder(DrugType.LSD)
            .intoxication(30).addiction(0.3f)
            .effect(ModEffects.ACID.getDelegate(), 600, 1)
            .effect(ModEffects.TRIP.getDelegate(), 240, 0)
            .effect(MobEffects.NIGHT_VISION, 600, 0)
            .build());

    // ---------- Meth ----------
    public static final Supplier<DrugDose> CRYSTAL_METH = memo(() -> DrugDose.builder(DrugType.METH)
            .intoxication(36).addiction(14)
            .effect(ModEffects.METH_HIGH.getDelegate(), 240, 0)
            .effect(MobEffects.NIGHT_VISION, 240, 0)
            .build());
    public static final Supplier<DrugDose> GLASS_PIPE_METH = memo(() -> DrugDose.builder(DrugType.METH)
            .intoxication(44).addiction(18)
            .effect(ModEffects.METH_HIGH.getDelegate(), 300, 1)
            .effect(MobEffects.NIGHT_VISION, 300, 0)
            .build());

    // ---------- Ecstasy ----------
    public static final Supplier<DrugDose> ECSTASY = memo(() -> DrugDose.builder(DrugType.MDMA)
            .intoxication(28).addiction(3)
            .effect(ModEffects.EUPHORIA.getDelegate(), 360, 0)
            .build());

    private static <T> Supplier<T> memo(Supplier<T> s) {
        return new Supplier<>() {
            private T value;
            @Override public T get() {
                if (value == null) value = s.get();
                return value;
            }
        };
    }
}
