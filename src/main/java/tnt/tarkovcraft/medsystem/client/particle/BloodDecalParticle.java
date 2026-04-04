package tnt.tarkovcraft.medsystem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.client.particle.DecalParticle;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;

import java.util.Optional;

public final class BloodDecalParticle extends DecalParticle {

    private final int inputColor;

    public BloodDecalParticle(ClientLevel level, BloodDecalParticleOptions options, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, options, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        BloodDecalConfig config = MedicalSystem.getConfig().bloodDecals;
        this.inputColor = config.getDecalColor(options.color());
        this.updateColor(1.0F);
        this.quadSize = config.bloodDecalScale;
        this.setFadeOutStartTime(config.bloodDecalFadeOutAt);
        this.setRoll(this.random.nextFloat() * Mth.PI);
        this.setLifetime(config.bloodDecalLifetime);
        this.age = options.decalAge();
        this.updateColor(this.age / (float) this.lifetime);
    }

    @Override
    protected void updateColor(float lifetimeLeft) {
        int color = BloodDripParticle.getParticleColor(this.inputColor);
        this.rCol = lifetimeLeft * ARGB.redFloat(color);
        this.gCol = lifetimeLeft * ARGB.greenFloat(color);
        this.bCol = lifetimeLeft * ARGB.blueFloat(color);
    }

    @Override
    public Optional<ParticleLimit> getParticleLimit() {
        return Optional.of(MedicalSystemClient.BLOOD_PARTICLES_LIMIT);
    }

    @Override
    protected void handleAttachedBlockRemoved(BlockState state) {
        this.level.addParticle(new BloodDripParticleOptions(this.inputColor, this.age), true, true, this.x, this.y, this.z, 0, 0, 0);
        super.handleAttachedBlockRemoved(state);
    }

    public static final class Provider implements ParticleProvider<BloodDecalParticleOptions> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(BloodDecalParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new BloodDecalParticle(level, options, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites.get(random));
        }
    }
}
