package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.*;
import java.util.stream.Collectors;

public final class ShaderHelper {

    private static final Set<ResourceLocation> ACTIVE_POST_EFFECTS = new LinkedHashSet<>();
    private static final Set<IdentifiablePostChain> LOADED_SHADERS = new LinkedHashSet<>();

    public static void apply(DeltaTracker tracker) {
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        LOADED_SHADERS.forEach(post -> post.postChain.process(tracker.getGameTimeDeltaTicks()));
    }

    public static void notifyUpdate(HealthContainer container, IAttachmentHolder holder) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (holder != player)
            return;
        StatusEffectMap map = container.getGlobalStatusEffects();
        Set<ResourceLocation> activeEffectIds = map.listEffects().stream()
                .map(effect -> effect.getType().getIdentifier())
                .collect(Collectors.toSet());
        ACTIVE_POST_EFFECTS.removeIf(activeEffect -> !activeEffectIds.contains(activeEffect));
        List<ResourceLocation> blocked = new ArrayList<>();
        for (StatusEffect effect : map) {
            StatusEffectType<?> type = effect.getType();
            if (type.hasPostShader()) {
                ACTIVE_POST_EFFECTS.add(type.getIdentifier());
                blocked.addAll(type.getBlockedPostEffects());
            }
        }
        blocked.forEach(ACTIVE_POST_EFFECTS::remove);
        Iterator<IdentifiablePostChain> postChainIterator = LOADED_SHADERS.iterator();
        while (postChainIterator.hasNext()) {
            IdentifiablePostChain postChain = postChainIterator.next();
            if (!ACTIVE_POST_EFFECTS.contains(postChain.id)) {
                postChain.postChain.close();
                postChainIterator.remove();
                MedicalSystem.LOGGER.debug("Deactivating post effect shader {}", postChain.id);
            }
        }

        Set<ResourceLocation> loadedShaderIdSet = LOADED_SHADERS.stream().map(chain -> chain.id).collect(Collectors.toSet());
        for (ResourceLocation location : ACTIVE_POST_EFFECTS) {
            if (!loadedShaderIdSet.contains(location)) {
                ResourceLocation shaderLocation = location.withPath(id -> "shaders/post/" + id + ".json");
                try {
                    PostChain postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), shaderLocation);
                    postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
                    LOADED_SHADERS.add(new IdentifiablePostChain(location, postChain));
                    MedicalSystem.LOGGER.debug("Activating post effect shader {}", shaderLocation);
                } catch (Exception e) {
                    MedicalSystem.LOGGER.error("Failed to load post effect shader {}", shaderLocation, e);
                }
            }
        }
    }

    public static void resize(int width, int height) {
        LOADED_SHADERS.forEach(chain -> chain.postChain.resize(width, height));
    }

    private record IdentifiablePostChain(ResourceLocation id, PostChain postChain) {

        @Override
        public boolean equals(Object o) {
            if (o instanceof ResourceLocation location) {
                return Objects.equals(id, location);
            }
            if (!(o instanceof IdentifiablePostChain that)) return false;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }
}
