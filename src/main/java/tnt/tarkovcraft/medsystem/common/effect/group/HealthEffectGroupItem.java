package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.effect.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import java.util.Locale;
import java.util.function.Consumer;

public class HealthEffectGroupItem implements EffectGroupItem {

    public static final MapCodec<HealthEffectGroupItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(t -> t.amount),
            Codec.INT.fieldOf("interval").forGetter(t -> t.interval)
    ).apply(instance, HealthEffectGroupItem::new));

    private final float amount;
    private final int interval;

    public HealthEffectGroupItem(float amount) {
        this(amount, 20);
    }

    public HealthEffectGroupItem(float amount, int interval) {
        this.amount = amount;
        this.interval = interval;
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        Level level = entity.level();
        if (level.isClientSide())
            return;
        long time = level.getGameTime();
        if (time % this.interval != 0)
            return;
        if (!entity.isAlive())
            return;
        if (this.amount >= 0.0F) {
            entity.heal(this.amount);
        } else {
            Holder<DamageType> toxinHolder = MedSystemDamageTypes.of(level.registryAccess(), MedSystemDamageTypes.TOXIC_SIDE_EFFECT);
            DamageSource damageSource = new DamageSource(toxinHolder);
            entity.hurtServer((ServerLevel) level, damageSource, Mth.abs(this.amount));
        }
    }

    @Override
    public void cleanup(Context context) {
    }

    @Override
    public void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip) {
        float perSecond = 20.0F / this.interval;
        MutableComponent value = Component.translatable((this.amount >= 0 ? "label.medsystem.health_recovery" : "label.medsystem.health_loss"), Component.literal(String.format(Locale.ROOT, "%.1f", this.amount * perSecond)));
        if (isItemTooltip) {
            // > [health recovery/loss per second] / Dur.: <duration> / Del.: <delay>
            tooltip.accept(SideEffect.createDescriptionComponent(amount > 0 ? EffectType.POSITIVE : amount < 0 ? EffectType.NEGATIVE : EffectType.NEUTRAL, value, 1.0F, holder.getDuration(), holder.getDelay()));
        } else {
            // [health recovery/loss per second] <duration>
            tooltip.accept(value.append(" ").append(StatusEffect.getDurationLabel(holder.getDuration())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other) {
        return null;
    }

    @Override
    public EffectGroupItem copy() {
        return new HealthEffectGroupItem(this.amount, this.interval);
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.HEALTH.value();
    }
}
