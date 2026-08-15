package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import java.util.Collection;
import java.util.List;

import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.NO_VALID_TARGET_FOUND;

public final class HealSubCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> node() {
        return Commands.literal("heal")
                .then(
                        Commands.argument("target", EntityArgument.entities())
                                .then(
                                        Commands.argument("limb", StringArgumentType.word())
                                                .suggests(MedSystemTarkovCraftCommand::suggestAllEntityLimbs)
                                                .then(
                                                        Commands.argument("amount", FloatArgumentType.floatArg(0.01F))
                                                                .executes(ctx -> healEntityLimb(ctx, false))
                                                                .then(
                                                                        Commands.literal("includeDisabledLimbs")
                                                                                .executes(HealSubCommand::healDisabledEntityLimb)
                                                                )
                                                )
                                )
                                .then(
                                        Commands.argument("amount", FloatArgumentType.floatArg(0.01F))
                                                .executes(ctx -> healEntity(ctx, false))
                                                .then(
                                                        Commands.literal("includeDisabledLimbs")
                                                                .executes(HealSubCommand::healEntityIgnoringDisabledLimbs)
                                                )
                                )
                );
    }

    private static int healEntityIgnoringDisabledLimbs(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return healEntity(ctx, true);
    }

    private static int healEntity(CommandContext<CommandSourceStack> ctx, boolean allowDisabled) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        List<LivingEntity> entities = targets.stream()
                .filter(HealthSystem::hasCustomHealth)
                .map(entity -> (LivingEntity) entity)
                .toList();
        if (entities.isEmpty())
            throw NO_VALID_TARGET_FOUND.create();
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        for (LivingEntity entity : entities) {
            HealthContainer container = HealthContainer.getAttached(entity);
            float remainingAmount = amount;
            while (remainingAmount > 0.0F) {
                Limb limb = HealthHelper.selectLimbForHealing(container, allowDisabled);
                if (limb == null)
                    break;
                float healAmount = Math.min(remainingAmount, limb.getMaxHealAmount());
                remainingAmount -= healAmount;
                limb.heal(healAmount);
            }
            HealthHelper.synchronizeHealth(entity, container);
            HealthSystem.synchronizeEntity(entity);
        }
        return 0;
    }

    private static int healDisabledEntityLimb(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return healEntityLimb(ctx, true);
    }

    private static int healEntityLimb(CommandContext<CommandSourceStack> ctx, boolean allowDisabled) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        List<LivingEntity> entities = targets.stream()
                .filter(HealthSystem::hasCustomHealth)
                .map(entity -> (LivingEntity) entity)
                .toList();
        if (entities.isEmpty())
            throw NO_VALID_TARGET_FOUND.create();
        String limbCode = StringArgumentType.getString(ctx, "limb");
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        for (LivingEntity entity : entities) {
            HealthContainer container = HealthContainer.getAttached(entity);
            Limb limb = container.getLimbByCode(limbCode);
            if (limb != null && (allowDisabled || !limb.isDead())) {
                limb.heal(amount);
            }
            HealthHelper.synchronizeHealth(entity, container);
            HealthSystem.synchronizeEntity(entity);
        }
        return 0;
    }
}
