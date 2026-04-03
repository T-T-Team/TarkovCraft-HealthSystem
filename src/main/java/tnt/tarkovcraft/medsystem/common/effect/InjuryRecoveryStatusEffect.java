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
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import java.util.function.Consumer;

public class InjuryRecoveryStatusEffect extends StatusEffect {

    public static final MapCodec<InjuryRecoveryStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.INT.fieldOf("reduction").forGetter(t -> t.reduction)
    ).apply(instance, InjuryRecoveryStatusEffect::new));
    private static final Component INFO = Component.translatable("status_effect.medsystem.injury_recovery.info").withStyle(ChatFormatting.DARK_GRAY);
    private static final ResourceLocation REDUCTION_ID = MedicalSystem.resource("reduction/injury_recovery");

    private final int reduction;

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
    public void apply(StatusEffectContext context) {
        Limb limb = context.limb();
        if (this.reduction < 1 || limb == null) {
            this.markForRemoval();
            return;
        }
        LivingEntity entity = context.entity();
        HealthContainer container = context.container();
        ResourceLocation modifierId = limb.getUniqueIdentifier();
        float previousReduction = limb.getTotalReduction();
        limb.addReduction(REDUCTION_ID, this.reduction);
        float newReduction = limb.getTotalReduction();
        if (previousReduction != newReduction) {
            AttributeMap map = entity.getAttributes();
            AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
            instance.addOrReplacePermanentModifier(new AttributeModifier(modifierId, -newReduction, AttributeModifier.Operation.ADD_VALUE));
            HealthHelper.synchronizeHealth(entity, container);
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
        context.ifLimbPresent(limb -> {
            HealthContainer container = context.container();
            LivingEntity entity = context.entity();
            limb.removeReduction(REDUCTION_ID);
            AttributeMap map = entity.getAttributes();
            AttributeInstance instance = map.getInstance(Attributes.MAX_HEALTH);
            instance.removeModifier(limb.getUniqueIdentifier());
            HealthHelper.synchronizeHealth(entity, container);
            HealthSystem.synchronizeEntity(entity);
        });
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
