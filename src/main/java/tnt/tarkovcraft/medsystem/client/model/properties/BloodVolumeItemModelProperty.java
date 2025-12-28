package tnt.tarkovcraft.medsystem.client.model.properties;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.status.BloodContainer;

public record BloodVolumeItemModelProperty() implements RangeSelectItemModelProperty {

    public static final MapCodec<BloodVolumeItemModelProperty> CODEC = MapCodec.unit(new BloodVolumeItemModelProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        BloodContainer container = stack.get(MedSystemItemComponents.BLOOD_CONTAINER);
        if (container == null)
            return 0.0F;
        float volume = container.value();
        float capacity = container.capacity();
        return volume / capacity;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}
