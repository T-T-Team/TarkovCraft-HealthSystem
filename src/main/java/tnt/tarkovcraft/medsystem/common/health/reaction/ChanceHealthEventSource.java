package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunction;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import java.util.Collections;
import java.util.List;

public class ChanceHealthEventSource implements HealthEventSource {

    public static final MapCodec<ChanceHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, ChanceHealthEventSource::new));

    private final float baseChance;
    private final List<ChanceFunction> functions;

    public ChanceHealthEventSource(float baseChance, List<ChanceFunction> functions) {
        this.baseChance = baseChance;
        this.functions = functions;
    }

    public float getChance(Context context) {
        float result = this.baseChance;
        for (ChanceFunction function : this.functions) {
            result = function.apply(result, context);
        }
        return result;
    }

    @Override
    public boolean canReact(Context context) {
        return context.get(ContextKeys.LIVING_ENTITY).map(entity -> {
            RandomSource source = entity.getRandom();
            return source.nextFloat() < this.getChance(context);
        }).orElse(false);
    }

    public float getBaseChance() {
        return baseChance;
    }

    public List<ChanceFunction> getFunctions() {
        return functions;
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.CHANCE.get();
    }

    public static <T extends ChanceHealthEventSource> Products.P2<RecordCodecBuilder.Mu<T>, Float, List<ChanceFunction>> common(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.FLOAT.fieldOf("chance").forGetter(ChanceHealthEventSource::getBaseChance),
                ChanceFunctionType.CODEC.listOf().optionalFieldOf("chanceModifiers", Collections.emptyList()).forGetter(ChanceHealthEventSource::getFunctions)
        );
    }
}
