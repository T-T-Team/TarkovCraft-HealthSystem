package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.NO_BLOOD_DATA_FOUND;
import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.UNCONSCIOUS_MODE_DISABLED;

public final class SetUnconsciousSubCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("unconscious")
                .then(
                        Commands.argument("target", EntityArgument.entity())
                                .then(
                                        Commands.argument("time", IntegerArgumentType.integer(0))
                                                .executes(SetUnconsciousSubCommand::setUnconsciousState)
                                )
                );
    }

    private static int setUnconsciousState(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "target");
        if (!(entity instanceof LivingEntity livingEntity) || !BloodSystemManager.isEnabled(livingEntity)) {
            throw NO_BLOOD_DATA_FOUND.create();
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        if (!definition.isUnconsciousModeAllowed()) {
            throw UNCONSCIOUS_MODE_DISABLED.create();
        }
        int time = IntegerArgumentType.getInteger(ctx, "time");
        bloodSystem.setUnconscious(livingEntity, time, UnconsciousOptions.PAIN_SHOCK, true);
        bloodSystem.markForUpdate();
        return 0;
    }
}
