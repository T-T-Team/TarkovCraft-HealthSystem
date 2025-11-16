package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;

public class HasStatusEffectHealthEventSource implements HealthEventSource {

    public static final MapCodec<HasStatusEffectHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.type)
    ).apply(instance, HasStatusEffectHealthEventSource::new));

    private final Holder<StatusEffectType<?>> type;

    public HasStatusEffectHealthEventSource(Holder<StatusEffectType<?>> type) {
        this.type = type;
    }

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, Limb limb) {
        StatusEffectType<?> effectType = this.type.value();
        if (effectType.isGlobalEffect()) {
            return container.getGlobalStatusEffects().hasEffect(this.type);
        } else {
            return limb.getStatusEffects().hasEffect(this.type);
        }
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.HAS_EFFECT.get();
    }
}
