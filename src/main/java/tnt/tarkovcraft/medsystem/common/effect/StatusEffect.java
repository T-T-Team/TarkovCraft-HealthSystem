package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.data.duration.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class StatusEffect {

    public static final Component PAINFUL_LABEL = Component.translatable("label.medsystem.painful").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY);
    public static final DurationFormatSettings DURATION_SETTINGS = new DurationFormatSettings();
    public static final int INFINITE_DURATION = -1;

    private int duration;

    public StatusEffect(int duration) {
        this.duration = duration;
    }

    public abstract StatusEffectType<?> getType();

    public abstract void apply(StatusEffectContext context);

    public abstract void onRemoved(StatusEffectContext context);

    public abstract StatusEffect copy();

    public void setCausingEntity(@Nullable UUID owner) {}

    public @Nullable UUID getCausingEntity() {
        return null;
    }

    public void addAdditionalInfo(Consumer<Component> tooltip) {}

    /**
     * Allows to override SideEffect tooltips
     * @return whether custom tooltip should be used
     * @see StatusEffect#addCustomTooltip(Consumer)
     */
    public boolean hasCustomTooltip() {
        return false;
    }

    public boolean hasVisibleDuration() {
        return true;
    }

    /**
     * Adds custom tooltip for SideEffect descriptions
     *
     * @param tooltip Tooltip registration
     */
    public void addCustomTooltip(Consumer<Component> tooltip) {

    }

    public boolean isVisible() {
        return true;
    }

    public final Optional<Entity> getCausingEntity(ServerLevel level) {
        UUID owner = this.getCausingEntity();
        if (owner != null) {
            return Optional.ofNullable(level.getEntity(owner));
        }
        return Optional.empty();
    }

    public final void markForRemoval() {
        this.setDuration(1);
    }

    public final int getDuration() {
        return this.duration;
    }

    public final void setDuration(int duration) {
        this.duration = duration;
    }

    public final StatusEffect setInfinite() {
        this.setDuration(INFINITE_DURATION);
        return this;
    }

    public final void addDuration(int duration) {
        this.setDuration(this.getDuration() + duration);
    }

    public final void addDuration(TickValue duration) {
        this.setDuration(this.getDuration() + duration.tickValue());
    }

    public final boolean isInfinite() {
        return this.getDuration() < 0;
    }

    public Component getCustomDisplayName() {
        return null;
    }

    public Identifier getCustomIcon() {
        return null;
    }

    protected @Nullable Integer getCustomHealingPriority() {
        return null;
    }

    public static <T extends StatusEffect> Products.P1<RecordCodecBuilder.Mu<T>, Integer> common(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.INT.optionalFieldOf("duration", 600).forGetter(StatusEffect::getDuration)
        );
    }

    public static <S extends StatusEffect> S merge(S a, S b) {
        if (a.isInfinite() || b.isInfinite()) {
            a.setDuration(INFINITE_DURATION);
        } else {
            int duration = a.getDuration();
            a.setDuration(duration + b.getDuration());
        }
        return a;
    }

    public static <S extends StatusEffect> S keep(S a, S b) {
        return a;
    }

    public static <S extends StatusEffect> S replace(S a, S b) {
        return b;
    }

    public static <S extends StatusEffect> S maxDuration(S a, S b) {
        return maxDuration(a, b, duration -> {
            a.setDuration(duration);
            return a;
        });
    }

    public static <S extends StatusEffect> S maxDuration(S a, S b, IntFunction<S> effect) {
        if (a.isInfinite())
            return a;
        if (b.isInfinite())
            return b;
        int duration = Math.max(a.getDuration(), b.getDuration());
        return effect.apply(duration);
    }

    public static Component getDurationLabel(int duration) {
        return getDurationLabel(duration, ChatFormatting.DARK_GRAY);
    }

    public static Component getDurationLabel(int duration, ChatFormatting color) {
        return Duration.format(duration, DURATION_SETTINGS, DurationFormats.TIME).copy().withStyle(color);
    }

    public static int sumEffectDurations(StatusEffect effect1, StatusEffect effect2) {
        if (effect1.isInfinite() || effect2.isInfinite()) {
            return INFINITE_DURATION;
        }
        return effect1.getDuration() + effect2.getDuration();
    }

    static {
        DURATION_SETTINGS.setUnits(Arrays.asList(DurationUnit.HOURS, DurationUnit.MINUTES, DurationUnit.SECONDS));
        DURATION_SETTINGS.setIncludeZeroValues(true);
    }
}
