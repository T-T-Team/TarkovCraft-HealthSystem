package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemAttributes {

    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(CoreRegistries.ATTRIBUTE, MedicalSystem.MOD_ID);

    public static final Holder<Attribute> LIMB_DEATH_CHANCE = REGISTRY.register("limb_death_chance", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> PAIN_RELIEF = REGISTRY.register("pain_relief", Attribute::create);
}
