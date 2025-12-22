package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.PainStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.QueuedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStats;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class HealthContainer {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, Limb.CODEC).fieldOf("bodyParts").forGetter(t -> t.limbs),
            Codecs.collection(QueuedStatusEffect.CODEC, list -> (Queue<QueuedStatusEffect>) new PriorityQueue<>(list), ArrayList::new).optionalFieldOf("effectQueue", new PriorityQueue<>()).forGetter(t -> t.effectQueue),
            Codec.BOOL.optionalFieldOf("invalidated", false).forGetter(t -> t.invalidated)
    ).apply(instance, HealthContainer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealthContainer> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final HealthContainerDefinition definition;
    private final Map<String, Limb> limbs;
    private final Queue<QueuedStatusEffect> effectQueue;
    private boolean invalidated;

    public HealthContainer(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("Holder must be an instance of LivingEntity");
        }
        this.definition = MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity).orElse(null);
        this.effectQueue = new PriorityQueue<>();
        ImmutableMap.Builder<String, Limb> builder = ImmutableMap.builder();
        if (this.definition != null) {
            this.definition.limbConfiguration().buildLimbInstances(builder::put);
            this.limbs = builder.build();
        } else {
            this.limbs = Collections.emptyMap();
        }
    }

    private HealthContainer(HealthContainerDefinition definition, Map<String, Limb> limbs, Queue<QueuedStatusEffect> effectQueue, boolean invalidated) {
        this.definition = definition;
        this.limbs = limbs;
        this.effectQueue = new PriorityQueue<>(effectQueue);
        this.invalidated = invalidated;
    }

    public void tick(LivingEntity entity) {
        if (this.invalidated) {
            this.clearBoundData(entity);
            return;
        }

        float previousHealth = this.getHealth();
        this.tickEffectQueue(entity);
        this.tickStatusEffectCheck(entity, 20, false);
        for (Limb limb : this.limbs.values()) {
            limb.tick(this, entity);
        }
        float health = this.getHealth();
        if (health != previousHealth) {
            updateHealth(entity);
        }
    }

    public void clearBoundData(LivingEntity entity) {
        for (Limb part : this.limbs.values()) {
            StatusEffectMap map = part.getStatusEffects();
            if (!map.isEmpty())
                map.removeAll(StatusEffectSubmitter.NOOP, this, entity, part);
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
        return this.definition == null || this.limbs.isEmpty() || this.invalidated;
    }

    public HealthContainerDefinition getDefinition() {
        return definition;
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
        return this.definition.getRootLimbCode();
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

    public boolean hasMatchingStatusEffect(TagKey<StatusEffectType<?>> tag) {
        return this.getStatusEffectStream().anyMatch(effect -> effect.getType().is(tag));
    }

    public boolean removeMatchingStatusEffects(TagKey<StatusEffectType<?>> tag, LivingEntity entity) {
        boolean modified = false;
        for (Limb part : this.limbs.values()) {
            modified |= part.getStatusEffects().removeMatching(StatusEffectSubmitter.NOOP, tag, this, entity, part);
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
        for (Limb limb : this.limbs.values()) {
            if (limb.isVital() && limb.isDead()) {
                return true;
            }
        }
        return false;
    }

    public void iterateHitboxes(BiConsumer<BodyPartHitbox, Limb> consumer) {
        this.iterateHitboxes((hb, p) -> true, consumer);
    }

    public void iterateHitboxes(BiPredicate<BodyPartHitbox, Limb> filter, BiConsumer<BodyPartHitbox, Limb> consumer) {
        for (BodyPartHitbox hitbox : this.definition.getHitboxes()) {
            Limb part = this.limbs.get(hitbox.getOwner());
            if (part == null)
                continue;
            if (filter.test(hitbox, part)) {
                consumer.accept(hitbox, part);
            }
        }
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
        this.tickStatusEffectCheck(entity, 0, true);
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

            StatusEffectHelper.addEffect(map, entity, part, effect.data().copy());
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
            StatusEffectHelper.addEffect(this.getGlobalStatusEffects(), entity, null, painDelay, new PainStatusEffect(-1));
        }
    }

    private void hurtInternal(DamageContext context, float amount, Limb limb, Consumer<Limb> onLimbLoss) {
        float damage = Math.min(limb.getHealth(), limb.getScaledDamage(amount));
        float leftover = amount - damage;
        boolean wasDead = limb.isDead();
        LivingEntity entity = context.getEntity();
        limb.hurt(damage);
        if (!limb.isVital() && limb.isDead() != wasDead) {
            StatisticTracker.incrementOptional(entity, MedSystemStats.LIMBS_LOST);
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

    public static final class SyncHandler implements AttachmentSyncHandler<HealthContainer> {

        @Override
        public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
            return true;
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, HealthContainer container, boolean b) {
            STREAM_CODEC.encode(registryFriendlyByteBuf, container);
        }

        @Override
        public @org.jetbrains.annotations.Nullable HealthContainer read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @org.jetbrains.annotations.Nullable HealthContainer container) {
            HealthContainer deserialized = STREAM_CODEC.decode(registryFriendlyByteBuf);
            MedicalSystemClient.onHealthContainerUpdated(iAttachmentHolder, deserialized);
            return deserialized;
        }
    }
}
