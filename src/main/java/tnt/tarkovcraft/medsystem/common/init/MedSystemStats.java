package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.statistic.Statistic;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemStats {

    public static final DeferredRegister<Statistic> REGISTRY = DeferredRegister.create(CoreRegistries.STATISTICS, MedicalSystem.MOD_ID);

    public static final Holder<Statistic> LIMBS_LOST = REGISTRY.register("limbs_lost", Statistic::new);
    public static final Holder<Statistic> HEADSHOTS = REGISTRY.register("headshots", Statistic::new);
    public static final Holder<Statistic> PLAYER_HEADSHOTS = REGISTRY.register("player_headshots", Statistic::new);
}
