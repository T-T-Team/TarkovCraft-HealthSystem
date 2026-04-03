package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
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
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;

import java.util.function.Consumer;

public record Surgery(float recoveryHealth, float healthMultiplier, int recoveryTime, int useTime) implements TooltipProvider {

    public static final Codec<Surgery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("recovery_health", 1.0F).forGetter(Surgery::recoveryHealth),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("health_multiplier", 1.0F).forGetter(Surgery::healthMultiplier),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("recovery_duration", Duration.minutes(10).tickValue()).forGetter(Surgery::recoveryTime),
            Codecs.NON_NEGATIVE_INT.fieldOf("use_duration").forGetter(Surgery::useTime)
    ).apply(instance, Surgery::new));

    public boolean canHeal(HealthContainer container) {
        LimbContainer limbContainer = container.getLimbContainer();
        return limbContainer.hasLimb(Limb::isDead);
    }

    public boolean hasPostRecovery() {
        return this.recoveryTime > 0 && this.healthMultiplier < 1.0F;
    }

    public void onSurgeryFinished(LivingEntity entity, Limb limb) {
        if (this.hasPostRecovery()) {
            float reductionScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_AMOUNT, 1.0F);
            float durationScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_DURATION, 1.0F);
            if (durationScale > 0.0F && reductionScale > 0.0F) {
                int reduction = Mth.ceil(limb.getRawMaxHealth() * (1.0F - this.healthMultiplier) * reductionScale);
                int duration = Mth.ceil(this.recoveryTime * durationScale);
                InjuryRecoveryStatusEffect effect = InjuryRecoveryStatusEffect.createTemplate(reduction);
                effect.setDuration(duration);
                StatusEffectHelper.addImmediateEffect(limb.getStatusEffects(), entity, limb, effect);
            }
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        Component health = Component.literal(String.valueOf(Mth.ceil(recoveryHealth))).withStyle(ChatFormatting.GRAY);
        tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.recovery", health).withStyle(ChatFormatting.DARK_GRAY));
        Component maxHealth = Component.literal((int) ((1.0F - healthMultiplier) * 100) + "%").withStyle(ChatFormatting.GRAY);
        if (this.hasPostRecovery()) {
            Component duration = Duration.format(recoveryTime).copy().withStyle(ChatFormatting.GRAY);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.max_health", maxHealth, duration).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static class SurgeryBuilder {

        private float healthAfterHeal = 1.0F;
        private float maxHealthMultiplier = 1.0F;
        private int recoveryTime = 0;
        private int useTime = 100;

        public SurgeryBuilder useTime(int useTime) {
            this.useTime = useTime;
            return this;
        }

        public SurgeryBuilder useTime(TickValue useTime) {
            return this.useTime(useTime.tickValue());
        }

        public SurgeryBuilder recoversTo(float healthAfterHeal) {
            this.healthAfterHeal = healthAfterHeal;
            return this;
        }

        public SurgeryBuilder postSurgeryRecovery(int recoveryTime, float maxHealthMultiplier) {
            this.recoveryTime = recoveryTime;
            this.maxHealthMultiplier = maxHealthMultiplier;
            return this;
        }

        public SurgeryBuilder postSurgeryRecovery(TickValue duration, float maxHealthMultiplier) {
            return this.postSurgeryRecovery(duration.tickValue(), maxHealthMultiplier);
        }

        Surgery buildSurgeryAttributes() {
            return new Surgery(this.healthAfterHeal, this.maxHealthMultiplier, this.recoveryTime, this.useTime);
        }
    }
}
