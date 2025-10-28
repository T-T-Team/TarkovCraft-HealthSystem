package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.DataResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class HealthContainerHelper {

    public static DataResult<HealthContainerDefinition> validate(HealthContainerDefinition container) {
        // validation of hitbox links
        Set<String> hitboxOwners = container.getHitboxes().stream().map(BodyPartHitbox::getOwner).collect(Collectors.toSet());
        if (container.getLimbDefinitionMap().isEmpty()) {
            return DataResult.error(() -> "At least one body part must be specified");
        }
        if (hitboxOwners.size() != container.getLimbDefinitionMap().size()) {
            return DataResult.error(() -> "Mismatched hitbox count. Got " + hitboxOwners.size() + ", expected " + container.getLimbDefinitionMap().size());
        }
        for (String owner : container.getLimbDefinitionMap().keySet()) {
            if (!hitboxOwners.contains(owner)) {
                return DataResult.error(() -> "Missing hitbox definition for body part " + owner);
            }
        }
        // Validation of body part links
        DataResult<String> rootValidation = getRootBodyPart(container.getLimbDefinitionMap());
        if (rootValidation.isError()) {
            return rootValidation.map(s -> container);
        }
        String root = rootValidation.getOrThrow();
        for (Map.Entry<String, LimbDefinition> entry : container.getLimbDefinitionMap().entrySet()) {
            String error = validateBodyPartLink(root, entry.getKey(), entry.getValue(), container.getLimbDefinitionMap());
            if (error != null) {
                return DataResult.error(() -> "Validation of body part links of " + entry.getKey() + " part failed: " + error);
            }
        }
        // Validation of display links
        Set<String> displaySources = container.getDisplayConfiguration().stream().map(BodyPartDisplay::source).collect(Collectors.toSet());
        for (String source : displaySources) {
            if (!container.getLimbDefinitionMap().containsKey(source)) {
                return DataResult.error(() -> "Missing body part for source " + source);
            }
        }
        return DataResult.success(container);
    }

    private static String validateBodyPartLink(String root, String partId, LimbDefinition part, Map<String, LimbDefinition> bodyParts) {
        if (partId.equals(root)) {
            return null;
        }
        Set<String> previousParents = new HashSet<>();
        String parent;
        while (true) {
            parent = part.getParent();
            if (!bodyParts.containsKey(parent)) {
                return "Unknown body part in link: " + parent;
            }
            if (!previousParents.add(parent)) {
                return "Circular reference in body part links: " + parent;
            }
            part = bodyParts.get(parent);

            if (parent.equals(root)) {
                return null;
            }
        }
    }

    private static DataResult<String> getRootBodyPart(Map<String, LimbDefinition> parts) {
        String root = null;
        for (Map.Entry<String, LimbDefinition> entry : parts.entrySet()) {
            LimbDefinition part = entry.getValue();
            if (part.getParent() == null) {
                if (root != null) {
                    return DataResult.error(() -> "Multiple root body parts detected");
                }
                root = entry.getKey();
            }
        }
        return root != null ? DataResult.success(root) : DataResult.error(() -> "Missing root body part");
    }
}
