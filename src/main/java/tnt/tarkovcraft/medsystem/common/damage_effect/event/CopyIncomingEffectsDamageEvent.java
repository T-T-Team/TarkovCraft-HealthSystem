package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffect;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

public record CopyIncomingEffectsDamageEvent() implements DamageEffectEvent {

    public static final CopyIncomingEffectsDamageEvent INSTANCE = new CopyIncomingEffectsDamageEvent();
    public static final MapCodec<CopyIncomingEffectsDamageEvent> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(DamageEffectContext context) {
        SideEffectHolder holder = context.damageContext().getSideEffects();
        if (holder != null) {
            LivingEntity entity = context.target();
            Limb limb = context.limb();
            HealthContainer healthContainer = context.health();
            DamageSource source = context.damageContext().getSource();
            holder.apply(entity, source, healthContainer, limb);
        }
    }

    @Override
    public void validate(DamageEffectContextType type) {
        DamageEffect.validateContext(this, type, DamageEffectContextType.ON_HURT);
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.COPY_INCOMING_EFFECTS.value();
    }
}
