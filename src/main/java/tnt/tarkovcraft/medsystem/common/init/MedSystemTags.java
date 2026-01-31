package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public final class MedSystemTags {

    public static final class DamageTypes {

        public static final TagKey<DamageType> IS_GENERIC = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("is_generic"));
        public static final TagKey<DamageType> IS_MOVEMENT_RESTRICTED = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("movement_restricted"));
        public static final TagKey<DamageType> BLEED_CAUSING = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("bleed_causing"));
        public static final TagKey<DamageType> FRACTURE_CAUSING = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.resource("fracture_causing"));
    }

    public static final class StatusEffects {

        public static final TagKey<StatusEffectType<?>> DISABLED = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("disabled"));
        public static final TagKey<StatusEffectType<?>> MOVEMENT_RESTRICTING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("movement_restricting"));
        public static final TagKey<StatusEffectType<?>> IS_PAIN_CAUSING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_pain_causing"));
        public static final TagKey<StatusEffectType<?>> IS_PAIN_RELIEF = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_pain_relief"));
        public static final TagKey<StatusEffectType<?>> IS_BLEED = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_bleed"));
        public static final TagKey<StatusEffectType<?>> IS_FRACTURE = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.resource("is_fracture"));
    }

    public static final class Entities {

        public static final TagKey<EntityType<?>> NO_LIMB_HIT_APPROXIMATION = TagKey.create(Registries.ENTITY_TYPE, MedicalSystem.resource("no_limb_hit_approximation"));
    }
}
