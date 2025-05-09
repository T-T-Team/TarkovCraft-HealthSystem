package tnt.tarkovcraft.medsystem.api.heal;

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
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.InjuryRecoveryStatusEffect;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;

import java.util.function.Consumer;

public record DeadLimbHealing(float healthAfterHeal, float maxHealthMultiplier, float minLimbHealth,
                              int recoveryTime, int useTime) implements TooltipProvider {

    public static final Codec<DeadLimbHealing> CODEC = RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("healthAfterHeal", 1.0F).forGetter(DeadLimbHealing::healthAfterHeal), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("maxHealthMultiplier", 1.0F).forGetter(DeadLimbHealing::maxHealthMultiplier), Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("minLimbHealth", 0.0F).forGetter(DeadLimbHealing::minLimbHealth), Codecs.NON_NEGATIVE_INT.optionalFieldOf("recoveryTime", 0).forGetter(DeadLimbHealing::recoveryTime), Codecs.NON_NEGATIVE_INT.fieldOf("useTime").forGetter(DeadLimbHealing::useTime)).apply(instance, DeadLimbHealing::new));

    public boolean canHeal(LivingEntity entity, HealthContainer container) {
        return container.getBodyPartStream().anyMatch(part -> part.isDead() && part.getMaxHealth() >= this.minLimbHealth);
    }

    public boolean hasPostRecovery() {
        return this.recoveryTime > 0 && this.maxHealthMultiplier < 1.0F;
    }

    public void addRecoveryAttributes(LivingEntity entity, BodyPart part) {
        if (this.hasPostRecovery()) {
            float reductionScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_AMOUNT, 1.0F);
            float durationScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_DURATION, 1.0F);
            if (durationScale > 0.0F && reductionScale > 0.0F) {
                int reduction = Mth.ceil(part.getMaxHealth() * (1.0F - this.maxHealthMultiplier) * reductionScale);
                int duration = Mth.ceil(Duration.minutes(10).tickValue() * reduction);
                InjuryRecoveryStatusEffect effect = new InjuryRecoveryStatusEffect(duration, reduction);
                part.getStatusEffects().addEffect(effect);
            }
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        Component health = Component.literal(String.valueOf(Mth.ceil(healthAfterHeal))).withStyle(ChatFormatting.YELLOW);
        tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.recovery", health).withStyle(ChatFormatting.DARK_GRAY));
        Component maxHealth = Component.literal((int) ((1.0F - maxHealthMultiplier) * 100) + "%").withStyle(ChatFormatting.YELLOW);
        if (this.hasPostRecovery()) {
            Component duration = Duration.format(recoveryTime).copy().withStyle(ChatFormatting.YELLOW);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.max_health", maxHealth, duration).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static class SurgeryBuilder {

        private final HealItemAttributes.Builder parent;
        private float healthAfterHeal = 1.0F;
        private float maxHealthMultiplier = 1.0F;
        private float minLimbHealth = 0.0F;
        private int recoveryTime = 0;
        private int useTime = 100;

        SurgeryBuilder(HealItemAttributes.Builder parent) {
            this.parent = parent;
        }

        public SurgeryBuilder useTime(int useTime) {
            this.useTime = useTime;
            return this;
        }

        public SurgeryBuilder useTime(TickValue useTime) {
            return this.useTime(useTime.tickValue());
        }

        public SurgeryBuilder recoverHealth(float healthAfterHeal) {
            this.healthAfterHeal = healthAfterHeal;
            return this;
        }

        public SurgeryBuilder minLimbHealth(float minLimbHealth) {
            this.minLimbHealth = minLimbHealth;
            return this;
        }

        public SurgeryBuilder recovery(int recoveryTime, float maxHealthMultiplier) {
            this.recoveryTime = recoveryTime;
            this.maxHealthMultiplier = maxHealthMultiplier;
            return this;
        }

        public SurgeryBuilder recovery(TickValue duration, float maxHealthMultiplier) {
            return this.recovery(duration.tickValue(), maxHealthMultiplier);
        }

        public HealItemAttributes.Builder buildSurgeryAttributes() {
            this.parent.deadLimbHealing = new DeadLimbHealing(this.healthAfterHeal, this.maxHealthMultiplier, this.minLimbHealth, this.recoveryTime, this.useTime);
            return this.parent;
        }
    }
}
