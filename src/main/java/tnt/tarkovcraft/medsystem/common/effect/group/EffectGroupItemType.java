package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record EffectGroupItemType<T extends EffectGroupItem>(Identifier identifier, MapCodec<T> codec) {

    public static final Codec<EffectGroupItem> CODEC = MedSystemRegistries.EFFECT_GROUP_ITEM.byNameCodec().dispatch(EffectGroupItem::getType, EffectGroupItemType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EffectGroupItemType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
