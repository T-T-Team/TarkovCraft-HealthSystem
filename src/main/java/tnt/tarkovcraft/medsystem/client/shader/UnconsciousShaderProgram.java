package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.function.BiConsumer;

public class UnconsciousShaderProgram implements ShaderProgram {

    public static final UnconsciousShaderProgram INSTANCE = new UnconsciousShaderProgram();
    public static final ResourceLocation IDENTIFIER = MedicalSystem.resource("unconscious");
    private boolean unconscious;
    private long gameTime;

    private UnconsciousShaderProgram() {}

    @Override
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }

    @Override
    public void update(Minecraft client, LivingEntity entity) {
        this.unconscious = BloodSystem.isEntityUnconscious(entity);
        this.gameTime = client.level.getGameTime() % 24000L;
    }

    @Override
    public boolean shouldRender() {
        return this.unconscious;
    }

    @Override
    public void renderTick(float delta, BiConsumer<String, Float> uniformSetter) {
        uniformSetter.accept("GameTime", this.gameTime / 24000.0F);
    }
}
