package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BodyPartGroup {

    HEAD(0xFF0000, EquipmentSlot.HEAD),
    TORSO(0xFFFF00, EquipmentSlot.CHEST),
    STOMACH(0xFF00, EquipmentSlot.CHEST),
    ARM(0xFFFF),
    LEG(0xFF, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    ANIMAL(0x00FF00, EquipmentSlot.BODY),
    OTHER(0x444444),
    // To be used for data loading to mark the body part as removed
    INACTIVE(0);

    private final Set<EquipmentSlot> armorSlots;
    private final int hitboxColor;

    BodyPartGroup(int hitboxColor) {
        this(hitboxColor, null);
    }

    BodyPartGroup(int hitboxColor, EquipmentSlot first, EquipmentSlot... other) {
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

    public boolean isInactive() {
        return this == INACTIVE;
    }
}
