package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PositiveEffectsGroup extends GroupStatusEffect {

    public static final MapCodec<PositiveEffectsGroup> CODEC = RecordCodecBuilder.mapCodec(instance -> commonGroup(instance).apply(instance, PositiveEffectsGroup::new));

    public PositiveEffectsGroup(List<EffectGroupHolder> items) {
        super(items);
    }

    public PositiveEffectsGroup(int duration, List<EffectGroupHolder> items) {
        super(duration, items);
    }

    public static PositiveEffectsGroup createTemplate(Consumer<EffectGroupHolder.Factory> tplBuilder) {
        List<EffectGroupHolder> items = new ArrayList<>();
        tplBuilder.accept(EffectGroupHolder.getFactory(items));
        return new PositiveEffectsGroup(items);
    }

    @Override
    public GroupStatusEffect newInstance(List<EffectGroupHolder> copiedItems) {
        return new PositiveEffectsGroup(copiedItems);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.POSITIVE_EFFECTS_GROUP.value();
    }
}
