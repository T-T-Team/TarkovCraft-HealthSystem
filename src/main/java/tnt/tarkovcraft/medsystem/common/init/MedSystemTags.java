package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public final class MedSystemTags {

    public static final class DamageTypes {

        // Used for compatibility with modded damage sources to easily map damage to specific limb types
        public static final TagKey<DamageType> LIMB_DAMAGE_TYPES = TagKey.create(Registries.DAMAGE_TYPE, MedicalSystem.createIdentifier("limb/limb_damage_types"));
    }

    public static final class StatusEffects {

        public static final TagKey<StatusEffectType<?>> DISABLED = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("disabled"));
        public static final TagKey<StatusEffectType<?>> MOVEMENT_RESTRICTING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("movement_restricting"));
        public static final TagKey<StatusEffectType<?>> IS_PAIN_CAUSING = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("is_pain_causing"));
        public static final TagKey<StatusEffectType<?>> IS_PAIN_RELIEF = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("is_pain_relief"));
        public static final TagKey<StatusEffectType<?>> IS_BLEED = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("is_bleed"));
        public static final TagKey<StatusEffectType<?>> IS_FRACTURE = TagKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.createIdentifier("is_fracture"));
    }

    public static final class Entities {

        public static final TagKey<EntityType<?>> UNCONSCIOUS_MOUNTABLE = TagKey.create(Registries.ENTITY_TYPE, MedicalSystem.createIdentifier("unconscious_mountable"));
        public static final TagKey<EntityType<?>> UNCONSCIOUS_DISMOUNTABLE = TagKey.create(Registries.ENTITY_TYPE, MedicalSystem.createIdentifier("unconscious_dismountable"));
    }
}
