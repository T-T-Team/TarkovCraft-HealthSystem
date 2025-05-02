package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemTags {

    public static final class DamageTypes {

        public static final TagKey<DamageType> IS_GENERIC = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("is_generic"));
    }
}
