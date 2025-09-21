package tnt.tarkovcraft.medsystem.common.effect.group;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface EffectGroupItem {

    void init(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb);

    void apply(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb);

    void cleanup(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb);

    void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip);

    default boolean isVisible() {
        return true;
    }

    EffectGroupItem copy();

    EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other);

    EffectGroupItemType<?> getType();
}
