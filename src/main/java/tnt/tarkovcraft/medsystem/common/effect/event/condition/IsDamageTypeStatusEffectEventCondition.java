package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.Optional;

public record IsDamageTypeStatusEffectEventCondition(Optional<TagKey<DamageType>> tag, Optional<ResourceLocation> identifier) implements StatusEffectEventCondition {

    public static final MapCodec<IsDamageTypeStatusEffectEventCondition> CODEC = RecordCodecBuilder.<IsDamageTypeStatusEffectEventCondition>mapCodec(instance -> instance.group(
            TagKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("tag").forGetter(IsDamageTypeStatusEffectEventCondition::tag),
            ResourceLocation.CODEC.optionalFieldOf("id").forGetter(IsDamageTypeStatusEffectEventCondition::identifier)
    ).apply(instance, IsDamageTypeStatusEffectEventCondition::new)).validate(condition -> {
        if (condition.tag.isEmpty() && condition.identifier.isEmpty()) {
            return DataResult.error(() -> "Either 'tag' or 'id' attribute must be defined for damage type condition");
        }
        return DataResult.success(condition);
    });

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        DamageContext context = ctx.getParameter(StatusEffectEventParams.DAMAGE_CONTEXT);
        if (context == null)
            return TriggerResult.INVALID;
        DamageSource source = context.getSource();
        if (this.tag.isPresent()) {
            return TriggerResult.condition(this.tag.map(source::is).orElse(false));
        } else {
            return TriggerResult.condition(this.identifier
                    .map(value -> source.is(ResourceKey.create(Registries.DAMAGE_TYPE, value)))
                    .orElse(false)
            );
        }
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.IS_DAMAGE.value();
    }
}
