package tnt.tarkovcraft.medsystem.common.effect.group;

import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.util.context.Context;

import java.util.function.Consumer;

public interface EffectGroupItem {

    void init(Context context);

    void apply(Context context);

    void cleanup(Context context);

    void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip);

    EffectGroupItem copy();

    EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other);

    EffectGroupItemType<?> getType();
}
