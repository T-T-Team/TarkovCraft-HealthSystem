package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousState;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.NO_BLOOD_DATA_FOUND;

public final class ReviveSubCommand {

    private static final SimpleCommandExceptionType NOT_REVIVABLE = new SimpleCommandExceptionType(Component.literal("Given entity cannot be revived"));

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("revive")
                .then(
                        Commands.argument("target", EntityArgument.entity())
                                .executes(ReviveSubCommand::reviveEntity)
                );
    }

    private static int reviveEntity(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        if (!(entity instanceof LivingEntity livingEntity) || !BloodSystemManager.isEnabled(livingEntity)) {
            throw NO_BLOOD_DATA_FOUND.create();
        }
        if (!BloodSystemManager.isUnconscious(livingEntity)) {
            throw NOT_REVIVABLE.create();
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
        UnconsciousState unconsciousState = bloodSystem.getUnconsciousState();
        UnconsciousOptions options = unconsciousState.getUnconsciousOptions();
        if (!options.allowRescue()) {
            throw NOT_REVIVABLE.create();
        }
        bloodSystem.rescueDownedEntity(livingEntity);
        bloodSystem.synchronizeImmediately(livingEntity);
        return 0;
    }
}
