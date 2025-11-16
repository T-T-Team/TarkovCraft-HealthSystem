package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunction;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;
import java.util.List;

public class DamageSourceHealthEventSource extends ChanceHealthEventSource {

    public static final MapCodec<DamageSourceHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            TagKey.codec(Registries.DAMAGE_TYPE).fieldOf("source").forGetter(t -> t.damageType)
    ).apply(instance, DamageSourceHealthEventSource::new));

    private final TagKey<DamageType> damageType;

    public DamageSourceHealthEventSource(Either<NumberProvider, Float> baseChance, List<ChanceFunction> functions, TagKey<DamageType> damageType) {
        super(baseChance, functions);
        this.damageType = damageType;
    }

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, Limb limb) {
        if (damageSource == null) {
            return false;
        }
        if (!damageSource.is(this.damageType)) {
            return false;
        }
        return super.canReact(container, entity, damageSource, limb);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.DAMAGE_SOURCE.get();
    }
}
