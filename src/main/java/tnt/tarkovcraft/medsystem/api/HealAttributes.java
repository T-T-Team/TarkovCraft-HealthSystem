package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.Optional;

public record HealAttributes(DeadLimbHealing deadLimbHealing) {

    public static final Codec<HealAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DeadLimbHealing.CODEC.optionalFieldOf("deadLimbHeal").forGetter(t -> Optional.ofNullable(t.deadLimbHealing))
    ).apply(instance, HealAttributes::new));

    private HealAttributes(Builder builder) {
        this(builder.deadLimbHealing);
    }

    private HealAttributes(Optional<DeadLimbHealing> deadLimbHealing) {
        this(deadLimbHealing.orElse(null));
    }

    public boolean canUseOn(LivingEntity entity, HealthContainer container) {
        if (this.canHealDeadLimbs()) {
            return this.deadLimbHealing.canHeal(entity, container);
        }
        return false;
    }

    public boolean canUseOnPart(BodyPart part, LivingEntity entity, HealthContainer container) {
        if (this.canHealDeadLimbs()) {
            return part.isDead(); // TODO or post effect heals are present and match the limb effects
        }
        return false;
    }

    public boolean canHealDeadLimbs() {
        return this.deadLimbHealing != null;
    }

    public int getConsumption() {
        if (this.canHealDeadLimbs()) {
            return 1;
        }
        return 20; // TODO
    }

    public static Builder builder() {
        return new Builder();
    }

    public record DeadLimbHealing(float healthAfterHeal) {
        // TODO post effects

        public static final Codec<DeadLimbHealing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("healthAfterHeal", 1.0F).forGetter(DeadLimbHealing::healthAfterHeal)
        ).apply(instance, DeadLimbHealing::new));

        public boolean canHeal(LivingEntity entity, HealthContainer container) {
            return container.getBodyPartStream().anyMatch(BodyPart::isDead);
        }

        public void applyPost(LivingEntity entity, HealthContainer container) {

        }
    }

    public static final class Builder {

        private DeadLimbHealing deadLimbHealing;

        private Builder() {
        }

        public Builder deadLimbHealing() {
            return this.deadLimbHealing(1.0F);
        }

        public Builder deadLimbHealing(float healthAfterHeal) {
            this.deadLimbHealing = new DeadLimbHealing(healthAfterHeal);
            return this;
        }

        public HealAttributes build() {
            return new HealAttributes(this);
        }
    }
}
