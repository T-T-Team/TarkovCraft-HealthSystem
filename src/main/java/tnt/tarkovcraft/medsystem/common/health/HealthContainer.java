package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.network.Synchronizable;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import javax.annotation.Nullable;
import java.util.*;

public final class HealthContainer implements Synchronizable<HealthContainer> {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, BodyPartHealth.CODEC).fieldOf("bodyParts").forGetter(t -> t.bodyParts)
    ).apply(instance, HealthContainer::new));

    private final HealthContainerDefinition definition;
    private final Map<String, BodyPartHealth> bodyParts;
    private final Map<String, String> bodyPartLinks;
    private final Set<String> vitalParts;
    private final String root;

    public HealthContainer() {
        throw new UnsupportedOperationException("Cannot instantiate default health container");
    }

    public HealthContainer(HealthContainerDefinition definition, Map<String, BodyPartHealth> bodyParts) {
        this.definition = definition;
        this.bodyParts = bodyParts;
        this.bodyPartLinks = new HashMap<>();
        this.vitalParts = new HashSet<>();
        this.root = this.resolveBodyParts(this.definition, this.bodyPartLinks, this.vitalParts);
    }

    public HealthContainerDefinition getDefinition() {
        return definition;
    }

    public boolean hasBodyPart(String part) {
        return this.bodyParts.containsKey(part);
    }

    public BodyPartHealth getBodyPart(@Nullable String name) {
        return this.bodyParts.get(name != null ? name : this.root);
    }

    public BodyPartHealth getRootBodyPart() {
        return this.getBodyPart(null);
    }

    public float getHealth() {
        float health = 0.0F;
        for (BodyPartHealth bodyPartHealth : bodyParts.values()) {
            if (bodyPartHealth.shouldOwnerDie()) {
                return 0.0F;
            }
            health += bodyPartHealth.getHealth();
        }
        return health;
    }

    public float getMaxHealth() {
        float maxHealth = 0.0F;
        for (BodyPartHealth bodyPartHealth : bodyParts.values()) {
            maxHealth += bodyPartHealth.getMaxHealth();
        }
        return maxHealth;
    }

    public void updateHealth(LivingEntity entity) {
        float playerMaxHealth = entity.getMaxHealth();
        float containerMaxHealth = this.getMaxHealth();
        if (playerMaxHealth != containerMaxHealth) {
            BodyPartHealth rootPart = this.getRootBodyPart();
            float diff = playerMaxHealth - containerMaxHealth;
            float newMaxHealth = rootPart.getMaxHealth() + diff;
            rootPart.setMaxHealth(Math.max(newMaxHealth, 1.0F));
        }
        float health = this.getHealth();
        entity.setHealth(health);
    }

    public void hurt(LivingEntity entity, float amount, @Nullable String bodyPart) {

    }

    public float heal(float amount, @Nullable String bodyPart) {
        if (bodyPart != null && this.hasBodyPart(bodyPart)) {
            // Heal specific body part only
            BodyPartHealth part = this.getBodyPart(bodyPart);
            float healAmount = Math.min(amount, part.getMaxHealAmount());
            part.heal(healAmount);
            return amount - healAmount;
        } else {
            // Heal body parts, prioritize vitals, then according to health
            BodyPartHealth part;
            while (amount > 0.0F && (part = this.getPartToHeal()) != null) {
                float healAmount = Math.min(amount, part.getMaxHealAmount());
                part.heal(healAmount);
                MedicalSystem.LOGGER.info("Healing part {} for amount {}. [{}/{}]", part.getGroup(), healAmount, part.getHealth(), part.getMaxHealth());
                amount -= healAmount;
            }
        }
        return amount;
    }

    @Override
    public Codec<HealthContainer> networkCodec() {
        return CODEC;
    }

    private BodyPartHealth getPartToHeal() {
        String targetPart = null;
        float targetPercentage = 1.0F;
        for (String vitalPart : this.vitalParts) {
            BodyPartHealth part = this.getBodyPart(vitalPart);
            float percentage = part.getHealthPercent();
            if (percentage < 0.75F && percentage < targetPercentage) {
                targetPercentage = percentage;
                targetPart = vitalPart;
            }
        }
        if (targetPart != null) {
            return this.getBodyPart(targetPart);
        }
        BodyPartHealth target = null;
        for (BodyPartHealth part : this.bodyParts.values()) {
            float percentage = part.getHealthPercent();
            if (percentage < 1.0F && percentage < targetPercentage) {
                target = part;
                targetPercentage = percentage;
            }
        }
        return target;
    }

    private String resolveBodyParts(HealthContainerDefinition definition, Map<String, String> links, Set<String> vitalParts) {
        String root = null;
        for (Map.Entry<String, BodyPartHealthDefinition> health : definition.getBodyParts().entrySet()) {
            String part = health.getKey();
            String parent = health.getValue().getParent();
            if (parent == null) {
                root = part;
            } else {
                links.put(part, parent);
            }
            BodyPartHealthDefinition healthDef = health.getValue();
            if (healthDef.isVital()) {
                vitalParts.add(part);
            }
        }
        return root;
    }
}
