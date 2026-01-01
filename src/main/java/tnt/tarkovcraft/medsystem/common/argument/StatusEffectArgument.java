package tnt.tarkovcraft.medsystem.common.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class StatusEffectArgument implements ArgumentType<StatusEffect> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "status_effect{key:value}");
    private static final DynamicCommandExceptionType UNKNOWN_STATUS_EFFECT = new DynamicCommandExceptionType(
            key -> Component.literal("Unknown status effect: " + key)
    );
    private static final DynamicCommandExceptionType INVALID_STATUS_EFFECT = new DynamicCommandExceptionType(
            error -> Component.literal("Invalid status effect: " + error)
    );

    private final HolderLookup.Provider registries;

    public StatusEffectArgument(CommandBuildContext context) {
        this.registries = context;
    }

    public static StatusEffectArgument statusEffect(CommandBuildContext context) {
        return new StatusEffectArgument(context);
    }

    public static StatusEffect getStatusEffect(CommandContext<?> context, String name) {
        return context.getArgument(name, StatusEffect.class);
    }

    @Override
    public StatusEffect parse(StringReader reader) throws CommandSyntaxException {
        StatusEffectType<?> effectType = readStatusEffectType(reader, this.registries.lookupOrThrow(MedSystemRegistries.Keys.STATUS_EFFECT));
        return parseStatusEffect(reader, effectType, this.registries);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        HolderLookup.RegistryLookup<StatusEffectType<?>> registrylookup = this.registries.lookupOrThrow(MedSystemRegistries.Keys.STATUS_EFFECT);
        return SharedSuggestionProvider.suggestResource(registrylookup.listElementIds().map(ResourceKey::location), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static <E extends StatusEffect> StatusEffect parseStatusEffect(StringReader reader, StatusEffectType<E> type, HolderLookup.Provider registries) throws CommandSyntaxException {
        CompoundTag tag;
        if (reader.canRead() && reader.peek() == '{') {
            tag = new TagParser(reader).readStruct();
        } else {
            tag = new CompoundTag();
        }
        DynamicOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return type.mapCodec().codec().parse(ops, tag)
                .getOrThrow(INVALID_STATUS_EFFECT::create);
    }

    private static StatusEffectType<?> readStatusEffectType(StringReader reader, HolderLookup<StatusEffectType<?>> lookup) throws CommandSyntaxException {
        ResourceLocation identifier = ResourceLocation.read(reader);
        ResourceKey<StatusEffectType<?>> key = ResourceKey.create(MedSystemRegistries.Keys.STATUS_EFFECT, identifier);
        return lookup.get(key)
                .orElseThrow(() -> UNKNOWN_STATUS_EFFECT.createWithContext(reader, identifier))
                .value();
    }
}
