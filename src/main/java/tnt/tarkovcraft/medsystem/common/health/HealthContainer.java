package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.client.ShaderHelper;
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
import java.util.stream.Stream;

public final class HealthContainer {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, Limb.CODEC).fieldOf("bodyParts").forGetter(t -> t.limbs),
            StatusEffectMap.CODEC.fieldOf("effects").forGetter(t -> t.statusEffects),
            Codecs.collection(QueuedStatusEffect.CODEC, list -> (Queue<QueuedStatusEffect>) new PriorityQueue<>(list), ArrayList::new).optionalFieldOf("effectQueue", new PriorityQueue<>()).forGetter(t -> t.effectQueue),
            Codec.BOOL.optionalFieldOf("invalidated", false).forGetter(t -> t.invalidated)
    ).apply(instance, HealthContainer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealthContainer> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final HealthContainerDefinition definition;
    private final Map<String, Limb> limbs;
    private final Map<Limb, Limb> limbLinks;
    private final List<Limb> vitalLimbs;
    private final String rootLimbCode;
    private final StatusEffectMap statusEffects;
    private final Queue<QueuedStatusEffect> effectQueue;
    @Deprecated
    private DamageContext activeDamageContext;
    private boolean invalidated;

    public HealthContainer(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("Holder must be an instance of LivingEntity");
        }
        this.definition = MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity).orElse(null);
        this.statusEffects = new StatusEffectMap();
        this.effectQueue = new PriorityQueue<>();
        ImmutableMap.Builder<String, Limb> builder = ImmutableMap.builder();
        if (this.definition != null) {
            for (Map.Entry<String, LimbDefinition> entry : this.definition.getLimbDefinitionMap().entrySet()) {
                String key = entry.getKey();
                LimbDefinition partDefinition = entry.getValue();
                builder.put(key, partDefinition.createLimbInstance(key));
            }
            this.limbs = builder.build();
            this.limbLinks = new IdentityHashMap<>();
            this.vitalLimbs = new ArrayList<>();
            this.rootLimbCode = this.resolveBodyParts(this.definition, this.limbLinks, this.vitalLimbs);
        } else {
            this.limbs = Collections.emptyMap();
            this.limbLinks = Collections.emptyMap();
            this.vitalLimbs = Collections.emptyList();
            this.rootLimbCode = "";
        }
    }

    private HealthContainer(HealthContainerDefinition definition, Map<String, Limb> limbs, StatusEffectMap statusEffects, Queue<QueuedStatusEffect> effectQueue, boolean invalidated) {
        this.definition = definition;
        this.limbs = limbs;
        this.limbLinks = new IdentityHashMap<>();
        this.vitalLimbs = new ArrayList<>();
        this.rootLimbCode = this.resolveBodyParts(this.definition, this.limbLinks, this.vitalLimbs);
        this.statusEffects = statusEffects;
        this.effectQueue = new PriorityQueue<>(effectQueue);
        this.invalidated = invalidated;
    }

    public void tick(LivingEntity entity) {
        if (this.invalidated) {
            this.clearBoundData(entity);
        } else {
            float previousHealth = this.getHealth();
            this.tickEffectQueue(entity);
            this.tickStatusEffectCheck(entity, 20, false);
            this.statusEffects.tick(this, entity, null);
            for (Limb part : this.limbs.values()) {
                part.tick(this, entity);
            }
            float health = this.getHealth();
            if (health != previousHealth) {
                updateHealth(entity);
            }
        }
    }

    public void clearBoundData(LivingEntity entity) {
        if (!this.statusEffects.isEmpty()) {
            this.statusEffects.removeAll(StatusEffectSubmitter.NOOP, this, entity, null);
        }

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
        return this.statusEffects;
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

    public Limb getLimb(@Nullable String code) {
        return this.limbs.get(code != null ? code : this.rootLimbCode);
    }

    public Limb getRootLimb() {
        return this.getLimb(null);
    }

    public Collection<Limb> getLimbs() {
        return this.limbs.values();
    }

    public Stream<Limb> getLimbsAsStream() {
        return this.limbs.values().stream();
    }

    public Stream<StatusEffect> getStatusEffectStream() {
        return Stream.concat(
                this.statusEffects.getEffectsStream(),
                this.limbs.values().stream().flatMap(part -> part.getStatusEffects().getEffectsStream())
        );
    }

    public boolean hasMatchingStatusEffect(TagKey<StatusEffectType<?>> tag) {
        return this.getStatusEffectStream().anyMatch(effect -> effect.getType().is(tag));
    }

    public boolean removeMatchingStatusEffects(TagKey<StatusEffectType<?>> tag, LivingEntity entity) {
        // could be probably used to add post effects
        boolean modified = this.statusEffects.removeMatching(StatusEffectSubmitter.NOOP, tag, this, entity, null);
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

    public void hurt(DamageContext context, Map<Limb, Float> distributedDamage, @Nullable SideEffectHolder effects, Consumer<Limb> onLimbDeath) {
        for (Map.Entry<Limb, Float> entry : distributedDamage.entrySet()) {
            Limb limb = entry.getKey();
            float amount = entry.getValue();
            this.hurt(context, amount, limb, onLimbDeath);
            if (effects != null) {
                effects.applyFromDamage(context.getEntity(), context.getSource(), this, limb);
            }
        }
    }

    public void hurt(DamageContext context, float amount, Limb part, Consumer<Limb> onLimbLoss) {
        float damage = Math.min(part.getHealth(), amount * part.getDamageScale());
        float leftover = amount - damage;
        boolean wasDead = part.isDead();
        LivingEntity entity = context.getEntity();
        DamageSource source = context.getSource();
        part.hurt(damage);
        part.trigger(this, entity, source);
        if (!part.isVital() && part.isDead() != wasDead) {
            StatisticTracker.incrementOptional(entity, MedSystemStats.LIMBS_LOST);
            onLimbLoss.accept(part);
        }
        // no need to redistribute damage from vital parts
        if (!part.isVital() && leftover > 0) {
            Limb parent = this.limbLinks.get(part);
            if (parent != null) {
                float scale = parent.getParentDamageScale();
                this.hurt(context, leftover * scale, parent, onLimbLoss);
            }
        }
    }

    public boolean canHeal(@Nullable Limb part, boolean allowDead) {
        if (part != null) {
            return (part.isDead() && allowDead) || part.getMaxHealAmount() > 0;
        }
        return this.getPartToHeal(allowDead) != null;
    }

    public float heal(LivingEntity entity, float amount, @Nullable Limb targetPart) {
        if (targetPart != null && !targetPart.isDead()) {
            // Heal specific body part only
            float healAmount = Math.min(amount, targetPart.getMaxHealAmount());
            targetPart.heal(healAmount);
            targetPart.trigger(this, entity, null);
            return amount - healAmount;
        } else {
            // Heal body parts, prioritize vitals, then according to health
            Limb part;
            while (amount > 0.0F && (part = this.getPartToHeal(false)) != null) {
                float healAmount = Math.min(amount, part.getMaxHealAmount());
                part.heal(healAmount);
                part.trigger(this, entity, null);
                amount -= healAmount;
            }
        }
        return amount;
    }

    public void setDamageContext(DamageContext damageContext) {
        if (this.activeDamageContext == null || this.activeDamageContext.getId() != damageContext.getId())
            this.activeDamageContext = damageContext;
    }

    public void clearDamageContext() {
        this.activeDamageContext = null;
    }

    public DamageContext getDamageContext() {
        return this.activeDamageContext;
    }

    public boolean shouldDie() {
        float health = 0.0F;
        for (Limb part : this.limbs.values()) {
            health += part.getHealth();
            if (part.shouldOwnerDie()) {
                return true;
            }
        }
        return health <= 0.0F;
    }

    public void acceptHitboxes(BiConsumer<BodyPartHitbox, Limb> consumer) {
        this.acceptHitboxes((hb, p) -> true, consumer);
    }

    public void acceptHitboxes(BiPredicate<BodyPartHitbox, Limb> filter, BiConsumer<BodyPartHitbox, Limb> consumer) {
        for (BodyPartHitbox hitbox : this.definition.getHitboxes()) {
            Limb part = this.limbs.get(hitbox.getOwner());
            if (part == null)
                continue;
            if (filter.test(hitbox, part)) {
                consumer.accept(hitbox, part);
            }
        }
    }

    public Limb getPartToHeal(boolean allowDead) {
        Limb targetPart = null;
        float targetPercentage = 1.0F;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.prioritizeVitalHealing) {
            for (Limb vitalPart : this.vitalLimbs) {
                if (vitalPart.isDead() && !allowDead)
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
            if (part.isDead() && !allowDead)
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

    private String resolveBodyParts(HealthContainerDefinition definition, Map<Limb, Limb> links, List<Limb> vitalParts) {
        String root = null;
        for (Map.Entry<String, LimbDefinition> health : definition.getLimbDefinitionMap().entrySet()) {
            String part = health.getKey();
            String parent = health.getValue().getParent();
            Limb limb = this.limbs.get(part);
            if (parent == null) {
                root = part;
            } else {
                links.put(limb, this.limbs.get(parent));
            }
            LimbDefinition healthDef = health.getValue();
            if (healthDef.isVital()) {
                vitalParts.add(limb);
            }
            limb.setDefinition(healthDef);
        }
        return root;
    }

    private void tickEffectQueue(LivingEntity entity) {
        QueuedStatusEffect effect;
        long gameTime = entity.level().getGameTime();
        boolean modified = false;
        while ((effect = this.effectQueue.peek()) != null && effect.isReady(gameTime)) {
            this.effectQueue.poll();
            String limbCode = effect.limb();

            StatusEffectMap map = this.statusEffects;
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
        if ((forcedTick || time % 20 == 0) && !this.statusEffects.hasEffect(MedSystemStatusEffects.PAIN) && HealthSystem.isInPain(entity)) {
            // delay cannot be bigger than 20 as otherwise it will schedule multiple pain effects
            StatusEffectHelper.addEffect(this.statusEffects, entity, null, painDelay, new PainStatusEffect(-1));
        }
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
        public @org.jetbrains.annotations.Nullable HealthContainer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @org.jetbrains.annotations.Nullable HealthContainer previousValue) {
            HealthContainer container = STREAM_CODEC.decode(buf);
            ShaderHelper.notifyUpdate(container, holder);
            return container;
        }
    }
}
