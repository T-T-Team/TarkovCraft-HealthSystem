package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import net.minecraft.world.entity.EquipmentSlot;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

import java.util.Set;

public class ArmorSystemConfig {

    @Configurable
    @Configurable.Comment(value = {
            "Defines armor calculation logic with custom health system",
            "SIMULATED - modular armor with fully simulated logic such as deflections, blunt damage and so on",
            "MODULAR - modular armor - getting entryPoint in head damages only helmet",
            "MODULAR_BOOSTED - same as above, but each armor piece has additional protection. Configurable below",
            "VANILLA - vanilla armor calculation, full armor is used for any damage calculations"
    }, localize = true)
    @Configurable.UpdateRestriction(UpdateRestrictions.MAIN_MENU)
    public ArmorSystem armorSystem = ArmorSystem.SIMULATED;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment(value = "Additional armor protection per armor piece when using MODULAR_BOOSTED armor system", localize = true)
    public float modularBoostedArmorMultiplier = 2.5F;

    @Configurable
    @Configurable.Comment(value = "Configure which limbs are covered by specific armor pieces. Does nothing when using VANILLA armor system", localize = true)
    public LimbArmorProtectionAreas limbArmorProtectionAreas = new LimbArmorProtectionAreas();

    public Set<EquipmentSlot> getProtectedAreasForLimb(LimbType limb) {
        EquipmentSlot[] slots = switch (limb) {
            case HEAD -> this.limbArmorProtectionAreas.head;
            case TORSO -> this.limbArmorProtectionAreas.chest;
            case STOMACH -> this.limbArmorProtectionAreas.stomach;
            case ARM -> this.limbArmorProtectionAreas.arms;
            case LEG -> this.limbArmorProtectionAreas.legs;
            case ANIMAL -> this.limbArmorProtectionAreas.animalBody;
            default -> this.limbArmorProtectionAreas.other;
        };
        return Set.of(slots);
    }

    public static class LimbArmorProtectionAreas {

        @Configurable
        public EquipmentSlot[] head = { EquipmentSlot.HEAD };

        @Configurable
        public EquipmentSlot[] chest = { EquipmentSlot.CHEST };

        @Configurable
        public EquipmentSlot[] stomach = { EquipmentSlot.CHEST };

        @Configurable
        public EquipmentSlot[] arms = {};

        @Configurable
        public EquipmentSlot[] legs = { EquipmentSlot.LEGS, EquipmentSlot.FEET };

        @Configurable
        public EquipmentSlot[] animalBody = { EquipmentSlot.BODY };

        @Configurable
        public EquipmentSlot[] other = {};
    }
}
