package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Optional;

public interface MedSystemDamageTypes {

    ResourceKey<DamageType> FRACTURE = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("fracture"));
    ResourceKey<DamageType> BLEED = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("bleed"));
    ResourceKey<DamageType> TOXIC_SIDE_EFFECT = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("toxic_side_effect"));
    ResourceKey<DamageType> IMMUNE_REACTION = ResourceKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("immune_reaction"));

    static DamageSource causeFractureDamage(RegistryAccess access) {
        return new DamageSource(of(access, FRACTURE));
    }

    static DamageSource causeBleedDamage(RegistryAccess access, Optional<Entity> cause) {
        Entity causingEntity = cause.orElse(null);
        return new DamageSource(of(access, BLEED), causingEntity);
    }

    static DamageSource causeToxinDamage(RegistryAccess access) {
        return new DamageSource(of(access, TOXIC_SIDE_EFFECT));
    }

    static DamageSource causeImmuneReactionDamage(RegistryAccess access) {
        return new DamageSource(of(access, IMMUNE_REACTION));
    }

    static Holder<DamageType> of(RegistryAccess access, ResourceKey<DamageType> type) {
        HolderLookup.RegistryLookup<DamageType> registry = access.lookupOrThrow(Registries.DAMAGE_TYPE);
        return registry.getOrThrow(type);
    }
}
