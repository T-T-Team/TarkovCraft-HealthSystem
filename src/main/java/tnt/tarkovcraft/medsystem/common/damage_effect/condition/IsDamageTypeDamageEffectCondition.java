package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffect;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

import java.util.Optional;

public record IsDamageTypeDamageEffectCondition(Optional<TagKey<DamageType>> tag, Optional<Identifier> identifier) implements DamageEffectCondition {

    public static final MapCodec<IsDamageTypeDamageEffectCondition> CODEC = RecordCodecBuilder.<IsDamageTypeDamageEffectCondition>mapCodec(instance -> instance.group(
            TagKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("tag").forGetter(IsDamageTypeDamageEffectCondition::tag),
            Identifier.CODEC.optionalFieldOf("id").forGetter(IsDamageTypeDamageEffectCondition::identifier)
    ).apply(instance, IsDamageTypeDamageEffectCondition::new)).validate(condition -> {
        if (condition.tag.isEmpty() && condition.identifier.isEmpty()) {
            return DataResult.error(() -> "Either 'tag' or 'id' attribute must be defined for damage type condition");
        }
        return DataResult.success(condition);
    });

    @Override
    public boolean matches(DamageEffectContext context) {
        DamageSource source = context.damageContext().getSource();
        if (this.tag.isPresent()) {
            return this.tag.map(source::is).orElse(false);
        } else {
            this.identifier.ifPresent(value -> source.is(ResourceKey.create(Registries.DAMAGE_TYPE, value)));
        }
        return false;
    }

    @Override
    public void validate(DamageEffectContextType contextType) {
        DamageEffect.validateContext(this, contextType, DamageEffectContextType.ON_HURT);
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.IS_DAMAGE.value();
    }
}
