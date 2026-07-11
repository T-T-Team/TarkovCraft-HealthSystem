package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

import java.util.List;

public final class DamageTypeCondition implements DamageCondition {

    public static final MapCodec<DamageTypeCondition> CODEC = Identifier.CODEC.listOf()
            .xmap(DamageTypeCondition::fromIdList, condition -> condition.resourceKeys.stream().map(ResourceKey::identifier).toList()).fieldOf("values");

    private final List<ResourceKey<DamageType>> resourceKeys;

    public DamageTypeCondition(List<ResourceKey<DamageType>> resourceKeys) {
        this.resourceKeys = resourceKeys;
    }

    public static DamageTypeCondition fromIdList(List<Identifier> ids) {
        var keys = ids.stream()
                .map(id -> ResourceKey.create(Registries.DAMAGE_TYPE, id))
                .toList();
        return new DamageTypeCondition(keys);
    }

    @Override
    public boolean test(HitCalculationContext context) {
        return this.resourceKeys.stream()
                .anyMatch(context::isDamage);
    }

    @Override
    public MapCodec<? extends DamageCondition> codec() {
        return CODEC;
    }
}
