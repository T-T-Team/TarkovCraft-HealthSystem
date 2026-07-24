package tnt.tarkovcraft.medsystem.integration.sable;

import dev.leo.sableplayerragdoll.api.DespawnCondition;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.api.RagdollLaunchOptions;
import dev.leo.sableplayerragdoll.api.RagdollSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

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
        if (entity instanceof ServerPlayer player) {
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

    private Vec3 computeRagdollVelocity(LivingEntity entity) {
        return entity.getLookAngle().scale(-10.0);
    }

    private record IsConsciousDespawnCondition() implements DespawnCondition {

        public static final IsConsciousDespawnCondition INSTANCE = new IsConsciousDespawnCondition();

        @Override
        public boolean shouldDespawn(RagdollSession ragdollSession) {
            return !BloodSystemManager.isUnconscious(ragdollSession.player());
        }
    }
}
