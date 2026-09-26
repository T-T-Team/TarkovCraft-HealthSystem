package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResult;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public record SpecificLimbTagDamageFunction(Map<LimbType, TagKey<DamageType>> mappings) implements DamageFunction {

    public static final MapCodec<SpecificLimbTagDamageFunction> CODEC = Codec.unboundedMap(LimbType.CODEC, TagKey.codec(Registries.DAMAGE_TYPE))
            .xmap(SpecificLimbTagDamageFunction::new, SpecificLimbTagDamageFunction::mappings).fieldOf("mappings");

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        DamageSource damageSource = context.source();
        Set<LimbType> affected = EnumSet.noneOf(LimbType.class);
        for (var entry : this.mappings.entrySet()) {
            TagKey<DamageType> requiredTag = entry.getValue();
            if (damageSource.is(requiredTag)) {
                affected.add(entry.getKey());
            }
        }
        return new Calculator(affected);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }

    private record Calculator(Set<LimbType> affectedLimbs) implements HitCalculator {

        @Override
        public HitCalculationResult calculateHits(HitCalculationContext context) {
            LivingEntity entity = context.entity();
            return HitCalculationResult.simpleMappedResult(
                    context,
                    hitbox -> this.affectedLimbs.contains(hitbox.limb().getType()),
                    hitbox -> HitInfo.create(hitbox, entity)
            );
        }
    }
}
