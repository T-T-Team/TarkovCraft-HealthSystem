package tnt.tarkovcraft.medsystem.common.effect.event.action;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventActions;

public class NoStatusEffectEventAction implements StatusEffectEventAction {

    public static final NoStatusEffectEventAction INSTANCE = new NoStatusEffectEventAction();
    public static final MapCodec<NoStatusEffectEventAction> CODEC = MapCodec.unit(INSTANCE);

    private NoStatusEffectEventAction() {
    }

    @Override
    public boolean apply(StatusEffectEventContext ctx) {
        return false;
    }

    @Override
    public StatusEffectEventActionType<?> getType() {
        return MedSystemStatusEffectEventActions.NONE.value();
    }
}
