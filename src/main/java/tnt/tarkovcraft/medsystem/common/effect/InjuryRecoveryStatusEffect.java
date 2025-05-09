package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Locale;
import java.util.function.Consumer;

public class InjuryRecoveryStatusEffect extends StatusEffect {

    public static final MapCodec<InjuryRecoveryStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.INT.fieldOf("reduction").forGetter(t -> t.reduction)
    ).apply(instance, InjuryRecoveryStatusEffect::new));

    private int reduction;

    public InjuryRecoveryStatusEffect(int duration, int delay) {
        this(duration, delay, 1);
    }

    public InjuryRecoveryStatusEffect(int duration, int delay, int reduction) {
        super(duration, delay);
        this.reduction = reduction;
    }

    @Override
    public void apply(Context context) {
        if (this.reduction < 1)
            return;
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        context.get(MedicalSystemContextKeys.BODY_PART).ifPresent(part -> {
            this.reduction = Math.min((int) part.getOriginalMaxHealth() - 1, this.reduction);
            int newMaxHealth = (int) part.getOriginalMaxHealth() - this.reduction;
            AttributeMap map = entity.getAttributes();
            AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
            ResourceLocation modifierId = this.getUniqueModifierId(part);
            if (newMaxHealth != part.getMaxHealth()) {
                if (instance.hasModifier(modifierId)) {
                    instance.removeModifier(modifierId);
                }
                instance.addPermanentModifier(new AttributeModifier(modifierId, -this.reduction, AttributeModifier.Operation.ADD_VALUE));
                part.setMaxHealth(newMaxHealth);
                HealthContainer container = context.getOrThrow(MedicalSystemContextKeys.HEALTH_CONTAINER);
                container.updateHealth(entity);
                HealthSystem.synchronizeEntity(entity);
            }
            if (!instance.hasModifier(modifierId)) {
                instance.addPermanentModifier(new AttributeModifier(modifierId, -this.reduction, AttributeModifier.Operation.ADD_VALUE));
            }
        });
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        context.get(MedicalSystemContextKeys.BODY_PART).ifPresent(part -> {
            part.setMaxHealth(Math.min(part.getMaxHealth() + this.reduction, part.getOriginalMaxHealth()));
            HealthContainer container = context.getOrThrow(MedicalSystemContextKeys.HEALTH_CONTAINER);
            container.updateHealth(entity);
            HealthSystem.synchronizeEntity(entity);
            AttributeMap map = entity.getAttributes();
            AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
            instance.removeModifier(this.getUniqueModifierId(part));
        });
        return null;
    }

    public void setReduction(int reduction) {
        this.reduction = reduction;
    }

    @Override
    public StatusEffect copy() {
        return new InjuryRecoveryStatusEffect(this.getDuration(), this.getDelay(), this.reduction);
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("status_effect.medsystem.injury_recovery.info").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.INJURY_RECOVERY.value();
    }

    private ResourceLocation getUniqueModifierId(BodyPart part) {
        return MedicalSystem.resource("health_reduction/" + part.getName().toLowerCase(Locale.ROOT));
    }

    public static InjuryRecoveryStatusEffect merge(InjuryRecoveryStatusEffect initial, InjuryRecoveryStatusEffect additional) {
        return new InjuryRecoveryStatusEffect(
                initial.getDuration() + additional.getDuration(),
                initial.getDelay() + additional.getDelay(),
                initial.reduction + additional.reduction
        );
    }
}
