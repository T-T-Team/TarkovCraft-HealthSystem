package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.network.Synchronizable;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class HealthContainer implements Synchronizable<HealthContainer> {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, BodyPart.CODEC).fieldOf("bodyParts").forGetter(t -> t.bodyParts)
    ).apply(instance, HealthContainer::new));

    private final HealthContainerDefinition definition;
    private final Map<String, BodyPart> bodyParts;
    private final Map<BodyPart, BodyPart> bodyPartLinks;
    private final List<BodyPart> vitalParts;
    private final String root;
    private DamageContext activeDamageContext;

    public HealthContainer() {
        this.definition = null;
        this.bodyParts = Collections.emptyMap();
        this.bodyPartLinks = Collections.emptyMap();
        this.vitalParts = Collections.emptyList();
        this.root = "";
    }

    public HealthContainer(HealthContainerDefinition definition, Map<String, BodyPart> bodyParts) {
        this.definition = definition;
        this.bodyParts = bodyParts;
        this.bodyPartLinks = new IdentityHashMap<>();
        this.vitalParts = new ArrayList<>();
        this.root = this.resolveBodyParts(this.definition, this.bodyPartLinks, this.vitalParts);
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

    public void hurt(float amount, BodyPart bodyPart) {
        float damage = Math.min(bodyPart.getHealth(), amount);
        float leftover = amount - damage;
        bodyPart.hurt(damage);
        if (leftover > 0) {
            BodyPart parent = this.bodyPartLinks.get(bodyPart);
            if (parent != null) {
                float scale = parent.getParentDamageScale();
                this.hurt(leftover * scale, parent);
            }
        }
    }

    public float heal(float amount, @Nullable String bodyPart) {
        if (bodyPart != null && this.hasBodyPart(bodyPart)) {
            // Heal specific body part only
            BodyPart part = this.getBodyPart(bodyPart);
            float healAmount = Math.min(amount, part.getMaxHealAmount());
            part.heal(healAmount);
            return amount - healAmount;
        } else {
            // Heal body parts, prioritize vitals, then according to health
            BodyPart part;
            while (amount > 0.0F && (part = this.getPartToHeal()) != null) {
                float healAmount = Math.min(amount, part.getMaxHealAmount());
                part.heal(healAmount);
                MedicalSystem.LOGGER.info("Healing part {} for amount {}. [{}/{}]", part.getGroup(), healAmount, part.getHealth(), part.getMaxHealth());
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

    private BodyPart getPartToHeal() {
        BodyPart targetPart = null;
        float targetPercentage = 1.0F;
        for (BodyPart vitalPart : this.vitalParts) {
            float percentage = vitalPart.getHealthPercent();
            if (percentage < 0.75F && percentage < targetPercentage) {
                targetPercentage = percentage;
                targetPart = vitalPart;
            }
        }
        if (targetPart != null) {
            return targetPart;
        }
        BodyPart target = null;
        for (BodyPart part : this.bodyParts.values()) {
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
        for (Map.Entry<String, BodyPartHealthDefinition> health : definition.getBodyParts().entrySet()) {
            String part = health.getKey();
            String parent = health.getValue().getParent();
            if (parent == null) {
                root = part;
            } else {
                links.put(this.bodyParts.get(part), this.bodyParts.get(parent));
            }
            BodyPartHealthDefinition healthDef = health.getValue();
            if (healthDef.isVital()) {
                vitalParts.add(this.bodyParts.get(part));
            }
        }
        return root;
    }
}
