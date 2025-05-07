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
    public static final Holder<Attribute> ARMOR_DURABILITY = REGISTRY.register("armor_durability", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> POSITIVE_EFFECT_DURATION = REGISTRY.register("positive_effect_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> NEGATIVE_EFFECT_DURATION = REGISTRY.register("negative_effect_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> POSITIVE_EFFECT_CHANCE = REGISTRY.register("positive_effect_chance", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> NEGATIVE_EFFECT_CHANCE = REGISTRY.register("negative_effect_chance", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> INJURY_RECOVERY_DURATION = REGISTRY.register("injury_recovery_duration", key -> Attribute.create(key, 1.0));
    public static final Holder<Attribute> INJURY_RECOVERY_AMOUNT = REGISTRY.register("injury_amount", key -> Attribute.create(key, 1.0));
}
