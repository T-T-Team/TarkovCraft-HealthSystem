package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Easing;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.BiConsumer;

public final class ConcussionShaderProgram extends StatusEffectShaderProgram {

    public static final ConcussionShaderProgram INSTANCE = new ConcussionShaderProgram();

    private long gameTime;

    private ConcussionShaderProgram() {}

    @Override
    protected Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.CONCUSSION;
    }

    @Override
    public void update(Minecraft client, LivingEntity entity) {
        super.update(client, entity);
        this.gameTime = client.level.getGameTime() % 24000L;
    }

    @Override
    protected float getGain() {
        return 1.0F;
    }

    @Override
    protected float getDecay() {
        return 0.01F;
    }

    @Override
    public void renderTick(float delta, BiConsumer<String, Float> uniformSetter) {
        super.renderTick(delta, uniformSetter);
        uniformSetter.accept("GameTime", this.gameTime / 24000.0F);
    }

    @Override
    protected float applySmoothing(float delta, float start, float end) {
        return Easing.EASE_OUT_SINE.apply(super.applySmoothing(delta, start, end));
    }
}
