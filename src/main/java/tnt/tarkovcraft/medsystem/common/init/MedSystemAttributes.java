package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemAttributes {

    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.ATTRIBUTE, MedicalSystem.MOD_ID);

    public static final Holder<Attribute> ARMOR_DURABILITY = REGISTRY.register("armor_durability", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> POSITIVE_EFFECT_DURATION = REGISTRY.register("positive_effect_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> NEGATIVE_EFFECT_DURATION = REGISTRY.register("negative_effect_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> POSITIVE_EFFECT_CHANCE = REGISTRY.register("positive_effect_chance", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> NEGATIVE_EFFECT_CHANCE = REGISTRY.register("negative_effect_chance", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> INJURY_RECOVERY_DURATION = REGISTRY.register("injury_recovery_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> INJURY_RECOVERY_AMOUNT = REGISTRY.register("injury_amount", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> BLOOD_REGENERATION_AMOUNT = REGISTRY.register("blood_regeneration_amount", key -> Attribute.create(key, 0.0005)); // 0.0005 * 1200 = 0.6L / mc day
    public static final Holder<Attribute> RANDOM_BLACKOUT_CHANCE = REGISTRY.register("random_blackout_chance", key -> Attribute.create(key, 0.05F)); // 5%/s
    public static final Holder<Attribute> UNCONSCIOUS_ON_LIMB_LOSS_CHANCE = REGISTRY.register("unconscious_on_limb_loss_chance", key -> Attribute.create(key, 0.20F));
    public static final Holder<Attribute> UNCONSCIOUS_ON_LIMB_LOSS_SCALING = REGISTRY.register("unconscious_on_limb_loss_scaling", key -> Attribute.createBool(key, false));
}
