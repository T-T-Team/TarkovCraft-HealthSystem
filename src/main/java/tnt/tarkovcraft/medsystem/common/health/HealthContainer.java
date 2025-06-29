package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.core.network.Synchronizable;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStats;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class HealthContainer implements Synchronizable<HealthContainer> {

    public static final MapCodec<HealthContainer> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, BodyPart.CODEC).fieldOf("bodyParts").forGetter(t -> t.bodyParts),
            StatusEffectMap.CODEC.fieldOf("effects").forGetter(t -> t.statusEffects),
            Codec.BOOL.optionalFieldOf("invalidated", false).forGetter(t -> t.invalidated)
    ).apply(instance, HealthContainer::new));
    public static final Codec<HealthContainer> CODEC = MAP_CODEC.codec();

    private final HealthContainerDefinition definition;
    private final Map<String, BodyPart> bodyParts;
    private final Map<BodyPart, BodyPart> bodyPartLinks;
    private final List<BodyPart> vitalParts;
    private final String root;
    private final StatusEffectMap statusEffects;
    private DamageContext activeDamageContext;
    private boolean invalidated;

    public HealthContainer(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("Holder must be an instance of LivingEntity");
        }
        this.definition = MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity).orElse(null);
        this.statusEffects = new StatusEffectMap();
        ImmutableMap.Builder<String, BodyPart> builder = ImmutableMap.builder();
        if (this.definition != null) {
            for (Map.Entry<String, BodyPartDefinition> entry : this.definition.getBodyParts().entrySet()) {
                String key = entry.getKey();
                BodyPartDefinition partDefinition = entry.getValue();
                builder.put(key, partDefinition.createContainer(key));
            }
            this.bodyParts = builder.build();
            this.bodyPartLinks = new IdentityHashMap<>();
            this.vitalParts = new ArrayList<>();
            this.root = this.resolveBodyParts(this.definition, this.bodyPartLinks, this.vitalParts);
        } else {
            this.bodyParts = Collections.emptyMap();
            this.bodyPartLinks = Collections.emptyMap();
            this.vitalParts = Collections.emptyList();
            this.root = "";
        }
    }

    private HealthContainer(HealthContainerDefinition definition, Map<String, BodyPart> bodyParts, StatusEffectMap statusEffects, boolean invalidated) {
        this.definition = definition;
        this.bodyParts = bodyParts;
        this.bodyPartLinks = new IdentityHashMap<>();
        this.vitalParts = new ArrayList<>();
        this.root = this.resolveBodyParts(this.definition, this.bodyPartLinks, this.vitalParts);
        this.statusEffects = statusEffects;
        this.invalidated = invalidated;
    }

    public void tick(LivingEntity entity) {
        ContextImpl context = ContextImpl.of(
                MedicalSystemContextKeys.HEALTH_CONTAINER, this,
                ContextKeys.LIVING_ENTITY, entity
        );
        float previousHealth = this.getHealth();
        this.statusEffects.tick(context);
        for (BodyPart part : this.bodyParts.values()) {
            part.tick(context);
        }
        float health = this.getHealth();
        if (health != previousHealth) {
            updateHealth(entity);
        }
        if (this.invalidated) {
            this.clearBoundData(entity);
        }
    }

    public void clearBoundData(LivingEntity entity) {
        ContextImpl context = ContextImpl.of(
                MedicalSystemContextKeys.HEALTH_CONTAINER, this,
                ContextKeys.LIVING_ENTITY, entity
        );
        this.statusEffects.removeAll(context);
        for (BodyPart part : this.bodyParts.values()) {
            context.set(MedicalSystemContextKeys.BODY_PART, part);
            StatusEffectMap map = part.getStatusEffects();
            map.removeAll(context);
        }
    }

    public StatusEffectMap getGlobalStatusEffects() {
        return this.statusEffects;
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public boolean isInvalid() {
        return this.definition == null || this.bodyParts.isEmpty() || this.invalidated;
    }

    public HealthContainerDefinition getDefinition() {
        return definition;
    }

    public boolean hasBodyPart(String part) {
        return this.bodyParts.containsKey(part);
    }

    public BodyPart getBodyPart(@Nullable String name) {
        return this.bodyParts.get(name != null ? name : this.root);
    }

    public BodyPart getRootBodyPart() {
        return this.getBodyPart(null);
    }

    public Stream<BodyPart> getBodyPartStream() {
        return this.bodyParts.values().stream();
    }

    public Stream<StatusEffect> getStatusEffectStream() {
        return Stream.concat(
                this.statusEffects.getEffectsStream(),
                this.bodyParts.values().stream().flatMap(part -> part.getStatusEffects().getEffectsStream())
        );
    }

    public float getHealth() {
        float health = 0.0F;
        for (BodyPart bodyPart : bodyParts.values()) {
            if (bodyPart.shouldOwnerDie()) {
                return 0.0F;
            }
            health += bodyPart.getHealth();
        }
        return health;
    }

    public float getMaxHealth() {
        float maxHealth = 0.0F;
        for (BodyPart bodyPart : bodyParts.values()) {
            maxHealth += bodyPart.getMaxHealth();
        }
        return maxHealth;
    }

    public void updateHealth(LivingEntity entity) {
        float playerMaxHealth = entity.getMaxHealth();
        float containerMaxHealth = this.getMaxHealth();
        if (playerMaxHealth != containerMaxHealth) {
            BodyPart rootPart = this.getRootBodyPart();
            float diff = playerMaxHealth - containerMaxHealth;
            float newMaxHealth = rootPart.getMaxHealth() + diff;
            rootPart.setMaxHealth(Math.max(newMaxHealth, 1.0F));
        }
        float health = this.getHealth();
        entity.setHealth(health);
    }

    public void hurt(DamageContext context, float amount, BodyPart part, Consumer<BodyPart> onBodyPartLoss) {
        float damage = Math.min(part.getHealth(), amount * part.getDamageScale());
        float leftover = amount - damage;
        boolean wasDead = part.isDead();
        LivingEntity entity = context.getEntity();
        DamageSource source = context.getSource();
        ContextImpl ctx = ContextImpl.of(
                MedicalSystemContextKeys.HEALTH_CONTAINER, this,
                ContextKeys.LIVING_ENTITY, entity,
                ContextKeys.DAMAGE_SOURCE, source
        );
        ctx.copyMissing(context.getData());
        part.hurt(damage);
        part.trigger(ctx);
        if (!part.isVital() && part.isDead() != wasDead) {
            StatisticTracker.incrementOptional(entity, MedSystemStats.LIMBS_LOST);
            onBodyPartLoss.accept(part);
        }
        // no need to redistribute damage from vital parts
        if (!part.isVital() && leftover > 0) {
            BodyPart parent = this.bodyPartLinks.get(part);
            if (parent != null) {
                float scale = parent.getParentDamageScale();
                this.hurt(context, leftover * scale, parent, onBodyPartLoss);
            }
        }
    }

    public boolean canHeal(@Nullable BodyPart part, boolean allowDead) {
        if (part != null) {
            return (part.isDead() && allowDead) || part.getMaxHealAmount() > 0;
        }
        return this.getPartToHeal(allowDead) != null;
    }

    public float heal(LivingEntity entity, float amount, @Nullable BodyPart targetPart) {
        if (targetPart != null && !targetPart.isDead()) {
            // Heal specific body part only
            float healAmount = Math.min(amount, targetPart.getMaxHealAmount());
            targetPart.heal(healAmount);
            targetPart.trigger(ContextImpl.of(
                    MedicalSystemContextKeys.HEALTH_CONTAINER, this,
                    ContextKeys.LIVING_ENTITY, entity
            ));
            return amount - healAmount;
        } else {
            // Heal body parts, prioritize vitals, then according to health
            BodyPart part;
            while (amount > 0.0F && (part = this.getPartToHeal(false)) != null) {
                float healAmount = Math.min(amount, part.getMaxHealAmount());
                part.heal(healAmount);
                part.trigger(ContextImpl.of(
                        MedicalSystemContextKeys.HEALTH_CONTAINER, this,
                        ContextKeys.LIVING_ENTITY, entity
                ));
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

    @Override
    public Codec<HealthContainer> networkCodec() {
        return CODEC;
    }

    public boolean shouldDie() {
        float health = 0.0F;
        for (BodyPart part : this.bodyParts.values()) {
            health += part.getHealth();
            if (part.shouldOwnerDie()) {
                return true;
            }
        }
        return health <= 0.0F;
    }

    public void acceptHitboxes(BiConsumer<BodyPartHitbox, BodyPart> consumer) {
        this.acceptHitboxes((hb, p) -> true, consumer);
    }

    public void acceptHitboxes(BiPredicate<BodyPartHitbox, BodyPart> filter, BiConsumer<BodyPartHitbox, BodyPart> consumer) {
        for (BodyPartHitbox hitbox : this.definition.getHitboxes()) {
            BodyPart part = this.bodyParts.get(hitbox.getOwner());
            if (part == null)
                continue;
            if (filter.test(hitbox, part)) {
                consumer.accept(hitbox, part);
            }
        }
    }

    public BodyPart getPartToHeal(boolean allowDead) {
        BodyPart targetPart = null;
        float targetPercentage = 1.0F;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.prioritizeVitalHealing) {
            for (BodyPart vitalPart : this.vitalParts) {
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
        BodyPart target = null;
        for (BodyPart part : this.bodyParts.values()) {
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

    private String resolveBodyParts(HealthContainerDefinition definition, Map<BodyPart, BodyPart> links, List<BodyPart> vitalParts) {
        String root = null;
        for (Map.Entry<String, BodyPartDefinition> health : definition.getBodyParts().entrySet()) {
            String part = health.getKey();
            String parent = health.getValue().getParent();
            BodyPart bodyPart = this.bodyParts.get(part);
            if (parent == null) {
                root = part;
            } else {
                links.put(bodyPart, this.bodyParts.get(parent));
            }
            BodyPartDefinition healthDef = health.getValue();
            if (healthDef.isVital()) {
                vitalParts.add(bodyPart);
            }
            bodyPart.setDefinition(healthDef);
        }
        return root;
    }
}
