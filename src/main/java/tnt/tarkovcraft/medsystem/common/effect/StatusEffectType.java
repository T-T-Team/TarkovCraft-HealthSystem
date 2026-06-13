package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BinaryOperator;

public final class StatusEffectType<S extends StatusEffect> {

    public static final Codec<StatusEffect> CODEC = MedSystemRegistries.STATUS_EFFECT.byNameCodec().dispatch(StatusEffect::getType, t -> t.codec);

    private final Holder<StatusEffectType<?>> intrusiveHolder = MedSystemRegistries.STATUS_EFFECT.createIntrusiveHolder(this);
    private final ResourceLocation identifier;
    private final Factory<S> factory;
    private final MapCodec<S> codec;
    private final BinaryOperator<S> merger;
    private final EffectType effectType;
    private final EffectVisibility visibility;
    private final boolean isGlobalEffect;
    private final boolean isSpecial;
    private final int healingPriority;
    private final ResourceLocation icon;
    private final Component displayName;

    private StatusEffectType(Builder<S> builder) {
        this.identifier = builder.identifier;
        this.factory = builder.factory;
        this.codec = builder.codec;
        this.merger = builder.merger;
        this.effectType = builder.effectType;
        this.visibility = builder.visibility;
        this.isGlobalEffect = builder.globalEffect;
        this.isSpecial = builder.special;
        this.healingPriority = builder.healingPriority;
        this.icon = StatusEffectHelper.getTextureResource(this.identifier);
        this.displayName = Component.translatable(this.identifier.toLanguageKey("status_effect"));
    }

    public static <S extends StatusEffect> Builder<S> builder(ResourceLocation identifier, Factory<S> factory) {
        return new Builder<>(identifier, factory);
    }

    public boolean is(TagKey<StatusEffectType<?>> tag) {
        return this.intrusiveHolder.is(tag);
    }

    public boolean is(HolderSet<StatusEffectType<?>> holderSet) {
        return holderSet.contains(this.intrusiveHolder);
    }

    public boolean isDisabled() {
        return this.is(MedSystemTags.StatusEffects.DISABLED);
    }

    public ResourceLocation getIcon(@Nullable StatusEffect instance) {
        return instance != null && instance.getCustomIcon() != null ? instance.getCustomIcon() : this.icon;
    }

    public ResourceLocation getIcon() {
        return this.getIcon(null);
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public EffectType getContextualEffectType(StatusEffect effect) {
        EffectType effectType = effect.getEffectType();
        return effectType != null ? effectType : this.getEffectType();
    }

    public EffectVisibility getVisibility() {
        return visibility;
    }

    public Component getDisplayName() {
        return this.getDisplayName(null);
    }

    public Component getDisplayName(@Nullable StatusEffect instance) {
        Component customDisplayName = instance != null ? instance.getCustomDisplayName() : null;
        return customDisplayName != null ? customDisplayName : this.displayName;
    }

    public S createEffect(int duration) {
        return this.factory.createNew(duration);
    }

    public S createEffect(TickValue duration) {
        return this.createEffect(duration.tickValue());
    }

    public S createEffect() {
        return this.createEffect(Duration.minutes(1));
    }

    public boolean isGlobalEffect() {
        return this.isGlobalEffect;
    }

    public S merge(S currentEffect, S newEffect) {
        S result = this.merger.apply(currentEffect, newEffect);
        MedicalSystem.LOGGER.debug(StatusEffectHelper.MARKER, "Merging incoming {} status effect with existing one, new duration is {}", this, result.getDuration());
        return result;
    }

    public boolean isSpecialStatusEffect() {
        return this.isSpecial;
    }

    public int getHealingPriority(StatusEffect effect) {
        Integer priority = effect.getCustomHealingPriority();
        return priority != null ? priority : this.healingPriority;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public static boolean isVisible(StatusEffect effect, EffectVisibility ctx) {
        return effect.isVisible() && effect.getType().getVisibility().isVisibleInMode(ctx);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }

    public MapCodec<S> mapCodec() {
        return codec;
    }

    public static final class Builder<S extends StatusEffect> {

        private final ResourceLocation identifier;
        private final Factory<S> factory;
        private MapCodec<S> codec;
        private EffectType effectType = EffectType.NEUTRAL;
        private EffectVisibility visibility = EffectVisibility.ALWAYS;
        private BinaryOperator<S> merger = StatusEffect::merge;
        private boolean globalEffect;
        private boolean special;
        private int healingPriority = 0;

        private Builder(ResourceLocation identifier, Factory<S> factory) {
            this.identifier = identifier;
            this.factory = factory;
        }

        public Builder<S> persist(MapCodec<S> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<S> type(EffectType type) {
            this.effectType = type;
            return this;
        }

        public Builder<S> visibility(EffectVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder<S> setGlobal() {
            this.globalEffect = true;
            return this;
        }

        public Builder<S> combineEffects(BinaryOperator<S> merger) {
            this.merger = merger;
            return this;
        }

        public Builder<S> setSpecial() {
            this.special = true;
            return this;
        }

        public Builder<S> healPriority(int healingPriority) {
            this.healingPriority = healingPriority;
            return this;
        }

        public StatusEffectType<S> build() {
            Objects.requireNonNull(this.identifier, "Identifier is required");
            Objects.requireNonNull(this.factory, "Instance factory is required");
            Objects.requireNonNull(this.codec, "Codec is required");
            Objects.requireNonNull(this.merger, "Merge function is required");
            Objects.requireNonNull(this.effectType, "Effect type is required");
            Objects.requireNonNull(this.visibility, "Effect visibility is required");
            return new StatusEffectType<>(this);
        }
    }

    @FunctionalInterface
    public interface Factory<S extends StatusEffect> {
        S createNew(int duration);
    }
}
