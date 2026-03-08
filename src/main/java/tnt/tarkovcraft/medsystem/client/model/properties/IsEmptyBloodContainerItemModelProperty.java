package tnt.tarkovcraft.medsystem.client.model.properties;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;

public record IsEmptyBloodContainerItemModelProperty() implements ConditionalItemModelProperty {

    public static final MapCodec<IsEmptyBloodContainerItemModelProperty> CODEC = MapCodec.unit(new IsEmptyBloodContainerItemModelProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        BloodContainer container = stack.get(MedSystemItemComponents.BLOOD_CONTAINER);
        return container == null || container.isEmpty();
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return CODEC;
    }
}
