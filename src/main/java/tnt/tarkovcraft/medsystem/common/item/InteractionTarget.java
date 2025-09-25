package tnt.tarkovcraft.medsystem.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record InteractionTarget(boolean self, int entityId, String limbCode) {

    public static final Codec<InteractionTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("self", true).forGetter(InteractionTarget::self),
            Codec.INT.optionalFieldOf("entityId", 0).forGetter(InteractionTarget::entityId),
            Codec.STRING.fieldOf("limb").forGetter(InteractionTarget::limbCode)
    ).apply(instance, InteractionTarget::new));
    public static final StreamCodec<ByteBuf, InteractionTarget> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, InteractionTarget::self,
            ByteBufCodecs.INT, InteractionTarget::entityId,
            ByteBufCodecs.STRING_UTF8, InteractionTarget::limbCode,
            InteractionTarget::new
    );

    public LivingEntity getTargetLivingEntity(LivingEntity healer) {
        if (!this.self) {
            Level level = healer.level();
            Entity entity = level.getEntity(this.entityId);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }

        return healer;
    }

    public static final class Mutable {

        private boolean self;
        private int entityId;
        private String limbCode;

        public Mutable(boolean self, int entityId, String limbCode) {
            this.self = self;
            this.entityId = entityId;
            this.limbCode = limbCode;
        }

        public boolean isSelf() {
            return this.self;
        }

        public void setSelf(boolean self) {
            this.self = self;
        }

        public int getEntityId() {
            return this.entityId;
        }

        public void setEntityId(int entityId) {
            this.entityId = entityId;
        }

        public String getLimbCode() {
            return limbCode;
        }

        public void setLimbCode(String limbCode) {
            this.limbCode = limbCode;
        }

        public InteractionTarget toImmutable() {
            return new InteractionTarget(this.self, this.entityId, Objects.requireNonNull(this.limbCode));
        }
    }
}
