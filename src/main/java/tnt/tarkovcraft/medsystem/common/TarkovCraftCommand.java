package tnt.tarkovcraft.medsystem.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.api.LimbDamageSource;
import tnt.tarkovcraft.medsystem.common.argument.StatusEffectArgument;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class TarkovCraftCommand {

    private static final SimpleCommandExceptionType NO_VALID_TARGET_FOUND = new SimpleCommandExceptionType(Component.literal("No health containers found for given entity selector"));
    private static final SimpleCommandExceptionType NO_BLOOD_DATA_FOUND = new SimpleCommandExceptionType(Component.literal("No blood data found for given entity"));
    private static final SimpleCommandExceptionType UNCONSCIOUS_MODE_DISABLED = new SimpleCommandExceptionType(Component.literal("Unconscious mode is not allowed for given entity"));
    private static final DynamicCommandExceptionType INVALID_STATUS_EFFECT = new DynamicCommandExceptionType(arg -> Component.literal("Status effect " + arg + " is not assignable"));

    public static void create(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("tarkovcraft")
                        .then(
                                Commands.literal("effect")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(
                                                Commands.argument("target", EntityArgument.entities())
                                                        .then(
                                                                Commands.literal("add")
                                                                        .then(
                                                                                Commands.argument("limb", StringArgumentType.word())
                                                                                        .suggests(TarkovCraftCommand::suggestAllEntityLimbs)
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
                                                                                                        .suggests(TarkovCraftCommand::suggestAllEntityLimbs)
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
                        .then(
                                Commands.literal("hurt")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(
                                                Commands.argument("target", EntityArgument.entities())
                                                        .then(
                                                                Commands.argument("limb", StringArgumentType.word())
                                                                        .suggests(TarkovCraftCommand::suggestAllEntityLimbs)
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
                                        )

                        )
                        .then(
                                Commands.literal("blood")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(
                                                Commands.argument("target", EntityArgument.entity())
                                                        .executes(TarkovCraftCommand::getBloodInfo)
                                                        .then(
                                                                Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                                                                        .executes(TarkovCraftCommand::setBloodVolume)
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("unconscious")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(
                                                Commands.argument("target", EntityArgument.entity())
                                                        .then(
                                                                Commands.argument("time", IntegerArgumentType.integer(0))
                                                                        .executes(TarkovCraftCommand::setUnconsciousState)
                                                        )
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
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
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
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
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

    private static <T extends StatusEffect> void addEffect(StatusEffectMap map, LivingEntity entity, @Nullable Limb limb, HealthContainer container, StatusEffect template, int delay) throws CommandSyntaxException {
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
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
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
        String bodyPartId = StringArgumentType.getString(ctx, "limb");
        Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "target");
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || !HealthSystem.hasCustomHealth(livingEntity)) {
                continue;
            }
            HealthContainer container = HealthSystem.getHealthData(livingEntity);
            if (!container.hasLimb(bodyPartId)) {
                continue;
            }
            Limb limb = container.getLimbByCode(bodyPartId);
            StatusEffectMap map = limb.getStatusEffects();
            StatusEffectHelper.removeEffect(StatusEffectSubmitter.NOOP, map, livingEntity, limb, container, type);
            HealthSystem.synchronizeEntity(livingEntity);
        }
        return 0;
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
            entity.hurtServer((ServerLevel) entity.level(), damageSource, amount);
        }
        return 0;
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
        bloodSystem.setUnconscious(time, UnconsciousOptions.PAIN_SHOCK);
        bloodSystem.synchronizeImmediately(livingEntity);
        return 0;
    }

    private static CompletableFuture<Suggestions> suggestAllEntityLimbs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(ctx, "target");
        Set<String> suggestions = new HashSet<>();
        for (Entity entity : entities) {
            if (!HealthSystem.hasCustomHealth(entity))
                continue;
            HealthContainer container = HealthSystem.getHealthData((LivingEntity) entity);
            HealthContainerDefinition definition = container.getDefinition();
            LimbConfiguration configuration = definition.limbConfiguration();
            suggestions.addAll(configuration.getLimbCodes());
        }
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
