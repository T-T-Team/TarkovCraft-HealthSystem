package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.DataResult;

import java.util.Set;
import java.util.stream.Collectors;

public final class HealthContainerHelper {

    public static DataResult<HealthContainerDefinition> validate(HealthContainerDefinition container) {
        // validation of hitbox links
        Set<String> hitboxOwners = container.getHitboxes().stream().map(BodyPartHitbox::getOwner).collect(Collectors.toSet());
        LimbConfiguration limbConfiguration = container.limbConfiguration();
        if (hitboxOwners.size() != limbConfiguration.getLimbCount()) {
            return DataResult.error(() -> "Mismatched hitbox count. Got " + hitboxOwners.size() + ", expected " + limbConfiguration.getLimbCount());
        }
        for (String owner : limbConfiguration.limbs().keySet()) {
            if (!hitboxOwners.contains(owner)) {
                return DataResult.error(() -> "Missing hitbox definition for body part " + owner);
            }
        }
        // Validation of display links
        Set<String> displaySources = container.getDisplayConfiguration().stream().map(BodyPartDisplay::source).collect(Collectors.toSet());
        for (String source : displaySources) {
            if (!limbConfiguration.limbs().containsKey(source)) {
                return DataResult.error(() -> "Missing body part for source " + source);
            }
        }
        return DataResult.success(container);
    }
}
