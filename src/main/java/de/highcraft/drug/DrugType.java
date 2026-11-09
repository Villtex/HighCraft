package de.highcraft.drug;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * The mod's substance classes. Each class has its own addiction, tolerance and withdrawal values.
 */
public enum DrugType {
    //          id            colour                    addiction     tolerance/dose  craving after (ticks)
    CANNABIS("cannabis", ChatFormatting.GREEN, 0.45f, 0.06f, 20 * 60 * 14),
    NICOTINE("nicotine", ChatFormatting.GOLD, 1.20f, 0.03f, 20 * 60 * 6),
    COCAINE("cocaine", ChatFormatting.WHITE, 1.60f, 0.10f, 20 * 60 * 8),
    OPIATES("opiates", ChatFormatting.DARK_RED, 2.00f, 0.12f, 20 * 60 * 7),
    PSILOCYBIN("psilocybin", ChatFormatting.LIGHT_PURPLE, 0.05f, 0.25f, Integer.MAX_VALUE),
    LSD("lsd", ChatFormatting.AQUA, 0.05f, 0.35f, Integer.MAX_VALUE),
    METH("meth", ChatFormatting.DARK_AQUA, 2.40f, 0.14f, 20 * 60 * 5),
    MDMA("mdma", ChatFormatting.YELLOW, 0.60f, 0.30f, 20 * 60 * 20);

    private final String id;
    private final ChatFormatting color;
    private final float addictiveness;
    private final float toleranceGain;
    private final int cravingDelay;

    DrugType(String id, ChatFormatting color, float addictiveness, float toleranceGain, int cravingDelay) {
        this.id = id;
        this.color = color;
        this.addictiveness = addictiveness;
        this.toleranceGain = toleranceGain;
        this.cravingDelay = cravingDelay;
    }

    public String id() { return id; }
    public ChatFormatting color() { return color; }
    /** How strongly a dose raises addiction (the base value is multiplied by this factor). */
    public float addictiveness() { return addictiveness; }
    /** Tolerance gain per use (0..1). */
    public float toleranceGain() { return toleranceGain; }
    /** After how many ticks without use an addicted player goes into withdrawal. */
    public int cravingDelay() { return cravingDelay; }

    public Component displayName() {
        return Component.translatable("drug.highcraft." + id).withStyle(color);
    }

    public static DrugType byId(String id) {
        for (DrugType t : values()) if (t.id.equals(id)) return t;
        return null;
    }
}
