package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.reaction.ReactionDefinition;

import java.util.*;
import java.util.stream.Collectors;

public final class HealthContainerHelper {

    public static DataResult<HealthContainerDefinition> validate(HealthContainerDefinition container) {
        // validation of hitbox links
        Set<String> hitboxOwners = container.getHitboxes().stream().map(BodyPartHitbox::getOwner).collect(Collectors.toSet());
        if (container.getBodyParts().isEmpty()) {
            return DataResult.error(() -> "At least one body part must be specified");
        }
        if (hitboxOwners.size() != container.getBodyParts().size()) {
            return DataResult.error(() -> "Mismatched hitbox count. Got " + hitboxOwners.size() + ", expected " + container.getBodyParts().size());
        }
        for (String owner : container.getBodyParts().keySet()) {
            if (!hitboxOwners.contains(owner)) {
                return DataResult.error(() -> "Missing hitbox definition for body part " + owner);
            }
        }
        // Validation of body part links
        DataResult<String> rootValidation = getRootBodyPart(container.getBodyParts());
        if (rootValidation.isError()) {
            return rootValidation.map(s -> container);
        }
        String root = rootValidation.getOrThrow();
        for (Map.Entry<String, BodyPartDefinition> entry : container.getBodyParts().entrySet()) {
            String error = validateBodyPartLink(root, entry.getKey(), entry.getValue(), container.getBodyParts());
            if (error != null) {
                return DataResult.error(() -> "Validation of body part links of " + entry.getKey() + " part failed: " + error);
            }
        }
        // Validation of display links
        Set<String> displaySources = container.getDisplayConfiguration().stream().map(BodyPartDisplay::source).collect(Collectors.toSet());
        for (String source : displaySources) {
            if (!container.getBodyParts().containsKey(source)) {
                return DataResult.error(() -> "Missing body part for source " + source);
            }
        }
        return DataResult.success(container);
    }

    private static String validateBodyPartLink(String root, String partId, BodyPartDefinition part, Map<String, BodyPartDefinition> bodyParts) {
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

    private static DataResult<String> getRootBodyPart(Map<String, BodyPartDefinition> parts) {
        String root = null;
        for (Map.Entry<String, BodyPartDefinition> entry : parts.entrySet()) {
            BodyPartDefinition part = entry.getValue();
            if (part.getParent() == null) {
                if (root != null) {
                    return DataResult.error(() -> "Multiple root body parts detected");
                }
                root = entry.getKey();
            }
        }
        return root != null ? DataResult.success(root) : DataResult.error(() -> "Missing root body part");
    }

    public static HealthContainerDefinition merge(EntityType<?> type, HealthContainerDefinition self, HealthContainerDefinition other) {
        MedicalSystem.LOGGER.warn(HealthSystem.MARKER, "Merging multiple health container definitions for entity '{}'", BuiltInRegistries.ENTITY_TYPE.getKey(type));
        if (other.canReplace()) {
            return other;
        }
        List<EntityType<?>> targets = mergeTargets(self, other);
        var bodyPartMerge = mergeAndRemoveBodyParts(self, other);
        Map<String, BodyPartDefinition> newBodyParts = bodyPartMerge.getFirst();
        Set<String> deletedParts = bodyPartMerge.getSecond();
        List<BodyPartHitbox> hitboxes = mergeHitboxes(self, other, deletedParts);
        List<BodyPartDisplay> displays = mergeDisplays(self, other, deletedParts);
        return validate(new HealthContainerDefinition(
                self.canReplace(),
                targets,
                newBodyParts,
                hitboxes,
                displays
        )).getOrThrow();
    }

    private static List<EntityType<?>> mergeTargets(HealthContainerDefinition self, HealthContainerDefinition other) {
        List<EntityType<?>> targets = new ArrayList<>(self.getTargets());
        targets.addAll(other.getTargets());
        return targets;
    }

    private static Pair<Map<String, BodyPartDefinition>, Set<String>> mergeAndRemoveBodyParts(HealthContainerDefinition self, HealthContainerDefinition other) {
        Set<String> deletedParts = new HashSet<>();
        Map<String, BodyPartDefinition> newBodyParts = new HashMap<>(self.getBodyParts());
        for (Map.Entry<String, BodyPartDefinition> entry : other.getBodyParts().entrySet()) {
            BodyPartGroup group = entry.getValue().getBodyPartGroup();
            if (group.isInactive()) {
                newBodyParts.remove(entry.getKey());
                deletedParts.add(entry.getKey());
            } else {
                BodyPartDefinition def1 = newBodyParts.get(entry.getKey());
                BodyPartDefinition def2 = entry.getValue();
                if (def1 == null) {
                    newBodyParts.put(entry.getKey(), def2);
                } else {
                    newBodyParts.put(entry.getKey(), mergeBodyPart(def1, def2));
                }
            }
        }

        return Pair.of(newBodyParts, deletedParts);
    }

    public static BodyPartDefinition mergeBodyPart(BodyPartDefinition self, BodyPartDefinition other) {
        boolean vital = other.isVital();
        String parent = other.getParent();
        float parentDamageScale = other.getParentDamageScale();
        float damageScale = other.getDamageScale();
        float health = other.getMaxHealth();
        BodyPartGroup group = other.getBodyPartGroup();
        Map<UUID, ReactionDefinition> reactions = mergeReactions(self, other);
        return new BodyPartDefinition(vital, Optional.ofNullable(parent), parentDamageScale, damageScale, health, group, reactions);
    }

    private static Map<UUID, ReactionDefinition> mergeReactions(BodyPartDefinition self, BodyPartDefinition other) {
        Map<UUID, ReactionDefinition> map = new HashMap<>(self.getReactionMap());
        map.putAll(other.getReactionMap());
        return map;
    }

    private static List<BodyPartHitbox> mergeHitboxes(HealthContainerDefinition self, HealthContainerDefinition other, Set<String> deletedParts) {
        List<BodyPartHitbox> hitboxes = new ArrayList<>(self.getHitboxes());
        hitboxes.addAll(other.getHitboxes());
        hitboxes.removeIf(t -> deletedParts.contains(t.getOwner()));
        return hitboxes;
    }

    private static List<BodyPartDisplay> mergeDisplays(HealthContainerDefinition self, HealthContainerDefinition other, Set<String> deletedParts) {
        List<BodyPartDisplay> display = new ArrayList<>(self.getDisplayConfiguration());
        display.addAll(other.getDisplayConfiguration());
        display.removeIf(t -> deletedParts.contains(t.source()));
        return display;
    }
}
