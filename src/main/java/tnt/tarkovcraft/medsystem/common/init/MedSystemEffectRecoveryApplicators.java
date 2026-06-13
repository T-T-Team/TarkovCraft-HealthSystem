package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicatorType;
import tnt.tarkovcraft.medsystem.common.health.applicator.BleedEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.applicator.FractureEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.applicator.SimpleEffectRecoveryApplicator;

public final class MedSystemEffectRecoveryApplicators {

    public static final DeferredRegister<EffectRecoveryApplicatorType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.EFFECT_RECOVERY_APPLICATOR, MedSystemConstants.MOD_ID);

    public static final Holder<EffectRecoveryApplicatorType<?>> SIMPLE = register("simple", SimpleEffectRecoveryApplicator.CODEC);
    public static final Holder<EffectRecoveryApplicatorType<?>> BLEED = register("bleed", BleedEffectRecoveryApplicator.CODEC);
    public static final Holder<EffectRecoveryApplicatorType<?>> FRACTURE = register("fracture", FractureEffectRecoveryApplicator.CODEC);

    private static Holder<EffectRecoveryApplicatorType<?>> register(String name, MapCodec<? extends EffectRecoveryApplicator> codec) {
        return REGISTRY.register(name, key -> new EffectRecoveryApplicatorType<>(key, codec));
    }
}
