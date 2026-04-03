package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.DataResult;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class HealthHelper {

    public static boolean allLimbsMatch(HealthContainer container, LimbType type, Predicate<Limb> filter) {
        return container.getLimbContainer().getLimbs()
                .filter(limb -> limb.getType() == type)
                .allMatch(filter);
    }

    public static boolean allLimbsMatch(HealthContainer container, Predicate<Limb> filter) {
        return container.getLimbContainer().getLimbs().allMatch(filter);
    }

    public static boolean anyLimbsMatch(HealthContainer container, Predicate<Limb> filter) {
        return container.getLimbContainer().hasLimb(filter);
    }

    public static boolean allLimbsDead(HealthContainer container, LimbType type) {
        return allLimbsMatch(container, type, Limb::isDead);
    }

    public static boolean anyLimbDead(HealthContainer container) {
        return anyLimbsMatch(container, Limb::isDead);
    }

    public static List<Limb> getDeadLimbs(HealthContainer container) {
        return container.getLimbContainer().getLimbs()
                .filter(Limb::isDead)
                .toList();
    }

    public static void recoverVitalLimbs(HealthContainer container, float health) {
        container.getLimbContainer().getVitalLimbs().forEach(limb -> {
            if (limb.isDead()) {
                limb.setHealth(health);
            }
        });
    }

    public static DataResult<HealthContainerDefinition> validateHealthContainer(HealthContainerDefinition container) {
        // validation of hitbox links
        LimbConfiguration limbConfiguration = container.limbConfiguration();
        Set<String> limbs = limbConfiguration.limbs().keySet();
        EntityHitboxContainer hitboxContainer = container.hitboxContainer();
        Map<String, EntityHitboxContainer.LimbHitboxContainer> hitboxMap = hitboxContainer.definitions();
        HealthContainerDisplay display = container.display();
        for (String limbCode : limbs) {
            if (!hitboxMap.containsKey(limbCode)) {
                return DataResult.error(() -> "Missing hitbox definition for limb " + limbCode);
            }
            if (!display.displayDataMap().containsKey(limbCode)) {
                return DataResult.error(() -> "Missing display data for limb " + limbCode);
            }
        }
        // unknown limbs in hitboxes validation
        Set<String> hitboxLimbs = hitboxMap.keySet();
        for (String limbCode : hitboxLimbs) {
            if (!limbs.contains(limbCode)) {
                return DataResult.error(() -> "Unknown hitbox limb " + limbCode);
            }
        }
        // unknown limbs in display configuration
        Set<String> displayLimbs = display.displayDataMap().keySet();
        for (String limbCode : displayLimbs) {
            if (!limbs.contains(limbCode)) {
                return DataResult.error(() -> "Unknown display limb " + limbCode);
            }
        }
        return DataResult.success(container);
    }

    public static boolean canHeal(HealthContainer container) {
        return selectLimbForHealing(container.getLimbContainer()) != null;
    }

    public static @Nullable Limb selectLimbForHealing(HealthContainer container) {
        return selectLimbForHealing(container.getLimbContainer());
    }

    public static @Nullable Limb selectLimbForHealing(LimbContainer container) {
        Limb targetPart = null;
        float targetPercentage = 1.0F;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.prioritizeVitalHealing) {
            for (Limb vitalPart : container.getVitalLimbs()) {
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
        for (Limb part : container) {
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

    public static void synchronizeHealth(LivingEntity entity, HealthContainer healthContainer) {
        LimbContainer container = healthContainer.getLimbContainer();
        HealthContainerDefinition definition = healthContainer.getDefinition();
        LimbConfiguration limbConfiguration = definition.limbConfiguration();
        float playerMaxHealth = entity.getMaxHealth();
        float containerMaxHealth = container.getMaxHealth();
        float originalContainerMaxHealth = limbConfiguration.getMaxHealth();
        if (playerMaxHealth != containerMaxHealth) {
            if (playerMaxHealth == originalContainerMaxHealth) {
                container.restoreHealthLimits();
            } else {
                double diff = playerMaxHealth - originalContainerMaxHealth;
                int limbs = container.getLimbCount();
                double perLimb = diff / limbs;
                for (Limb limb : container) {
                    limb.restoreHealthLimit();
                    float newMaxHealth = (float) (limb.getMaxHealth() + perLimb);
                    limb.setMaxHealth(Math.max(newMaxHealth, 1.0F));
                }
            }
        }
        float health = container.getHealth();
        entity.setHealth(health);
    }

    private HealthHelper() {
    }
}
