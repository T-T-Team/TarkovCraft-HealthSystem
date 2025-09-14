package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public final class MedSystemTags {

    public static final class DamageTypes {

        public static final TagKey<DamageType> IS_GENERIC = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("is_generic"));
        public static final TagKey<DamageType> IS_MOVEMENT_RESTRICTED = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("movement_restricted"));
    }

    public static final class StatusEffects {

        public static final TagKey<StatusEffectType<?>> OVERWEIGHT = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("overweight"));
        public static final TagKey<StatusEffectType<?>> MOVEMENT_RESTRICTING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("movement_restricting"));
        public static final TagKey<StatusEffectType<?>> IS_PAIN_CAUSING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_pain_causing"));
        public static final TagKey<StatusEffectType<?>> IS_BLEED = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_bleed"));
    }
}
