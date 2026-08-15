package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteraction;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionData;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionType;

public final class InteractionSubCommand {

    private static final SimpleCommandExceptionType INTERACTION_ALREADY_ACTIVE = new SimpleCommandExceptionType(Component.literal("Entity has already active interaction"));
    private static final SimpleCommandExceptionType NO_INTERACTION = new SimpleCommandExceptionType(Component.literal("Entity has no interaction active"));
    private static final DynamicCommandExceptionType INTERACTION_FAILED = new DynamicCommandExceptionType(arg -> Component.literal("Interaction failed: " + ((Component) arg).getString()));
    private static final SimpleCommandExceptionType INTERACTION_UPDATE_FAILED = new SimpleCommandExceptionType(Component.literal("Interaction update failed"));

    public static ArgumentBuilder<CommandSourceStack, ?> node(CommandBuildContext context) {
        return Commands.literal("interaction")
                .then(
                        Commands.argument("target", EntityArgument.player())
                                .then(
                                        Commands.literal("get")
                                                .executes(InteractionSubCommand::getActiveInteraction)
                                )
                                .then(
                                    Commands.argument("interaction_id", ResourceArgument.resource(context, MedSystemRegistries.Keys.ENTITY_INTERACTION))
                                            .then(
                                                    Commands.literal("start")
                                                            .executes(InteractionSubCommand::startInteraction)
                                            )
                                            .then(
                                                    Commands.literal("finish")
                                                            .executes(InteractionSubCommand::finishInteraction)
                                            )
                                            .then(
                                                    Commands.literal("cancel")
                                                            .executes(InteractionSubCommand::cancelInteraction)
                                            )
                                )
                );
    }

    private static int startInteraction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "target");
        EntityInteractionData interactionManager = EntityInteractionData.getInteractionData(player);
        if (interactionManager.isAnyInteractionActive()) {
            throw INTERACTION_ALREADY_ACTIVE.create();
        }
        Holder.Reference<EntityInteractionType<?>> reference = ResourceArgument.getResource(ctx, "interaction_id", MedSystemRegistries.Keys.ENTITY_INTERACTION);
        EntityInteraction interaction = reference.value().createNewInteractionInstance(player, player);
        UserActionResult<Void> initiateResult = interaction.checkAvailability(player, player);
        if (initiateResult.isFailure()) {
            throw INTERACTION_FAILED.create(initiateResult.message());
        }
        long initiationTime = player.level().getGameTime();
        if (!interactionManager.startInteraction(reference.value(), player, player, initiationTime)) {
            throw INTERACTION_UPDATE_FAILED.create();
        }
        interactionManager.sync(player);
        return 0;
    }

    private static int finishInteraction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "target");
        EntityInteractionData interactionManager = EntityInteractionData.getInteractionData(player);
        if (!interactionManager.isAnyInteractionActive()) {
            throw NO_INTERACTION.create();
        }
        long initiationTime = player.level().getGameTime();
        if (!interactionManager.finishInteraction(player, player, true)) {
            throw INTERACTION_UPDATE_FAILED.create();
        }
        interactionManager.sync(player);
        return 0;
    }

    private static int cancelInteraction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "target");
        EntityInteractionData interactionManager = EntityInteractionData.getInteractionData(player);
        if (!interactionManager.isAnyInteractionActive()) {
            throw NO_INTERACTION.create();
        }
        if (!interactionManager.cancelInteraction(player, player)) {
            throw INTERACTION_UPDATE_FAILED.create();
        }
        interactionManager.sync(player);
        return 0;
    }

    private static int getActiveInteraction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "target");
        EntityInteractionData interactionManager = EntityInteractionData.getInteractionData(player);
        EntityInteraction interaction = interactionManager.getActiveInteraction();
        if (interaction != null) {
            long gameTime = player.level().getGameTime();
            EntityInteractionType<?> type = interaction.type();
            boolean expired = interactionManager.isInteractionExpired(gameTime);
            boolean ready = interactionManager.isInteractionReady(gameTime);

            CommandSourceStack source = ctx.getSource();
            source.sendSystemMessage(Component.literal("Active interaction:"));
            source.sendSystemMessage(type.getDisplayName());
            source.sendSystemMessage(Component.literal("Ready: " + ready));
            source.sendSystemMessage(Component.literal("Expired: " + expired));
        } else {
            ctx.getSource().sendSystemMessage(Component.literal("Entity has no active interaction"));
        }
        return 0;
    }
}
