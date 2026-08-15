package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.DataResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.checkerframework.checker.nullness.qual.NonNull;
import tnt.tarkovcraft.core.network.message.S2C_MakeParticles;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import javax.annotation.Nullable;
import java.util.*;
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
                limb.healUpTo(health);
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
        return selectLimbForHealing(container, false);
    }

    public static @Nullable Limb selectLimbForHealing(HealthContainer container, boolean allowDisabledLimbs) {
        return selectLimbForHealing(container.getLimbContainer(), allowDisabledLimbs);
    }

    public static @Nullable Limb selectLimbForHealing(LimbContainer container) {
        return selectLimbForHealing(container, false);
    }

    public static @Nullable Limb selectLimbForHealing(LimbContainer container, boolean allowDisabledLimbs) {
        Limb targetPart = null;
        float targetPercentage = 1.0F;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.health.prioritizeVitalHealing) {
            for (Limb vitalPart : container.getVitalLimbs()) {
                if (vitalPart.isDead())
                    continue;
                float percentage = vitalPart.getHealthPercent();
                if (percentage < config.health.vitalBodyPartHealthTrigger && percentage < targetPercentage) {
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
            if (!allowDisabledLimbs && part.isDead())
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

    public static void submitServerBleedParticles(@NonNull BloodDripParticleOptions options, int count, double x, double y, double z, double mx, double my, double mz, LivingEntity entity) {
        submitServerBleedParticles(options, count, x, y, z, mx, my, mz, 0.5, entity);
    }

    public static void submitServerBleedParticles(@NonNull BloodDripParticleOptions options, int count, double x, double y, double z, double mx, double my, double mz, double spreadFactor, LivingEntity entity) {
        if (entity.level().isClientSide() || count < 1)
            return;
        if (!MedicalSystem.getConfig().bloodDecals.enableBloodDecals)
            return;
        RandomSource random = entity.getRandom();
        List<Vec3> movements = count > 1
                ? randomizeParticleMovements(random, mx, my, mz, spreadFactor, count)
                : Collections.singletonList(new Vec3(mx, my, mz));
        S2C_MakeParticles message = new S2C_MakeParticles(options, x, y, z, true, true, movements);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }

    public static void doWithEntityControlOverride(Entity entity, Runnable action) {
        boolean controlled = entity.getData(MedSystemDataAttachments.EXTERNALLY_CONTROLLED);
        if (controlled) {
            action.run();
            return;
        }
        entity.setData(MedSystemDataAttachments.EXTERNALLY_CONTROLLED, true);
        action.run();
        entity.removeData(MedSystemDataAttachments.EXTERNALLY_CONTROLLED);
    }

    private static List<Vec3> randomizeParticleMovements(RandomSource random, double x, double y, double z, double randomizeFactor, int outputSize) {
        List<Vec3> list = new ArrayList<>(outputSize);
        for (int i = 0; i < outputSize; i++) {
            double mx = (random.nextDouble() * randomizeFactor) * x;
            double mz = (random.nextDouble() * randomizeFactor) * z;
            list.add(new Vec3(mx, y, mz));
        }
        return list;
    }

    private HealthHelper() {
    }
}
