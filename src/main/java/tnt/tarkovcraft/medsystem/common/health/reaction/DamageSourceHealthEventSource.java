package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunction;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import java.util.List;

public class DamageSourceHealthEventSource extends ChanceHealthEventSource {

    public static final MapCodec<DamageSourceHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            TagKey.codec(Registries.DAMAGE_TYPE).fieldOf("source").forGetter(t -> t.damageType)
    ).apply(instance, DamageSourceHealthEventSource::new));

    private final TagKey<DamageType> damageType;

    public DamageSourceHealthEventSource(float baseChance, List<ChanceFunction> functions, TagKey<DamageType> damageType) {
        super(baseChance, functions);
        this.damageType = damageType;
    }

    @Override
    public boolean canReact(Context context) {
        BodyPart part = context.getOrDefault(MedicalSystemContextKeys.BODY_PART, null);
        if (part == null) {
            return false;
        }
        return context.get(ContextKeys.DAMAGE_SOURCE).map(source -> {
            if (!source.is(this.damageType)) {
                return false;
            }
            return super.canReact(context);
        }).orElse(false);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.DAMAGE_SOURCE.get();
    }
}
