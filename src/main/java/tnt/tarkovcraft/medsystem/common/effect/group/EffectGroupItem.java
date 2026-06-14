package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EffectGroupItem {

    Codec<EffectGroupItem> CODEC = MedSystemRegistries.EFFECT_GROUP_ITEM.byNameCodec()
            .dispatch(EffectGroupItem::codec, Function.identity());

    void init(EffectGroupHolder holder, StatusEffectContext context);

    void apply(EffectGroupHolder holder, StatusEffectContext context);

    void cleanup(EffectGroupHolder holder, StatusEffectContext context);

    void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip);

    default boolean visible() {
        return true;
    }

    EffectGroupItem copy();

    EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other);

    MapCodec<? extends EffectGroupItem> codec();
}
