package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.LimbConfiguration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static tnt.tarkovcraft.core.common.command.CoreTarkovcraftCommand.ROOT_NODE;
import static tnt.tarkovcraft.core.common.command.CoreTarkovcraftCommand.gameMasterOnly;

public final class MedSystemTarkovCraftCommand {

    static final SimpleCommandExceptionType NO_VALID_TARGET_FOUND = new SimpleCommandExceptionType(Component.literal("No health containers found for given entity selector"));
    static final SimpleCommandExceptionType NO_BLOOD_DATA_FOUND = new SimpleCommandExceptionType(Component.literal("No blood data found for given entity"));
    static final SimpleCommandExceptionType UNCONSCIOUS_MODE_DISABLED = new SimpleCommandExceptionType(Component.literal("Unconscious mode is not allowed for given entity"));

    public static void create(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal(ROOT_NODE)
                        .then(
                                // tarkovcraft effect ...
                                StatusEffectSubCommand.node(context)
                                        .requires(gameMasterOnly())
                        )
                        .then(
                                // tarkovcraft hurt ...
                                HurtSubCommand.node(context)
                                        .requires(gameMasterOnly())
                        )
                        .then(
                                // tarkovcraft heal ...
                                HealSubCommand.node()
                                        .requires(gameMasterOnly())
                        )
                        .then(
                                // tarkovcraft blood ...
                                BloodSubCommand.node()
                                        .requires(gameMasterOnly())
                        )
                        .then(
                                // tarkovcraft unconscious ...
                                SetUnconsciousSubCommand.node()
                                        .requires(gameMasterOnly())
                        )
                        .then(
                                // tarkovcraft revive ...
                                ReviveSubCommand.node()
                                        .requires(gameMasterOnly())
                        )
        );
    }

    static CompletableFuture<Suggestions> suggestAllEntityLimbs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(ctx, "target");
        Set<String> suggestions = new HashSet<>();
        for (Entity entity : entities) {
            if (!HealthSystem.hasCustomHealth(entity))
                continue;
            HealthContainer container = HealthContainer.getAttached((LivingEntity) entity);
            HealthContainerDefinition definition = container.getDefinition();
            LimbConfiguration configuration = definition.limbConfiguration();
            suggestions.addAll(configuration.getLimbCodes());
        }
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
