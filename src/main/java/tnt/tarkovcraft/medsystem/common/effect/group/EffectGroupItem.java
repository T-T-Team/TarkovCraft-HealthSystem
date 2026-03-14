package tnt.tarkovcraft.medsystem.common.effect.group;

import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;

import java.util.function.Consumer;

public interface EffectGroupItem {

    void init(EffectGroupHolder holder, StatusEffectContext context);

    void apply(EffectGroupHolder holder, StatusEffectContext context);

    void cleanup(EffectGroupHolder holder, StatusEffectContext context);

    void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip);

    default boolean visible() {
        return true;
    }

    EffectGroupItem copy();

    EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other);

    EffectGroupItemType<?> getType();
}
