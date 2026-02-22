package tnt.tarkovcraft.medsystem.common.effect.event.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventActions;

public record CopyIncomingEffectsStatusEventAction() implements StatusEffectEventAction {

    public static final CopyIncomingEffectsStatusEventAction INSTANCE = new CopyIncomingEffectsStatusEventAction();
    public static final MapCodec<CopyIncomingEffectsStatusEventAction> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean apply(StatusEffectEventContext ctx) {
        DamageContext context = ctx.getParameter(StatusEffectEventParams.DAMAGE_CONTEXT);
        if (context == null)
            return false;
        SideEffectHolder holder = context.getEffects();
        if (holder != null) {
            LivingEntity entity = ctx.getEntity();
            Limb limb = ctx.getLimb();
            HealthContainer healthContainer = ctx.getHealthContainer();
            DamageSource source = context.getSource();
            holder.apply(entity, source, healthContainer, limb);
        }
        return true;
    }

    @Override
    public StatusEffectEventActionType<?> getType() {
        return MedSystemStatusEffectEventActions.COPY_INCOMING_EFFECTS.value();
    }
}
