package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;

public record CopyIncomingEffectsEventAction() implements HealthEventAction {

    public static final CopyIncomingEffectsEventAction INSTANCE = new CopyIncomingEffectsEventAction();
    public static final MapCodec<CopyIncomingEffectsEventAction> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean apply(HealthEventContext ctx) {
        DamageContext context = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
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
    public MapCodec<? extends HealthEventAction> codec() {
        return CODEC;
    }
}
