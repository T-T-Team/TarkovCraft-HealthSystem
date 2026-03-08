package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.DataResult;
import tnt.tarkovcraft.medsystem.common.health.*;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class HealthHelper {

    public static boolean allMatch(HealthContainer container, LimbType type, Predicate<Limb> filter) {
        return container.getLimbsAsStream()
                .filter(limb -> limb.getType() == type)
                .allMatch(filter);
    }

    public static boolean allMatch(HealthContainer container, Predicate<Limb> filter) {
        return container.getLimbsAsStream().allMatch(filter);
    }

    public static boolean allDead(HealthContainer container, LimbType type) {
        return allMatch(container, type, Limb::isDead);
    }

    public static boolean allDead(HealthContainer container) {
        return allMatch(container, Limb::isDead);
    }

    public static void recoverVitalLimbs(HealthContainer container, float health) {
        container.getVitalLimbs().forEach(limb -> {
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

    private HealthHelper() {
    }
}
