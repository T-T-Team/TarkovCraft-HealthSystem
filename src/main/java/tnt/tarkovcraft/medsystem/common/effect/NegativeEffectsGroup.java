package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NegativeEffectsGroup extends GroupStatusEffect {

    public static final MapCodec<NegativeEffectsGroup> CODEC = RecordCodecBuilder.mapCodec(instance -> commonGroup(instance).apply(instance, NegativeEffectsGroup::new));

    public NegativeEffectsGroup(List<EffectGroupHolder> items) {
        super(items);
    }

    public NegativeEffectsGroup(int duration, int delay, List<EffectGroupHolder> items) {
        super(duration, delay, items);
    }

    public static NegativeEffectsGroup createTemplate(Consumer<EffectGroupHolder.Factory> tplBuilder) {
        List<EffectGroupHolder> items = new ArrayList<>();
        tplBuilder.accept(EffectGroupHolder.getFactory(items));
        return new NegativeEffectsGroup(items);
    }

    @Override
    public GroupStatusEffect newInstance(List<EffectGroupHolder> copiedItems) {
        return new NegativeEffectsGroup(copiedItems);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.NEGATIVE_EFFECTS_GROUP.value();
    }
}
