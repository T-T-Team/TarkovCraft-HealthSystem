package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.LimbDamageSource;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

import java.util.Collection;
import java.util.List;

import static tnt.tarkovcraft.medsystem.common.command.MedSystemTarkovCraftCommand.NO_VALID_TARGET_FOUND;

public final class HurtSubCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> node(CommandBuildContext context) {
        return Commands.literal("hurt")
                .then(
                        Commands.argument("target", EntityArgument.entities())
                                .then(
                                        Commands.argument("limb", StringArgumentType.word())
                                                .suggests(MedSystemTarkovCraftCommand::suggestAllEntityLimbs)
                                                .then(
                                                        Commands.argument("damage_type", ResourceArgument.resource(context, Registries.DAMAGE_TYPE))
                                                                .then(
                                                                        Commands.argument("amount", FloatArgumentType.floatArg(0.01F))
                                                                                .executes(ctx -> hurtLimb(ctx, null, null))
                                                                                .then(
                                                                                        Commands.argument("causing_entity", EntityArgument.entity())
                                                                                                .executes(ctx -> hurtLimb(ctx, EntityArgument.getEntity(ctx, "causing_entity"), null))
                                                                                                .then(
                                                                                                        Commands.argument("direct_entity", EntityArgument.entity())
                                                                                                                .executes(ctx -> hurtLimb(
                                                                                                                        ctx,
                                                                                                                        EntityArgument.getEntity(ctx, "causing_entity"),
                                                                                                                        EntityArgument.getEntity(ctx, "direct_entity")
                                                                                                                ))
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                );
    }

    private static int hurtLimb(CommandContext<CommandSourceStack> ctx, Entity source, Entity projectile) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        List<LivingEntity> entities = targets.stream()
                .filter(HealthSystem::hasCustomHealth)
                .map(entity -> (LivingEntity) entity)
                .toList();
        if (entities.isEmpty())
            throw NO_VALID_TARGET_FOUND.create();
        Holder<DamageType> damageTypeHolder = ResourceArgument.getResource(ctx, "damage_type", Registries.DAMAGE_TYPE);
        String limb = StringArgumentType.getString(ctx, "limb");
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        DamageSource damageSource = new LimbDamageSource(damageTypeHolder, projectile, source, limb);
        for (LivingEntity entity : entities) {
            entity.hurt(damageSource, amount);
        }
        return 0;
    }
}
