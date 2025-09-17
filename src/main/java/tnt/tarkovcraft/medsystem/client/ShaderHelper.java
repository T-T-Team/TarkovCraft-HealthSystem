package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

public final class ShaderHelper {

    public static void renderPostEffects(Minecraft minecraft, CrossFrameResourcePool resourcePool) {
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || !HealthSystem.hasCustomHealth(camera)) {
            return;
        }
        HealthContainer container = HealthSystem.getHealthData((LivingEntity) camera);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        RenderSystem.resetTextureMatrix();
        for (StatusEffect effect : effects) {
            StatusEffectType<?> type = effect.getType();
            if (type.hasPostShader()) {
                ResourceLocation shaderId = type.getIdentifier();
                PostChain postChain = minecraft.getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);
                if (postChain != null) {
                    postChain.process(minecraft.getMainRenderTarget(), resourcePool);
                }
            }
        }
    }
}
