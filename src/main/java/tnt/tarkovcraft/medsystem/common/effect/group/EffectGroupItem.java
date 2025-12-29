package tnt.tarkovcraft.medsystem.common.effect.group;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface EffectGroupItem {

    void init(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb);

    void apply(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb);

    void cleanup(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb);

    void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip);

    default boolean visible() {
        return true;
    }

    EffectGroupItem copy();

    EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other);

    EffectGroupItemType<?> getType();
}
