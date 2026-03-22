package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.Queues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.PainStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.QueuedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class HealthContainer {

    public static final MapCodec<HealthContainer> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entity_type").forGetter(t -> Optional.ofNullable(t.type)),
            Codec.unboundedMap(Codec.STRING, Limb.CODEC).optionalFieldOf("limbs", Collections.emptyMap()).forGetter(t -> t.limbs),
            Codecs.collection(QueuedStatusEffect.CODEC, list -> (Queue<QueuedStatusEffect>) new PriorityQueue<>(list), ArrayList::new).optionalFieldOf("effect_queue", new PriorityQueue<>()).forGetter(t -> t.effectQueue),
            Codec.BOOL.optionalFieldOf("invalidated", false).forGetter(t -> t.invalidated)
    ).apply(instance, HealthContainer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealthContainer> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(MAP_CODEC.codec());

    private final EntityType<?> type;
    private final Map<String, Limb> limbs;
    private final Queue<QueuedStatusEffect> effectQueue;

    private boolean invalidated;
    private final Cached<HealthContainerDefinition> definition;

    public HealthContainer(EntityType<?> type, HealthContainerDefinition definition) {
        this(
                Optional.of(type),
                definition != null ? definition.limbConfiguration().buildLimbInstances() : Collections.emptyMap(),
                Queues.newPriorityQueue(),
                false
        );
    }

    private HealthContainer(Optional<EntityType<?>> type, Map<String, Limb> limbs, Queue<QueuedStatusEffect> effectQueue, boolean invalidated) {
        this.type = type.orElse(null);
        this.limbs = limbs;
        this.definition = Cached.create(this::loadDefinition);
        this.effectQueue = new PriorityQueue<>(effectQueue);
        this.invalidated = invalidated;
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

        float previousHealth = this.getHealth();
        this.tickEffectQueue(entity);
        this.tickStatusEffectCheck(entity, 20, false);
        StatusEffectContext.MutableContext statusEffectContext = new StatusEffectContext.MutableContext(this, entity);
        for (Limb limb : this.limbs.values()) {
            statusEffectContext.withLimb(limb);
            limb.tick(statusEffectContext);
        }
        float health = this.getHealth();
        if (health != previousHealth) {
            updateHealth(entity);
        }
    }

    public void clearBoundData(LivingEntity entity) {
        for (Limb limb : this.limbs.values()) {
            StatusEffectMap map = limb.getStatusEffects();
            StatusEffectContext ctx = StatusEffectContext.of(this, entity, StatusEffectSubmitter.NOOP, limb);
            if (!map.isEmpty())
                map.removeAll(ctx);
        }
        this.effectQueue.clear();
    }

    public void scheduleStatusEffect(LivingEntity entity, int delay, @Nullable Limb part, StatusEffect effect) {
        String partId = part != null ? part.getLimbCode() : "";
        Level level = entity.level();
        long target = level.getGameTime() + delay;
        QueuedStatusEffect queuedStatusEffect = new QueuedStatusEffect(target, partId, effect);
        this.effectQueue.offer(queuedStatusEffect);
    }

    public StatusEffectMap getGlobalStatusEffects() {
        return this.getRootLimb().getStatusEffects();
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public boolean isInvalid() {
        return this.type == null || this.getDefinition() == null || this.limbs.isEmpty() || this.invalidated;
    }

    public HealthContainerDefinition getDefinition() {
        return this.definition.get();
    }

    public boolean hasLimb(String code) {
        return this.limbs.containsKey(code);
    }

    public Limb getLimbByCode(@Nullable String code) {
        return this.limbs.get(code != null ? code : this.getRootLimbCode());
    }

    public Limb getRootLimb() {
        return this.getLimbByCode(null);
    }

    public String getRootLimbCode() {
        return this.getDefinition().getRootLimbCode();
    }

    public Stream<Limb> getLimbsAsStream() {
        return this.limbs.values().stream();
    }

    public Collection<Limb> getVitalLimbs() {
        return this.getLimbsAsStream().filter(Limb::isVital).toList();
    }

    public Stream<StatusEffect> getStatusEffectStream() {
        return this.limbs.values().stream()
                .flatMap(limb -> limb.getStatusEffects().getEffectsStream());
    }

    public boolean removeMatchingStatusEffects(TagKey<StatusEffectType<?>> tag, LivingEntity entity) {
        boolean modified = false;
        for (Limb limb : this.limbs.values()) {
            StatusEffectContext ctx = StatusEffectContext.of(this, entity, StatusEffectSubmitter.NOOP, limb);
            modified |= limb.getStatusEffects().removeMatching(tag, ctx);
        }
        return modified;
    }

    public float getHealth() {
        float health = 0.0F;
        for (Limb limb : limbs.values()) {
            if (limb.shouldOwnerDie()) {
                return 0.0F;
            }
            health += limb.getHealth();
        }
        return health;
    }

    public float getMaxHealth() {
        float maxHealth = 0.0F;
        for (Limb limb : limbs.values()) {
            maxHealth += limb.getMaxHealth();
        }
        return maxHealth;
    }

    public float getOriginalMaxHealth() {
        float maxHealth = 0.0F;
        for (Limb limb : limbs.values()) {
            maxHealth += limb.getOriginalMaxHealth();
        }
        return maxHealth;
    }

    public void updateHealth(LivingEntity entity) {
        float playerMaxHealth = entity.getMaxHealth();
        float containerMaxHealth = this.getMaxHealth();
        float originalContainerMaxHealth = this.getOriginalMaxHealth();
        if (playerMaxHealth != containerMaxHealth) {
            if (playerMaxHealth == originalContainerMaxHealth) {
                for (Limb limb : limbs.values()) {
                    limb.setMaxHealth(limb.getOriginalMaxHealth());
                }
            } else {
                double diff = playerMaxHealth - containerMaxHealth;
                int parts = this.limbs.size();
                double perPart = diff / parts;
                for (Limb limb : this.limbs.values()) {
                    float newMaxHealth = (float) (limb.getMaxHealth() + perPart);
                    limb.setMaxHealth(Math.max(newMaxHealth, 1.0F));
                }
            }
        }
        float health = this.getHealth();
        entity.setHealth(health);
    }

    public void hurt(DamageContext context, Map<Limb, Float> distributedDamage, Consumer<Limb> onLimbDeath) {
        for (Map.Entry<Limb, Float> entry : distributedDamage.entrySet()) {
            Limb limb = entry.getKey();
            float amount = entry.getValue();
            this.hurtInternal(context, amount, limb, onLimbDeath);
        }
    }

    public boolean canHeal() {
        return this.getPartToHeal() != null;
    }

    public float heal(float amount, @Nullable Limb targetPart) {
        if (targetPart != null && !targetPart.isDead()) {
            // Heal specific body part only
            float healAmount = Math.min(amount, targetPart.getMaxHealAmount());
            targetPart.heal(healAmount);
            return amount - healAmount;
        } else {
            // Heal body parts, prioritize vitals, then according to health
            Limb part;
            while (amount > 0.0F && (part = this.getPartToHeal()) != null) {
                float healAmount = Math.min(amount, part.getMaxHealAmount());
                part.heal(healAmount);
                amount -= healAmount;
            }
        }
        return amount;
    }

    public boolean shouldDie() {
        return this.getLimbsAsStream()
                .anyMatch(limb -> limb.isVital() && limb.isDead());
    }

    public Limb getPartToHeal() {
        Limb targetPart = null;
        float targetPercentage = 1.0F;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.prioritizeVitalHealing) {
            for (Limb vitalPart : this.getVitalLimbs()) {
                if (vitalPart.isDead())
                    continue;
                float percentage = vitalPart.getHealthPercent();
                if (percentage < config.vitalBodyPartHealthTrigger && percentage < targetPercentage) {
                    targetPercentage = percentage;
                    targetPart = vitalPart;
                }
            }
        }
        if (targetPart != null) {
            return targetPart;
        }
        Limb target = null;
        for (Limb part : this.limbs.values()) {
            if (part.isDead())
                continue;
            float percentage = part.getHealthPercent();
            if (percentage < 1.0F && percentage < targetPercentage) {
                target = part;
                targetPercentage = percentage;
            }
        }
        return target;
    }

    public void markStatusEffectAdded(LivingEntity entity) {
        this.tickStatusEffectCheck(entity, 5, true);
    }

    private void tickEffectQueue(LivingEntity entity) {
        QueuedStatusEffect effect;
        long gameTime = entity.level().getGameTime();
        boolean modified = false;
        while ((effect = this.effectQueue.peek()) != null && effect.isReady(gameTime)) {
            this.effectQueue.poll();
            String limbCode = effect.limb();

            StatusEffectMap map = this.getGlobalStatusEffects();
            Limb part = null;
            if (!limbCode.isBlank()) {
                part = this.limbs.get(limbCode);
                if (part == null)
                    continue;
                map = part.getStatusEffects();
            }

            StatusEffectHelper.addImmediateEffect(map, entity, part, effect.data().copy());
            modified = true;
        }
        if (modified) {
            HealthSystem.synchronizeEntity(entity);
        }
    }

    private void tickStatusEffectCheck(LivingEntity entity, int painDelay, boolean forcedTick) {
        long time = entity.level().getGameTime();
        if ((forcedTick || time % 20 == 0) && !this.getGlobalStatusEffects().hasEffect(MedSystemStatusEffects.PAIN) && HealthSystem.isInPain(entity)) {
            // delay cannot be bigger than 20 as otherwise it will schedule multiple pain effects
            StatusEffectHelper.addGlobalEffect(this.getGlobalStatusEffects(), entity, painDelay, PainStatusEffect.infinite());
        }
    }

    private void hurtInternal(DamageContext context, float amount, Limb limb, Consumer<Limb> onLimbLoss) {
        float damage = Math.min(limb.getHealth(), limb.getScaledDamage(amount));
        float leftover = amount - damage;
        boolean wasDead = limb.isDead();
        limb.hurt(damage);
        if (!limb.isVital() && limb.isDead() != wasDead) {
            onLimbLoss.accept(limb);
        }
        // no need to redistribute damage from vital parts
        if (!limb.isVital() && leftover > 0) {
            Collection<Limb> aliveLimbs = this.getLimbsAsStream().filter(Limb::isAlive).toList();
            if (aliveLimbs.isEmpty()) {
                return;
            }
            DamageSource source = context.getSource();
            float pooledDamage = (source.is(DamageTypeTags.BYPASSES_ARMOR) ? leftover : limb.getScaledTransferDamage(leftover)) / aliveLimbs.size();
            for (Limb liveLimb : aliveLimbs) {
                this.hurtInternal(context, pooledDamage, liveLimb, onLimbLoss);
            }
        }
    }

    private HealthContainerDefinition loadDefinition() {
        return HealthSystem.getHealthContainerDefinition(this.type);
    }

    public static final class SyncHandler implements AttachmentSyncHandler<HealthContainer> {

        @Override
        public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
            return true;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, HealthContainer attachment, boolean initialSync) {
            STREAM_CODEC.encode(buf, attachment);
        }

        @Override
        public @Nullable HealthContainer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable HealthContainer previousValue) {
            HealthContainer container = STREAM_CODEC.decode(buf);
            MedicalSystemClient.onHealthContainerUpdated(holder, container);
            return container;
        }
    }
}
