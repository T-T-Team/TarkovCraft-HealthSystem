package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ShaderHelper {

    private static final Set<Identifier> POST_EFFECTS = new LinkedHashSet<>();

    public static void renderPostEffects(Minecraft minecraft, CrossFrameResourcePool resourcePool) {
        for (Identifier shaderId : POST_EFFECTS) {
            PostChain postChain = minecraft.getShaderManager().getPostChain(shaderId, LevelTargetBundle.MAIN_TARGETS);
            if (postChain != null) {
                postChain.process(minecraft.getMainRenderTarget(), resourcePool);
            }
        }
    }

    public static void updateActiveEffects(ClientTickEvent.Pre event) {
        POST_EFFECTS.clear();
        Minecraft minecraft = Minecraft.getInstance();
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || !HealthSystem.hasCustomHealth(camera)) {
            return;
        }
        HealthContainer container = HealthSystem.getHealthData((LivingEntity) camera);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        List<Identifier> blockedShaders = new ArrayList<>();
        for (StatusEffect effect : effects) {
            StatusEffectType<?> type = effect.getType();
            if (type.hasPostShader()) {
                Identifier shaderId = type.getIdentifier();
                POST_EFFECTS.add(shaderId);
                blockedShaders.addAll(type.getBlockedPostEffects());
            }
        }
        blockedShaders.forEach(POST_EFFECTS::remove);
    }
}
