package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum LimbType {

    HEAD(0, 0xFF0000, EquipmentSlot.HEAD),
    TORSO(0, 0xFFFF00, EquipmentSlot.CHEST),
    STOMACH(WoundPriorities.SURGERY_HEALTH, 0xFF00, EquipmentSlot.CHEST),
    ARM(WoundPriorities.SURGERY_OTHER, 0xFFFF),
    LEG(WoundPriorities.SURGERY_MOVEMENT, 0xFF, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    ANIMAL(0, 0x00FF00, EquipmentSlot.BODY),
    OTHER(0, 0x444444);

    private final int surgeryPriority;
    private final int hitboxColor;
    private final Set<EquipmentSlot> armorSlots;

    LimbType(int surgeryPriority, int hitboxColor) {
        this(surgeryPriority, hitboxColor, null);
    }

    LimbType(int surgeryPriority, int hitboxColor, EquipmentSlot first, EquipmentSlot... other) {
        this.surgeryPriority = surgeryPriority;
        this.hitboxColor = hitboxColor;
        this.armorSlots = first != null ? EnumSet.of(first, other) : Collections.emptySet();
    }

    public static EnumSet<LimbType> getProtectedByEquipment(EquipmentSlot slot) {
        EnumSet<LimbType> set = EnumSet.noneOf(LimbType.class);
        for (LimbType group : LimbType.values()) {
            if (group.armorSlots.contains(slot)) {
                set.add(group);
            }
        }
        return set;
    }

    public Set<EquipmentSlot> getArmorSlots() {
        return armorSlots;
    }

    public int getHitboxColor() {
        return hitboxColor;
    }

    public int getSurgeryHealingPriority() {
        return surgeryPriority;
    }
}
