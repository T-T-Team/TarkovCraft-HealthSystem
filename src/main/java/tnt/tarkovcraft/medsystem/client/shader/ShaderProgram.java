package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;

public interface ShaderProgram {

    ResourceLocation postChainId();

    void update(Minecraft client, LivingEntity entity);

    boolean shouldRender();

    void renderTick(float delta, BiConsumer<String, Float> uniformSetter);

    @FunctionalInterface
    interface UniformRegistration {
        void register(String name, int type, int count);
    }
}
