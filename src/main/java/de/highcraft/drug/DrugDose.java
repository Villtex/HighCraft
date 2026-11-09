package de.highcraft.drug;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

/**
 * Describes what a single dose does.
 *
 * @param type         substance class (for addiction/tolerance)
 * @param intoxication increase of the intoxication level (overdose from 100)
 * @param addiction    base addiction gain (multiplied by {@link DrugType#addictiveness()})
 * @param effects      effects incl. base duration
 * @param instantHeal  instant healing in half hearts (e.g. morphine)
 */
public record DrugDose(DrugType type, float intoxication, float addiction, List<Entry> effects, float instantHeal) {

    public record Entry(Holder<MobEffect> effect, int duration, int amplifier) {}

    public static Builder builder(DrugType type) { return new Builder(type); }

    public static final class Builder {
        private final DrugType type;
        private float intoxication;
        private float addiction;
        private float heal;
        private final java.util.ArrayList<Entry> effects = new java.util.ArrayList<>();

        private Builder(DrugType type) { this.type = type; }

        public Builder intoxication(float v) { this.intoxication = v; return this; }
        public Builder addiction(float v) { this.addiction = v; return this; }
        public Builder heal(float v) { this.heal = v; return this; }
        public Builder effect(Holder<MobEffect> effect, int seconds, int amplifier) {
            effects.add(new Entry(effect, seconds * 20, amplifier));
            return this;
        }
        public DrugDose build() { return new DrugDose(type, intoxication, addiction, List.copyOf(effects), heal); }
    }
}
