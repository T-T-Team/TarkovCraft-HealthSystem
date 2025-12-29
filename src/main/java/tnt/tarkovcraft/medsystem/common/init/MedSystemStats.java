package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.statistic.Statistic;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemStats {

    public static final DeferredRegister<Statistic> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.STATISTICS, MedSystemConstants.MOD_ID);

    public static final Holder<Statistic> LIMBS_LOST = REGISTRY.register("limbs_lost", Statistic::new);
}
