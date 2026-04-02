package tnt.tarkovcraft.medsystem.common.effect.util;

import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.util.Lazy;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListStatusEffectSubmitter implements StatusEffectSubmitter {

    private final Lazy<List<StatusEffectWithDelay>> effects;

    ListStatusEffectSubmitter(List<StatusEffectWithDelay> effects) {
        this.effects = Lazy.of(effects);
    }

    ListStatusEffectSubmitter() {
        this.effects = Lazy.of(ArrayList::new);
    }

    @Override
    public void submit(int delay, StatusEffect template) {
        this.effects.get().add(new StatusEffectWithDelay(ConstantNumberProvider.of(delay), template));
    }

    public void forEach(Consumer<StatusEffectWithDelay> consumer) {
        this.effects.ifPresent(effects -> effects.forEach(consumer));
    }
}
