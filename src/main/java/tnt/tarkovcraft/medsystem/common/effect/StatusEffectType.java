package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;
import java.util.function.BinaryOperator;

public final class StatusEffectType<S extends StatusEffect> {

    public static final Codec<StatusEffect> CODEC = MedSystemRegistries.STATUS_EFFECT.byNameCodec().dispatch(StatusEffect::getType, t -> t.codec);

    private final ResourceLocation identifier;
    private final Factory<S> factory;
    private final MapCodec<S> codec;
    private final BinaryOperator<S> merger;
    private final EffectType effectType;
    private final EffectVisibility visibility;
    private final boolean isGlobalEffect;
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
        this.icon = this.identifier.withPath(path -> "textures/icons/status_effect/" + path + ".png");
        this.displayName = Component.translatable(this.identifier.toLanguageKey("status_effect"));
    }

    public static <S extends StatusEffect> Builder<S> builder(ResourceLocation identifier, Factory<S> factory) {
        return new Builder<>(identifier, factory);
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public EffectVisibility getVisibility() {
        return visibility;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public S createDelayedEffect(int duration, int delay) {
        return this.factory.createNew(duration, delay);
    }

    public S createDelayedEffect(TickValue duration, int delay) {
        return this.createDelayedEffect(duration.tickValue(), delay);
    }

    public S createDelayedEffect(int duration, TickValue delay) {
        return this.createDelayedEffect(duration, delay.tickValue());
    }

    public S createDelayedEffect(TickValue duration, TickValue delay) {
        return this.createDelayedEffect(duration.tickValue(), delay.tickValue());
    }

    public S createImmediateEffect(int duration) {
        return this.createDelayedEffect(duration, 0);
    }

    public S createImmediateEffect(TickValue duration) {
        return this.createImmediateEffect(duration.tickValue());
    }

    public S createImmediateEffect() {
        return this.createImmediateEffect(Duration.minutes(1));
    }

    public boolean isGlobalEffect() {
        return this.isGlobalEffect;
    }

    public S merge(S a, S b) {
        return this.merger.apply(a, b);
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

    public static final class Builder<S extends StatusEffect> {

        private final ResourceLocation identifier;
        private final Factory<S> factory;
        private MapCodec<S> codec;
        private EffectType effectType = EffectType.NEUTRAL;
        private EffectVisibility visibility = EffectVisibility.ALWAYS;
        private BinaryOperator<S> merger = StatusEffect::merge;
        private boolean globalEffect;

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
        S createNew(int duration, int delay);
    }
}
