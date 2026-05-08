package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.core.api.shader.ShaderType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.UnconsciousOverlayType;

public final class UnconsciousEffectShaderProgram implements PostEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("unconscious");
    private boolean unconscious;

    @Override
    public void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        MedSystemConfig config = MedicalSystem.getConfig();
        this.unconscious = config.bloodSystem.unconsciousOverlayType == UnconsciousOverlayType.BLINKING && BloodSystemManager.isUnconscious(livingEntity);
    }

    @Override
    public void onRender(DeltaTracker deltaTracker) {

    }

    @Override
    public boolean active() {
        return this.unconscious;
    }

    @Override
    public ShaderType getShaderType() {
        return ShaderType.GAME;
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }
}
