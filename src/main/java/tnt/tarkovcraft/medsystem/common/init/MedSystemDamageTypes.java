package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public interface MedSystemDamageTypes {

    ResourceKey<DamageType> BROKEN_LEG = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("broken_leg"));
    ResourceKey<DamageType> BLEED = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("bleed"));

    static Holder<DamageType> of(RegistryAccess access, ResourceKey<DamageType> type) {
        Registry<DamageType> registry = access.lookupOrThrow(Registries.DAMAGE_TYPE);
        return registry.getOrThrow(type);
    }
}
