package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BodyPartGroup {

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

    BodyPartGroup(int surgeryPriority, int hitboxColor) {
        this(surgeryPriority, hitboxColor, null);
    }

    BodyPartGroup(int surgeryPriority, int hitboxColor, EquipmentSlot first, EquipmentSlot... other) {
        this.surgeryPriority = surgeryPriority;
        this.hitboxColor = hitboxColor;
        this.armorSlots = first != null ? EnumSet.of(first, other) : Collections.emptySet();
    }

    public static EnumSet<BodyPartGroup> getProtectedByEquipment(EquipmentSlot slot) {
        EnumSet<BodyPartGroup> set = EnumSet.noneOf(BodyPartGroup.class);
        for (BodyPartGroup group : BodyPartGroup.values()) {
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
