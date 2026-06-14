package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemAttributes {

    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.ATTRIBUTE, MedSystemConstants.MOD_ID);

    public static final Holder<Attribute> ARMOR_DURABILITY = register("armor_durability", 1.0);
    public static final Holder<Attribute> POSITIVE_EFFECT_DURATION = register("positive_effect_duration", 1.0);
    public static final Holder<Attribute> NEGATIVE_EFFECT_DURATION = register("negative_effect_duration", 1.0);
    public static final Holder<Attribute> POSITIVE_EFFECT_CHANCE = register("positive_effect_chance", 1.0);
    public static final Holder<Attribute> NEGATIVE_EFFECT_CHANCE = register("negative_effect_chance", 1.0);
    public static final Holder<Attribute> INJURY_RECOVERY_DURATION = register("injury_recovery_duration", 1.0);
    public static final Holder<Attribute> INJURY_RECOVERY_AMOUNT = register("injury_amount", 1.0);
    @SuppressWarnings("unused") // used within data files dynamically
    public static final Holder<Attribute> BLOOD_REGENERATION_AMOUNT = register("blood_regeneration_amount", 0.0005); // 0.0005 * 1200 = 0.6L / mc day
    @SuppressWarnings("unused") // used within data files dynamically
    public static final Holder<Attribute> RANDOM_BLACKOUT_CHANCE = register("random_blackout_chance", 0.05F); // 5%/s
    public static final Holder<Attribute> SHOCK_SCALE = register("shock_scale", 1.0);

    private static Holder<Attribute> register(String name, double value) {
        return REGISTRY.register(name, key -> Attribute.create(key, value));
    }
}
