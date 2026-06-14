package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.core.api.shader.ShaderType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

public class UnconsciousEffectShaderProgram implements PostEffectShaderProgram {

    public static final UnconsciousEffectShaderProgram INSTANCE = new UnconsciousEffectShaderProgram();
    public static final ResourceLocation IDENTIFIER = MedicalSystem.createIdentifier("unconscious");
    private boolean unconscious;
    private long gameTime;

    private UnconsciousEffectShaderProgram() {}

    @Override
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }

    @Override
    public void tickProgram(Minecraft client, LivingEntity entity) {
        this.unconscious = BloodSystemManager.isUnconscious(entity);
        this.gameTime = client.level.getGameTime() % 24000L;
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
    public void onRender(float delta, UniformSetter uniformSetter) {
        uniformSetter.setUniform("GameTime", this.gameTime / 24000.0F);
    }
}
