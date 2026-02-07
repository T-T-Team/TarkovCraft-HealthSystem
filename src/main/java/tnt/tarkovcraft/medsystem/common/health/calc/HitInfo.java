package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class HitInfo {

    private final EntityHitboxContainer.LimbHitboxDefinition hitbox;
    private final Limb limb;
    private final AABB aabb;
    private final Vec3 entryPoint;
    private final Map<Identifier, Object> metadata;

    private HitInfo(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb, AABB aabb, Vec3 entryPoint, Map<Identifier, Object> metadata) {
        this.hitbox = hitbox;
        this.limb = limb;
        this.aabb = aabb;
        this.entryPoint = entryPoint;
        this.metadata = metadata;
    }

    public static HitInfo.Mutable createMutable(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb) {
        return new HitInfo.Mutable(hitbox, limb);
    }

    public static HitInfo.Mutable createMutable(LimbHitbox hitbox) {
        return createMutable(hitbox.definition(), hitbox.limb());
    }

    public HitInfo(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb, AABB aabb) {
        this(hitbox, limb, aabb, aabb.getCenter(), null);
    }

    public static HitInfo create(LimbHitbox hitbox, AABB aabb, Vec3 entryPoint) {
        return new HitInfo(hitbox.definition(), hitbox.limb(), aabb, entryPoint, null);
    }

    public static HitInfo create(LimbHitbox hitbox, AABB aabb) {
        return create(hitbox, aabb, aabb.getCenter());
    }

    public static HitInfo create(LimbHitbox hitbox, LivingEntity entity) {
        AABB aabb = hitbox.worldspaceAABB(entity);
        return create(hitbox, aabb);
    }

    public EntityHitboxContainer.LimbHitboxDefinition hitbox() {
        return this.hitbox;
    }

    public Limb limb() {
        return this.limb;
    }

    public AABB aabb() {
        return this.aabb;
    }

    public Vec3 entryPoint() {
        return this.entryPoint;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getMetadataValue(Identifier key) {
        if (this.metadata == null)
            return null;
        return (T) this.metadata.get(key);
    }

    public boolean hasMetadataValue(Identifier key) {
        return this.metadata != null && this.metadata.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HitInfo hitInfo)) return false;
        return Objects.equals(this.limb, hitInfo.limb);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.limb);
    }

    public static final class Mutable {

        private final EntityHitboxContainer.LimbHitboxDefinition hitbox;
        private final Limb limb;
        private AABB aabb;
        private Vec3 entryPoint;
        private Map<Identifier, Object> metadata;

        public Mutable(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb) {
            this.hitbox = hitbox;
            this.limb = limb;
        }

        public void setAABB(AABB aabb) {
            this.aabb = aabb;
        }

        public void setEntryPoint(Vec3 entryPoint) {
            this.entryPoint = entryPoint;
        }

        public void setMetadataParam(Identifier key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
        }

        public HitInfo toImmutable() {
            return new HitInfo(hitbox, limb, aabb, entryPoint, metadata);
        }
    }
}
