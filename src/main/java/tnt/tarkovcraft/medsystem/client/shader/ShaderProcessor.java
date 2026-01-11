package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.*;

public final class ShaderProcessor {

    public static final Marker MARKER = MarkerManager.getMarker("Shaders");
    public static final ShaderProcessor INSTANCE = new ShaderProcessor();
    private final List<ShaderProgram> registeredShaders = new ArrayList<>();
    private final Set<ShaderProgram> pendingActivation = new HashSet<>();
    private final Set<ResourceLocation> activeShaderIds = new HashSet<>();
    private final Set<ShaderInstanceHolder> activeShaders = new HashSet<>();

    private ShaderProcessor() {}

    public void registerProgram(ShaderProgram program) {
        synchronized (this.registeredShaders) {
            this.registeredShaders.add(program);
        }
    }

    public void tick() {
        Minecraft client = Minecraft.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        if (
                client.level == null ||
                cameraEntity == null ||
                !cameraEntity.isAlive() ||
                !(cameraEntity instanceof LivingEntity entity)
        ) {
            return;
        }
        this.registeredShaders.forEach(program -> {
            program.update(client, entity);
            if (!this.activeShaderIds.contains(program.postChainId()) && program.shouldRender()) {
                MedicalSystem.LOGGER.debug(MARKER, "Activating shader {}", program.postChainId());
                this.pendingActivation.add(program);
            }
        });
    }

    public void render(float delta) {
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        this.loadPendingShaders();
        Iterator<ShaderInstanceHolder> iterator = this.activeShaders.iterator();
        while (iterator.hasNext()) {
            ShaderInstanceHolder shader = iterator.next();
            if (!shader.canRender()) {
                MedicalSystem.LOGGER.debug(MARKER, "Disabling shader {}", shader);
                shader.close();
                this.activeShaderIds.remove(shader.program.postChainId());
                iterator.remove();
                return;
            }
            shader.render(delta);
        }
    }

    public void resize(int width, int height) {
        this.activeShaders.forEach(shader -> shader.resize(width, height));
    }

    private void loadPendingShaders() {
        Minecraft minecraft = Minecraft.getInstance();
        Iterator<ShaderProgram> iterator = this.pendingActivation.iterator();
        while (iterator.hasNext()) {
            ShaderProgram program = iterator.next();
            ResourceLocation shaderLocation = program.postChainId().withPath(id -> "shaders/post/" + id + ".json");
            MedicalSystem.LOGGER.debug(MARKER, "Loading post effect shader {}", shaderLocation);
            try {
                PostChain postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), shaderLocation);
                Window window = minecraft.getWindow();
                ShaderInstanceHolder shader = new ShaderInstanceHolder(program, postChain);
                shader.resize(window);
                this.activeShaders.add(shader);
                this.activeShaderIds.add(program.postChainId());
            } catch (Exception e) {
                MedicalSystem.LOGGER.error(MARKER, "Failed to load post effect shader {}", shaderLocation, e);
            }
            iterator.remove();
        }
    }

    private record ShaderInstanceHolder(ShaderProgram program, PostChain postChain) {

        boolean canRender() {
            return this.program.shouldRender();
        }

        void render(float delta) {
            this.program.renderTick(delta, this.postChain::setUniform);
            this.postChain.process(delta);
        }

        void resize(Window window) {
            this.resize(window.getWidth(), window.getHeight());
        }

        void resize(int width, int height) {
            this.postChain.resize(width, height);
        }

        void close() {
            this.postChain.close();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ShaderProgram shaderProgram) {
                return Objects.equals(this.program.postChainId(), shaderProgram.postChainId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(program.postChainId());
        }

        @Override
        public @NotNull String toString() {
            return this.program.postChainId().toString();
        }
    }
}
