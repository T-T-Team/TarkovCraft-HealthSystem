package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.blood_system.effect.*;

public final class MedSystemBloodLevelEffects {

    public static final DeferredRegister<BloodLevelEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.BLOOD_LEVEL_EFFECT, MedSystemConstants.MOD_ID);

    public static final Holder<BloodLevelEffectType<?>> DEATH = REGISTRY.register("death", key -> new BloodLevelEffectType<>(key, DeathBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> UNCONSCIOUS = REGISTRY.register("unconscious", key -> new BloodLevelEffectType<>(key, UnconsciousBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> CONFIGURABLE_UNCONSCIOUS = REGISTRY.register("configurable_unconscious", key -> new BloodLevelEffectType<>(key, ConfigurableUnconsciousBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> APPLY_UNCONSCIOUS_CONFIG = REGISTRY.register("apply_unconscious_config", key -> new BloodLevelEffectType<>(key, ApplyUnconsciousConfigBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> ADD_VANILLA_ATTRIBUTE_MODIFIER = REGISTRY.register("add_vanilla_attribute_modifier", key -> new BloodLevelEffectType<>(key, AddVanillaAttributeModifierBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> ADD_ATTRIBUTE_MODIFIER = REGISTRY.register("add_attribute_modifier", key -> new BloodLevelEffectType<>(key, AddAttributeModifierBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> REMOVE_VANILLA_ATTRIBUTE_MODIFIER = REGISTRY.register("remove_vanilla_attribute_modifier", key -> new BloodLevelEffectType<>(key, RemoveVanillaAttributeModifierBloodLevelEffect.CODEC));
    public static final Holder<BloodLevelEffectType<?>> REMOVE_ATTRIBUTE_MODIFIER = REGISTRY.register("remove_attribute_modifier", key -> new BloodLevelEffectType<>(key, RemoveAttributeModifierBloodLevelEffect.CODEC));
}
