package tnt.tarkovcraft.medsystem.client;

import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class MedicalSystemClientEventHandler {

    public static Vec3 from;
    public static Vec3 to;

    @SubscribeEvent
    private void renderLevelStageEvent(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
        }
    }
}
