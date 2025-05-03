package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.Optional;
import java.util.function.Consumer;

public record HealAttributes(DeadLimbHealing deadLimbHealing) implements TooltipProvider {

    public static final Codec<HealAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DeadLimbHealing.CODEC.optionalFieldOf("deadLimbHeal").forGetter(t -> Optional.ofNullable(t.deadLimbHealing))
    ).apply(instance, HealAttributes::new));

    private HealAttributes(Builder builder) {
        this(builder.deadLimbHealing);
    }

    private HealAttributes(Optional<DeadLimbHealing> deadLimbHealing) {
        this(deadLimbHealing.orElse(null));
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (this.canHealDeadLimbs()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.title").withStyle(ChatFormatting.GRAY));
            Component health = Component.literal(String.valueOf(Mth.ceil(this.deadLimbHealing.healthAfterHeal))).withStyle(ChatFormatting.YELLOW);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.recovery", health).withStyle(ChatFormatting.DARK_GRAY));
            Component maxHealth = Component.literal((int) (this.deadLimbHealing.maxHealthMultiplier * 100) + "%").withStyle(ChatFormatting.YELLOW);
            Component duration = Component.literal("10 minutes").withStyle(ChatFormatting.YELLOW);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.max_health", maxHealth, duration).withStyle(ChatFormatting.DARK_GRAY));
        }
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

    public record DeadLimbHealing(float healthAfterHeal, float maxHealthMultiplier) {
        // TODO post effects

        public static final Codec<DeadLimbHealing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("healthAfterHeal", 1.0F).forGetter(DeadLimbHealing::healthAfterHeal),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("maxHealthMultiplier", 1.0F).forGetter(DeadLimbHealing::maxHealthMultiplier)
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
            return this.deadLimbHealing(1.0F, 1.0F);
        }

        public Builder deadLimbHealing(float healthAfterHeal, float maxHealthMultiplier) {
            this.deadLimbHealing = new DeadLimbHealing(healthAfterHeal, maxHealthMultiplier);
            return this;
        }

        public HealAttributes build() {
            return new HealAttributes(this);
        }
    }
}
