package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NeutralEffectsGroup extends GroupStatusEffect {

    public static final MapCodec<NeutralEffectsGroup> CODEC = RecordCodecBuilder.mapCodec(instance -> commonGroup(instance).apply(instance, NeutralEffectsGroup::new));

    public NeutralEffectsGroup(List<EffectGroupHolder> items) {
        super(items);
    }

    public NeutralEffectsGroup(int duration, int delay, List<EffectGroupHolder> items) {
        super(duration, delay, items);
    }

    public static NeutralEffectsGroup createTemplate(Consumer<EffectGroupHolder.Factory> tplBuilder) {
        List<EffectGroupHolder> items = new ArrayList<>();
        tplBuilder.accept(EffectGroupHolder.getFactory(items));
        return new NeutralEffectsGroup(items);
    }

    @Override
    public GroupStatusEffect newInstance(List<EffectGroupHolder> copiedItems) {
        return new NeutralEffectsGroup(copiedItems);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.NEUTRAL_EFFECTS_GROUP.value();
    }
}
