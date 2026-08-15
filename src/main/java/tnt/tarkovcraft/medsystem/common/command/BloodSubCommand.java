package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

import java.util.Locale;

import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.NO_BLOOD_DATA_FOUND;

public final class BloodSubCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("blood")
                .then(
                        Commands.argument("target", EntityArgument.entity())
                                .executes(BloodSubCommand::getBloodInfo)
                                .then(
                                        Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                                                .executes(BloodSubCommand::setBloodVolume)
                                )
                );
    }

    private static int getBloodInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        if (!(entity instanceof LivingEntity livingEntity) || !BloodSystemManager.isEnabled(livingEntity)) {
            throw NO_BLOOD_DATA_FOUND.create();
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        CommandSourceStack source = ctx.getSource();
        source.sendSystemMessage(Component.literal(entity.getDisplayName().getString() + " blood: " + String.format(Locale.ROOT, "%.4f/%.2fL", bloodSystem.getBloodVolume(), definition.getMaxBloodVolume())));
        return 0;
    }

    private static int setBloodVolume(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        if (!(entity instanceof LivingEntity livingEntity) || !BloodSystemManager.isEnabled(livingEntity)) {
            throw NO_BLOOD_DATA_FOUND.create();
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
        float volume = FloatArgumentType.getFloat(ctx, "volume");
        bloodSystem.setBloodVolume(volume);
        bloodSystem.synchronizeImmediately(livingEntity);
        return 0;
    }
}
