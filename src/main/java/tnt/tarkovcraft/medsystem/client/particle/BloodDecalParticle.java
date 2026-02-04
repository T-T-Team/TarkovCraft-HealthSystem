package tnt.tarkovcraft.medsystem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.client.particle.DecalParticle;
import tnt.tarkovcraft.core.common.particle.SimpleDecalParticleOptions;
import tnt.tarkovcraft.core.util.helper.ARGB;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;

public final class BloodDecalParticle extends DecalParticle {

    public BloodDecalParticle(ClientLevel level, Direction direction, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, direction, x, y, z, xSpeed, ySpeed, zSpeed);
        BloodDecalConfig config = MedicalSystem.getConfig().bloodDecals;
        this.updateColor(1.0F);
        this.quadSize = config.bloodDecalScale;
        this.setFadeOutStartTime(config.bloodDecalFadeOutAt);
        this.setRoll(this.random.nextFloat() * Mth.PI);
        this.setLifetime(config.bloodDecalLifetime);
    }

    @Override
    protected void updateColor(float lifetimeLeft) {
        int color = BloodDripParticle.getParticleColor();
        this.rCol = lifetimeLeft * ARGB.redFloat(color);
        this.gCol = lifetimeLeft * ARGB.greenFloat(color);
        this.bCol = lifetimeLeft * ARGB.blueFloat(color);
    }

    public static final class Provider implements ParticleProvider<SimpleDecalParticleOptions> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleDecalParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodDecalParticle particle = new BloodDecalParticle(level, options.attachDirection(), x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
