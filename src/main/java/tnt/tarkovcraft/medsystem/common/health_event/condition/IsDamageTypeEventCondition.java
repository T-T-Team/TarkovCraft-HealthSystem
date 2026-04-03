package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

import java.util.Optional;

public record IsDamageTypeEventCondition(Optional<TagKey<DamageType>> tag, Optional<ResourceLocation> identifier) implements HealthEventCondition {

    public static final MapCodec<IsDamageTypeEventCondition> CODEC = RecordCodecBuilder.<IsDamageTypeEventCondition>mapCodec(instance -> instance.group(
            TagKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("tag").forGetter(IsDamageTypeEventCondition::tag),
            ResourceLocation.CODEC.optionalFieldOf("id").forGetter(IsDamageTypeEventCondition::identifier)
    ).apply(instance, IsDamageTypeEventCondition::new)).validate(condition -> {
        if (condition.tag.isEmpty() && condition.identifier.isEmpty()) {
            return DataResult.error(() -> "Either 'tag' or 'id' attribute must be defined for damage type condition");
        }
        return DataResult.success(condition);
    });

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        DamageContext context = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
        if (context == null)
            return HealthEventResult.INVALID;
        DamageSource source = context.getSource();
        if (this.tag.isPresent()) {
            return HealthEventResult.condition(this.tag.map(source::is).orElse(false));
        } else {
            return HealthEventResult.condition(this.identifier
                    .map(value -> source.is(ResourceKey.create(Registries.DAMAGE_TYPE, value)))
                    .orElse(false)
            );
        }
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.IS_DAMAGE.value();
    }
}
