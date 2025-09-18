package tnt.tarkovcraft.medsystem.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public final class GiveUpCommand {

    public static final SimpleCommandExceptionType NOT_UNCONSCIOUS = new SimpleCommandExceptionType(Component.translatable("commands.giveup.failed.not_unconscious"));

    public static void create(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("giveUp")
                        .executes(GiveUpCommand::giveUp)
        );
    }

    private static int giveUp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        Player player = stack.getPlayerOrException();
        if (!BloodSystem.isEntityUnconscious(player)) {
            throw NOT_UNCONSCIOUS.create();
        }
        BloodSystem.causeBloodLoss(player, Float.MAX_VALUE);
        return 0;
    }
}
