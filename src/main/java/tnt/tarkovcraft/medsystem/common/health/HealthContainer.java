package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.core.api.AttachmentSyncCallbackListener;
import tnt.tarkovcraft.core.api.client.SynchronizableScreen;
import tnt.tarkovcraft.core.client.TarkovCraftCoreClient;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.Optional;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class HealthContainer {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entity_type").forGetter(t -> Optional.ofNullable(t.type)),
            LimbContainer.CODEC.optionalFieldOf("limb_container", LimbContainer.EMPTY).forGetter(t -> t.limbContainer),
            StatusEffectQueue.CODEC.optionalFieldOf("effect_queue", StatusEffectQueue.createEmpty()).forGetter(t -> t.effectQueue),
            Codec.BOOL.optionalFieldOf("invalidated", false).forGetter(t -> t.invalidated)
    ).apply(instance, HealthContainer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealthContainer> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final SynchronizableScreen.DataSource HEALTH = new SynchronizableScreen.DataSource(MedicalSystem.createIdentifier("health"));

    private final EntityType<?> type;
    private final LimbContainer limbContainer;
    private final StatusEffectQueue effectQueue;

    private boolean invalidated;
    private boolean changed;
    private final Cached<HealthContainerDefinition> definition;

    public HealthContainer(EntityType<?> type, @Nullable HealthContainerDefinition definition) {
        this(
                Optional.of(type),
                LimbContainer.create(definition),
                StatusEffectQueue.createEmpty(),
                false
        );
    }

    private HealthContainer(Optional<EntityType<?>> type, LimbContainer limbContainer, StatusEffectQueue effectQueue, boolean invalidated) {
        this.type = type.orElse(null);
        this.limbContainer = limbContainer;
        this.effectQueue = effectQueue;
        this.invalidated = invalidated;
        this.definition = Cached.create(this::loadDefinition);
    }

    public static HealthContainer invalid(IAttachmentHolder holder) {
        if (holder instanceof Entity entity) {
            return new HealthContainer(entity.getType(), null);
        }
        return null;
    }

    public static void detach(LivingEntity entity) {
        entity.removeData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static @Nullable HealthContainer getAttached(LivingEntity entity) {
        return entity.getExistingDataOrNull(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static @Nullable HealthContainer getAttachedValid(LivingEntity entity) {
        HealthContainer container = getAttached(entity);
        if (container == null || container.isInvalid()) {
            return null;
        }
        return container;
    }

    public void tick(LivingEntity entity) {
        if (this.invalidated) {
            this.clearBoundData(entity);
            return;
        }

        this.effectQueue.update(this, entity);
        StatusEffectMap globalEffects = this.getGlobalStatusEffects();
        globalEffects.painEffectTick(entity, 20, false);
        StatusEffectContext.MutableContext statusEffectContext = new StatusEffectContext.MutableContext(this, entity);
        this.limbContainer.update(statusEffectContext);

        // synchronization
        if (this.changed) {
            this.changed = false;
            HealthSystem.synchronizeEntity(entity);
        }
    }

    public void clearBoundData(LivingEntity entity) {
        this.limbContainer.clearData(this, entity);
        this.effectQueue.clear();
    }

    public StatusEffectMap getGlobalStatusEffects() {
        return this.getRootLimb().getStatusEffects();
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public boolean isInvalid() {
        return this.type == null || this.getDefinition() == null || this.limbContainer.isEmpty() || this.invalidated;
    }

    public HealthContainerDefinition getDefinition() {
        return this.definition.get();
    }

    public LimbContainer getLimbContainer() {
        return limbContainer;
    }

    public boolean hasLimb(String code) {
        return this.limbContainer.hasLimb(code);
    }

    public Limb getLimbByCode(@Nullable String code) {
        return this.limbContainer.getLimb(code);
    }

    public Limb getRootLimb() {
        return this.limbContainer.getRootLimb();
    }

    public String getRootLimbCode() {
        return this.getDefinition().getRootLimbCode();
    }

    public StatusEffectQueue getEffectQueue() {
        return effectQueue;
    }

    public boolean isDead() {
        return this.getLimbContainer().hasLimb(limb -> limb.isVital() && limb.isDead());
    }

    public void setChanged() {
        this.changed = true;
    }

    private HealthContainerDefinition loadDefinition() {
        return HealthSystem.getHealthContainerDefinition(this.type);
    }

    public static final class SyncHandler implements AttachmentSyncHandler<HealthContainer>, AttachmentSyncCallbackListener<HealthContainer> {

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, HealthContainer container, boolean b) {
            STREAM_CODEC.encode(registryFriendlyByteBuf, container);
        }

        public @Nullable HealthContainer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable HealthContainer previousValue) {
            return STREAM_CODEC.decode(buf);
        }

        @Override
        public void onDataSynced(IAttachmentHolder holder, AttachmentType<HealthContainer> attachmentType, HealthContainer attachment) {
            TarkovCraftCoreClient.synchronizeCurrentScreen(HEALTH);
        }
    }
}
