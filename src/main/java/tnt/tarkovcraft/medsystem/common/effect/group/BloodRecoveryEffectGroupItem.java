package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public record BloodRecoveryEffectGroupItem(float amount) implements EffectGroupItem {

    public static final MapCodec<BloodRecoveryEffectGroupItem> CODEC = Codec.FLOAT
            .xmap(BloodRecoveryEffectGroupItem::new, BloodRecoveryEffectGroupItem::amount).fieldOf("amount");

    @Override
    public void init(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void cleanup(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void apply(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        Level level = entity.level();
        long time = level.getGameTime();
        if (time % 20L != 0L) {
            return;
        }
        if (BloodSystemManager.isEnabled(entity)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            bloodSystem.recoverBlood(this.amount);
        }
    }

    @Override
    public void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip) {
        EffectType type = this.amount > 0 ? EffectType.POSITIVE : EffectType.NEGATIVE;
        MutableComponent label = Component.translatable("label.medsystem." + (this.amount > 0 ? "blood_recovery" : "blood_loss"), Component.literal(String.format(Locale.ROOT, "%.1f", this.amount * 1000.0F)));
        if (isItemTooltip) {
            tooltip.accept(SideEffect.createDescriptionComponent(type, label, 1.0F, holder.getDuration(), holder.getDelay()));
        } else {
            tooltip.accept(label.append(" ").append(StatusEffect.getDurationLabel(holder.getDuration())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean visible() {
        return this.amount != 0.0F;
    }

    @Override
    public EffectGroupItem copy() {
        return new BloodRecoveryEffectGroupItem(this.amount);
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other) {
        return null;
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.BLOOD_RECOVERY.value();
    }
}
