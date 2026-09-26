package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

public final class IsTaggedLimbDamage implements DamageCondition {

    private static final IsTaggedLimbDamage INSTANCE = new IsTaggedLimbDamage();
    public static final MapCodec<IsTaggedLimbDamage> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(HitCalculationContext hitCalculationContext) {
        DamageSource damageSource = hitCalculationContext.source();
        return damageSource.is(MedSystemTags.DamageTypes.LIMB_DAMAGE_TYPES);
    }

    @Override
    public MapCodec<? extends DamageCondition> codec() {
        return CODEC;
    }
}
