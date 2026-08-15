package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Objects;

public final class EntityInteractionType<T extends EntityInteraction> {

    public static final String LOCALIZATION_PREFIX = "entity_interaction";
    public static final Identifier SHARED_ERROR_IDENTIFIER = MedicalSystem.createIdentifier("shared");
    private final Identifier identifier;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
    private final InteractionFactory<T> interactionFactory;
    private final Component displayName;

    private EntityInteractionType(Builder<T> builder) {
        this.identifier = builder.identifier;
        this.codec = builder.mapCodec;
        this.streamCodec = builder.streamCodec;
        this.interactionFactory = builder.interactionFactory;
        this.displayName = getDisplayName(identifier);
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public MapCodec<T> codec() {
        return this.codec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    public T createNewInteractionInstance(Player player, LivingEntity target) {
        return this.interactionFactory.createNewInteractionInstance(player, target);
    }

    public Component getValidationErrorMessage(String errorCode, Object... args) {
        return getErrorMessage(this.identifier, errorCode, args);
    }

    public Component getDisplayName() {
        return displayName;
    }

    public static Component getDisplayName(Identifier identifier) {
        return Component.translatable(identifier.toLanguageKey(LOCALIZATION_PREFIX));
    }

    public static Component getErrorMessage(Identifier identifier, String errorCode, Object... args) {
        String key = identifier.toLanguageKey(LOCALIZATION_PREFIX, "error." + errorCode);
        return Component.translatable(key, args);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EntityInteractionType<?> that)) return false;
        return Objects.equals(this.identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.identifier);
    }

    @Override
    public @NotNull String toString() {
        return this.identifier.toString();
    }

    @FunctionalInterface
    public interface InteractionFactory<T extends EntityInteraction> {
        T createNewInteractionInstance(Player player, LivingEntity target);
    }

    public static final class Builder<T extends EntityInteraction> {

        private final Identifier identifier;
        private MapCodec<T> mapCodec;
        private StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
        private InteractionFactory<T> interactionFactory;

        private Builder(Identifier identifier) {
            this.identifier = identifier;
        }

        public static <T extends EntityInteraction> Builder<T> create(Identifier identifier) {
            return new Builder<>(identifier);
        }

        public static <T extends EntityInteraction> Builder<T> createSingleton(Identifier identifier, T instance) {
            return Builder.<T>create(identifier)
                    .factory((_, _) -> instance);
        }

        public Builder<T> serialize(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
            this.mapCodec = codec;
            this.streamCodec = streamCodec;
            return this;
        }

        public Builder<T> factory(InteractionFactory<T> interactionFactory) {
            this.interactionFactory = interactionFactory;
            return this;
        }

        public EntityInteractionType<T> build() {
            Objects.requireNonNull(this.identifier, "ID is required");
            Objects.requireNonNull(this.mapCodec, "Map codec is required");
            Objects.requireNonNull(this.streamCodec, "Stream codec is required");
            Objects.requireNonNull(this.interactionFactory, "Interaction factory is required");
            return new EntityInteractionType<>(this);
        }
    }
}
