package tnt.tarkovcraft.medsystem.common.effect.util;

import cpw.mods.util.Lazy;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListStatusEffectSubmitter implements StatusEffectSubmitter {

    private final Lazy<List<PostEffect>> effects;

    ListStatusEffectSubmitter(List<PostEffect> effects) {
        this.effects = Lazy.of(effects);
    }

    ListStatusEffectSubmitter() {
        this.effects = Lazy.of(ArrayList::new);
    }

    @Override
    public void submit(int delay, StatusEffect template) {
        this.effects.get().add(new PostEffect(delay, template));
    }

    public void forEach(Consumer<PostEffect> consumer) {
        this.effects.ifPresent(effects -> effects.forEach(consumer));
    }
}
