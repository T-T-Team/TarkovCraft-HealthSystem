package tnt.tarkovcraft.medsystem.integration.sable;

import dev.leo.sableplayerragdoll.api.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tnt.tarkovcraft.core.api.EntityInteraction;
import tnt.tarkovcraft.core.api.event.EntityInteractionEvent;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityInteractions;

import java.util.Collections;

public final class SableEventListener {

    private static final RagdollLaunchOptions RAGDOLL_OPTIONS = RagdollLaunchOptions.builder()
            .autoSeat(true)
            .lockDismount(true)
            .despawnConditions(Collections.singletonList(IsConsciousDespawnCondition.INSTANCE))
            .build();

    @SubscribeEvent
    private void onUnconsciousStart(BloodSystemEvent.UnconsciousStart event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide())
            return;
        if (entity instanceof ServerPlayer player && !player.isPassenger()) {
            event.disableModelAnimation();
            RagdollAPI.launch(player, this.computeRagdollVelocity(player), RAGDOLL_OPTIONS);
        }
    }

    @SubscribeEvent
    private void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && BloodSystemManager.isUnconscious(serverPlayer) && !RagdollAPI.isRagdolled(serverPlayer)) {
            RagdollAPI.launch(serverPlayer, this.computeRagdollVelocity(serverPlayer), RAGDOLL_OPTIONS);
        }
    }

    // does nothing...
    /*@SubscribeEvent
    private void onRagdollInteraction(RagdollInteractEvent event) {
        ServerPlayer player = event.player();
        if (player.isCrouching()) {
            event.setCanceled(true);
        }
    }*/

    @SubscribeEvent
    private void onInteractionFinished(EntityInteractionEvent.OnFinished event) {
        EntityInteraction.InteractionResult result = event.getInteractionResult();
        if (result != EntityInteraction.InteractionResult.SUCCESS) {
            return;
        }
        LivingEntity entity = event.getInteractionTarget();
        if (!(entity instanceof ServerPlayer player))
            return;
        if (event.getInteraction().type() == MedSystemEntityInteractions.DISMOUNT_ENTITY.value() && !RagdollAPI.isRagdolled(player)) {
            RagdollAPI.launch(player, Vec3.ZERO, RAGDOLL_OPTIONS);
        }
    }

    private Vec3 computeRagdollVelocity(LivingEntity entity) {
        int hurtAt = entity.getLastHurtByMobTimestamp();
        DamageSource damageSource = entity.getLastDamageSource();
        float lookAngleScaleF = 10.0F;
        if (entity.tickCount - hurtAt < 10 && damageSource != null) {
            Entity attacker = damageSource.isDirect() ? damageSource.getDirectEntity() : damageSource.getEntity();
            if (attacker != null) {
                return attacker instanceof Projectile projectile
                        ? projectile.getDeltaMovement()
                        : attacker.getLookAngle().scale(lookAngleScaleF);
            }
        }
        return entity.getLookAngle().scale(-lookAngleScaleF);
    }

    private record IsConsciousDespawnCondition() implements DespawnCondition {

        public static final IsConsciousDespawnCondition INSTANCE = new IsConsciousDespawnCondition();

        @Override
        public boolean shouldDespawn(RagdollSession ragdollSession) {
            return !BloodSystemManager.isUnconscious(ragdollSession.player());
        }
    }
}
