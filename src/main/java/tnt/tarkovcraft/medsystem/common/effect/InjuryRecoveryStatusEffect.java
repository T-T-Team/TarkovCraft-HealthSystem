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
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public class InjuryRecoveryStatusEffect extends StatusEffect {

    public static final MapCodec<InjuryRecoveryStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.INT.fieldOf("reduction").forGetter(t -> t.reduction)
    ).apply(instance, InjuryRecoveryStatusEffect::new));
    private static final Component INFO = Component.translatable("status_effect.medsystem.injury_recovery.info").withStyle(ChatFormatting.DARK_GRAY);

    private int reduction;

    public InjuryRecoveryStatusEffect(int duration) {
        this(duration, 1);
    }

    public InjuryRecoveryStatusEffect(int duration, int reduction) {
        super(duration);
        this.reduction = reduction;
    }

    public static InjuryRecoveryStatusEffect createTemplate(int reduction) {
        return new InjuryRecoveryStatusEffect(-1, reduction);
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (this.reduction < 1 || limb == null) {
            this.markForRemoval();
            return;
        }
        this.reduction = Math.min((int) limb.getMaxHealth() - 1, this.reduction);
        AttributeMap map = entity.getAttributes();
        AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
        ResourceLocation modifierId = this.getUniqueModifierId(limb);
        AttributeModifier modifier = instance.getModifier(modifierId);
        if (modifier == null || modifier.amount() != -this.reduction) {
            float newMaxHealth = limb.getMaxHealth() - this.reduction;
            instance.addOrReplacePermanentModifier(new AttributeModifier(modifierId, -this.reduction, AttributeModifier.Operation.ADD_VALUE));
            limb.setMaxHealth(newMaxHealth);
            container.updateHealth(entity);
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (limb != null) {
            limb.setMaxHealth(limb.getMaxHealth() + this.reduction);
            AttributeMap map = entity.getAttributes();
            AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
            instance.removeModifier(this.getUniqueModifierId(limb));
            container.updateHealth(entity);
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @Override
    public StatusEffect copy() {
        return new InjuryRecoveryStatusEffect(this.getDuration(), this.reduction);
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(INFO);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.INJURY_RECOVERY.value();
    }

    private ResourceLocation getUniqueModifierId(Limb part) {
        return MedicalSystem.resource("health_reduction/" + part.getLimbCode().toLowerCase(Locale.ROOT));
    }

    public static InjuryRecoveryStatusEffect merge(InjuryRecoveryStatusEffect initial, InjuryRecoveryStatusEffect additional) {
        boolean allowScaling = MedicalSystem.getConfig().allowInjuryRecoveryScaling;
        if (allowScaling) {
            return new InjuryRecoveryStatusEffect(
                    initial.getDuration() + additional.getDuration(),
                    initial.reduction + additional.reduction
            );
        } else {
            return new InjuryRecoveryStatusEffect(
                    Math.max(initial.getDuration(), additional.getDuration()),
                    initial.reduction // keep same reduction
            );
        }
    }
}
