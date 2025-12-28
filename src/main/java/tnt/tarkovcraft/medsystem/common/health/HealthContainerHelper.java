package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.DataResult;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class HealthContainerHelper {

    public static DataResult<HealthContainerDefinition> validate(HealthContainerDefinition container) {
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
}
