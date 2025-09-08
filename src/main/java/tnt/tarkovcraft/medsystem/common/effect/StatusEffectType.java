package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
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
    private final Set<BodyPartGroup> ignoredBodyParts;
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
        this.ignoredBodyParts = builder.bodyPartGroups;
        this.isGlobalEffect = builder.globalEffect;
        this.icon = this.identifier.withPath(path -> "textures/icons/status_effect/" + path + ".png");
        this.displayName = Component.translatable(this.identifier.toLanguageKey("status_effect"));
    }

    public static <S extends StatusEffect> Builder<S> builder(ResourceLocation identifier, Factory<S> factory) {
        return new Builder<>(identifier, factory);
    }

    public boolean is(TagKey<StatusEffectType<?>> tag) {
        return this.intrusiveHolder.is(tag);
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

    public S merge(S a, S b) {
        return this.merger.apply(a, b);
    }

    public boolean isIgnoredBodyPart(BodyPartGroup group) {
        return this.ignoredBodyParts.contains(group);
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

    public static final class Builder<S extends StatusEffect> {

        private final ResourceLocation identifier;
        private final Factory<S> factory;
        private final Set<BodyPartGroup> bodyPartGroups = EnumSet.noneOf(BodyPartGroup.class);
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

        public Builder<S> ignoresBodyParts(BodyPartGroup... groups) {
            this.bodyPartGroups.addAll(Arrays.asList(groups));
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
