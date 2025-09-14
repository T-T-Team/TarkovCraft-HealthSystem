package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.effect.EffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import java.util.List;
import java.util.function.Consumer;

public class DeadLimbRecoveryEffectGroupItem implements EffectGroupItem {

    public static final MapCodec<DeadLimbRecoveryEffectGroupItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("health", 1.0F).forGetter(t -> t.health)
    ).apply(instance, DeadLimbRecoveryEffectGroupItem::new));

    private final float health;

    public DeadLimbRecoveryEffectGroupItem(float health) {
        this.health = health;
    }

    @Override
    public void init(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        List<BodyPart> deadLimbs = container.getBodyPartStream()
                .filter(BodyPart::isDead)
                .toList();
        for (BodyPart part : deadLimbs) {
            part.setHealth(this.health);
        }
        container.updateHealth(entity);
        HealthSystem.synchronizeEntity(entity);
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public void cleanup(Context context) {
    }

    @Override
    public void addInformation(EffectGroupHolder effectGroupHolder, Consumer<Component> consumer, boolean isItemTooltip) {
        if (!isItemTooltip)
            return;
        consumer.accept(SideEffect.createDescriptionComponent(EffectType.POSITIVE, Component.translatable("label.medsystem.limb_recovery"), 1.0F, 0, effectGroupHolder.getDelay()));
    }

    @Override
    public EffectGroupItem copy() {
        return new DeadLimbRecoveryEffectGroupItem(this.health);
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder effectGroupHolder, EffectGroupHolder effectGroupHolder1) {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.DEAD_LIMB_RECOVERY.value();
    }
}
