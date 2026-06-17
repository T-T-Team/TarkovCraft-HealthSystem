package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.advancements.criterion.LoseLimbTrigger;
import tnt.tarkovcraft.medsystem.common.advancements.criterion.ReceiveStatusEffectTrigger;

public final class MedSystemCriterionTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY = DeferredRegister.create(Registries.TRIGGER_TYPE, MedSystemConstants.MOD_ID);

    public static final Holder<CriterionTrigger<?>> RECEIVE_STATUS_EFFECT = REGISTRY.register("receive_status_effect", ReceiveStatusEffectTrigger::new);
    public static final Holder<CriterionTrigger<?>> LOSE_LIMB = REGISTRY.register("lose_limb", LoseLimbTrigger::new);
}
