package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public final class UnconsciousEffectShaderProgram implements PostEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("unconscious");
    private boolean unconscious;

    @Override
    public void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        this.unconscious = BloodSystem.isEntityUnconscious(livingEntity);
    }

    @Override
    public void onRender(DeltaTracker deltaTracker) {

    }

    @Override
    public boolean active() {
        return this.unconscious;
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }
}
