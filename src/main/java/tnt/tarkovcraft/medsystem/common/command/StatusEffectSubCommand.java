package tnt.tarkovcraft.medsystem.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.common.argument.StatusEffectArgument;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;

public final class StatusEffectSubCommand {

    private static final DynamicCommandExceptionType INVALID_STATUS_EFFECT = new DynamicCommandExceptionType(arg -> Component.literal("Status effect " + arg + " is not assignable"));

    public static ArgumentBuilder<CommandSourceStack, ?> node(CommandBuildContext context) {
        return Commands.literal("effect")
                .then(
                        Commands.argument("target", EntityArgument.entities())
                                .then(
                                        Commands.literal("add")
                                                .then(
                                                        Commands.argument("limb", StringArgumentType.word())
                                                                .suggests(MedSystemTarkovCraftCommand::suggestAllEntityLimbs)
                                                                .then(
                                                                        Commands.argument("status_effect", StatusEffectArgument.statusEffect(context))
                                                                                .executes(ctx -> addLocalStatusEffect(ctx, Duration.seconds(60).tickValue(), 0))
                                                                                .then(
                                                                                        Commands.literal("infinite")
                                                                                                .executes(ctx -> addLocalStatusEffect(ctx, -1, 0))
                                                                                                .then(
                                                                                                        Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                                                                .executes(ctx -> addLocalStatusEffect(ctx, -1, IntegerArgumentType.getInteger(ctx, "delay")))
                                                                                                )
                                                                                )
                                                                                .then(
                                                                                        Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                                                .executes(ctx -> addLocalStatusEffect(ctx, IntegerArgumentType.getInteger(ctx, "duration"), 0))
                                                                                                .then(
                                                                                                        Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                                                                .executes(ctx -> addLocalStatusEffect(ctx, IntegerArgumentType.getInteger(ctx, "duration"), IntegerArgumentType.getInteger(ctx, "delay")))
                                                                                                )
                                                                                )
                                                                )
                                                )

                                )
                                .then(
                                        Commands.literal("addGlobal")
                                                .then(
                                                        Commands.argument("status_effect", StatusEffectArgument.statusEffect(context))
                                                                .executes(ctx -> addGlobalStatusEffect(ctx, Duration.seconds(60).tickValue(), 0))
                                                                .then(
                                                                        Commands.literal("infinite")
                                                                                .executes(ctx -> addGlobalStatusEffect(ctx, -1, 0))
                                                                                .then(
                                                                                        Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                                                .executes(ctx -> addGlobalStatusEffect(ctx, -1, IntegerArgumentType.getInteger(ctx, "delay")))
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                                .executes(ctx -> addGlobalStatusEffect(ctx, IntegerArgumentType.getInteger(ctx, "duration"), 0))
                                                                                .then(
                                                                                        Commands.argument("delay", IntegerArgumentType.integer(0))
                                                                                                .executes(ctx -> addGlobalStatusEffect(ctx, IntegerArgumentType.getInteger(ctx, "duration"), IntegerArgumentType.getInteger(ctx, "delay")))
                                                                                )
                                                                )
                                                )
                                )
                                .then(
                                        Commands.literal("remove")
                                                .then(
                                                        Commands.argument("type", ResourceArgument.resource(context, MedSystemRegistries.Keys.STATUS_EFFECT))
                                                                .then(
                                                                        Commands.argument("limb", StringArgumentType.word())
                                                                                .suggests(MedSystemTarkovCraftCommand::suggestAllEntityLimbs)
                                                                                .executes(StatusEffectSubCommand::removeLocalStatusEffect)
                                                                )
                                                )
                                )
                                .then(
                                        Commands.literal("removeGlobal")
                                                .then(
                                                        Commands.argument("type", ResourceArgument.resource(context, MedSystemRegistries.Keys.STATUS_EFFECT))
                                                                .executes(StatusEffectSubCommand::removeGlobalStatusEffect)
                                                )
                                )
                );
    }

    private static int addGlobalStatusEffect(CommandContext<CommandSourceStack> ctx, int duration, int delay) throws CommandSyntaxException {
        StatusEffect template = StatusEffectArgument.getStatusEffect(ctx, "status_effect");
        template.setDuration(duration);
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            StatusEffectMap map = container.getGlobalStatusEffects();
            addEffect(map, livingEntity, null, container, template, delay);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static int addLocalStatusEffect(CommandContext<CommandSourceStack> ctx, int duration, int delay) throws CommandSyntaxException {
        StatusEffect template = StatusEffectArgument.getStatusEffect(ctx, "status_effect");
        template.setDuration(duration);
        String limbCode = StringArgumentType.getString(ctx, "limb");
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            if (!container.hasLimb(limbCode)) {
                continue;
            }
            Limb limb = container.getLimbByCode(limbCode);
            StatusEffectMap map = limb.getStatusEffects();
            addEffect(map, livingEntity, limb, container, template, delay);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static void addEffect(StatusEffectMap map, LivingEntity entity, @Nullable Limb limb, HealthContainer container, StatusEffect template, int delay) throws CommandSyntaxException {
        StatusEffectType<?> type = template.getType();
        if (type.isSpecialStatusEffect()) {
            throw INVALID_STATUS_EFFECT.create(MedSystemRegistries.STATUS_EFFECT.getKey(type));
        }
        StatusEffectHelper.removeEffect(StatusEffectSubmitter.NOOP, map, entity, limb, container, type);
        StatusEffectHelper.addEffect(map, entity, limb, delay, template.copy());
    }

    private static int removeGlobalStatusEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        StatusEffectType<?> type = reference.value();
        if (type.isSpecialStatusEffect()) {
            throw INVALID_STATUS_EFFECT.create(reference.getKey().identifier());
        }
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            StatusEffectMap map = container.getGlobalStatusEffects();
            StatusEffectHelper.removeEffect(StatusEffectSubmitter.NOOP, map, livingEntity, null, container, type);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static int removeLocalStatusEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        StatusEffectType<?> type = reference.value();
        if (type.isSpecialStatusEffect()) {
            throw INVALID_STATUS_EFFECT.create(reference.getKey().identifier());
        }
        String limbCode = StringArgumentType.getString(ctx, "limb");
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            if (!container.hasLimb(limbCode)) {
                continue;
            }
            Limb limb = container.getLimbByCode(limbCode);
            StatusEffectMap map = limb.getStatusEffects();
            StatusEffectHelper.removeEffect(StatusEffectSubmitter.NOOP, map, livingEntity, limb, container, type);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }
}
