package tnt.tarkovcraft.medsystem.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;

@SuppressWarnings("unchecked")
public final class TarkovCraftCommand {

    public static void create(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("tarkovcraft")
                        .then(
                                Commands.literal("effect")
                                        .requires(src -> src.hasPermission(2))
                                        .then(
                                                Commands.argument("target", EntityArgument.entities())
                                                        .then(
                                                                Commands.literal("add")
                                                                        .then(
                                                                                Commands.argument("bodypart", StringArgumentType.word())
                                                                                        .then(
                                                                                                Commands.argument("type", ResourceArgument.resource(context, MedSystemRegistries.Keys.STATUS_EFFECT))
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
                                                                                Commands.argument("type", ResourceArgument.resource(context, MedSystemRegistries.Keys.STATUS_EFFECT))
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
                                                                                                Commands.argument("bodypart", StringArgumentType.word())
                                                                                                        .executes(TarkovCraftCommand::removeLocalStatusEffect)
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                Commands.literal("removeGlobal")
                                                                        .then(
                                                                                Commands.argument("type", ResourceArgument.resource(context, MedSystemRegistries.Keys.STATUS_EFFECT))
                                                                                        .executes(TarkovCraftCommand::removeGlobalStatusEffect)
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int addGlobalStatusEffect(CommandContext<CommandSourceStack> ctx, int duration, int delay) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
            StatusEffectMap map = container.getGlobalStatusEffects();
            addEffect(map, reference, duration, delay);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static int addLocalStatusEffect(CommandContext<CommandSourceStack> ctx, int duration, int delay) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        String bodyPartId = StringArgumentType.getString(ctx, "bodypart");
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
            if (!container.hasBodyPart(bodyPartId)) {
                continue;
            }
            BodyPart bodyPart = container.getBodyPart(bodyPartId);
            StatusEffectMap map = bodyPart.getStatusEffects();
            addEffect(map, reference, duration, delay);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static <T extends StatusEffect> void addEffect(StatusEffectMap map, Holder<StatusEffectType<?>> holder, int duration, int delay) {
        StatusEffectType<T> type = (StatusEffectType<T>) holder.value();
        T effect = delay > 0 ? type.createDelayedEffect(duration, delay) : type.createImmediateEffect(duration);
        map.replace(effect);
    }

    private static int removeGlobalStatusEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
            StatusEffectMap map = container.getGlobalStatusEffects();
            Context context = ContextImpl.of(
                    ContextKeys.LIVING_ENTITY, livingEntity,
                    MedicalSystemContextKeys.HEALTH_CONTAINER, container
            );
            map.remove(reference.value(), context);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }

    private static int removeLocalStatusEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Holder.Reference<StatusEffectType<?>> reference = ResourceArgument.getResource(ctx, "type", MedSystemRegistries.Keys.STATUS_EFFECT);
        String bodyPartId = StringArgumentType.getString(ctx, "bodypart");
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
            if (!container.hasBodyPart(bodyPartId)) {
                continue;
            }
            BodyPart bodyPart = container.getBodyPart(bodyPartId);
            Context context = ContextImpl.of(
                    ContextKeys.LIVING_ENTITY, livingEntity,
                    MedicalSystemContextKeys.HEALTH_CONTAINER, container,
                    MedicalSystemContextKeys.BODY_PART, bodyPart
            );
            StatusEffectMap map = bodyPart.getStatusEffects();
            map.remove(reference.value(), context);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
    }
}
