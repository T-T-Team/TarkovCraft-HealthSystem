package tnt.tarkovcraft.medsystem.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import tnt.tarkovcraft.core.common.particle.SimpleDecalParticleOptions;
import tnt.tarkovcraft.core.util.helper.ARGB;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;

public final class BloodDripParticle extends TextureSheetParticle {

    public BloodDripParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
        this.level.addAlwaysVisibleParticle(options, true, x, y, z, 0, 0, 0);
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
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodDripParticle particle = new BloodDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
