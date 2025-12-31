package tnt.tarkovcraft.medsystem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.particle.SimpleDecalParticleOptions;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;

public final class BloodDripParticle extends SingleQuadParticle {

    public BloodDripParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        int color = getParticleColor();
        this.rCol = ARGB.redFloat(color);
        this.gCol = ARGB.greenFloat(color);
        this.bCol = ARGB.blueFloat(color);
        this.gravity = 0.4F;
        this.quadSize = 0.075F;
        this.setLifetime(300);
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public void move(double x, double y, double z) {
        if (!this.stoppedByCollision) {
            if (this.hasPhysics && (x != 0.0 || y != 0.0 || z != 0.0) && x * x + y * y + z * z < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
                BlockHitResult result = this.level.clip(new ClipContext(this.getPos(), this.getPos().add(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                if (result.getType() != BlockHitResult.Type.MISS) {
                    Vec3 hit = result.getLocation();
                    Direction direction = result.getDirection();
                    this.onCollision(hit.x, hit.y, hit.z, direction);
                    return;
                }
            }
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setLocationFromBoundingbox();
        }
    }

    private void onCollision(double x, double y, double z, Direction direction) {
        SimpleDecalParticleOptions options = new SimpleDecalParticleOptions(MedSystemParticleTypes.BLOOD_DECAL, direction);
        this.level.addParticle(options, true, true, x, y, z, 0, 0, 0);
        this.remove();
    }

    public static int getParticleColor() {
        return Integer.decode(MedicalSystemClient.getConfig().bloodDecals.bloodDecalColor);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new BloodDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet.get(random));
        }
    }
}
