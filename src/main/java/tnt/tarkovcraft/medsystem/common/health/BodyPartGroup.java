package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.EnumSet;

public enum BodyPartGroup {

    HEAD(EquipmentSlot.HEAD, 0xFF0000),
    TORSO(EquipmentSlot.CHEST, 0xFFFF00),
    STOMACH(EquipmentSlot.CHEST, 0xFF00),
    ARM(0xFFFF),
    LEG(EquipmentSlot.LEGS, 0xFF),
    ANIMAL(EquipmentSlot.BODY, 0x00FF00),
    OTHER(0x444444),
    // To be used for data loading to mark the body part as removed
    INACTIVE(0);

    private final EquipmentSlot armorSlot;
    private final int hitboxColor;

    BodyPartGroup(int hitboxColor) {
        this(null, hitboxColor);
    }

    BodyPartGroup(EquipmentSlot armorSlot, int hitboxColor) {
        this.armorSlot = armorSlot;
        this.hitboxColor = hitboxColor;
    }

    public static EnumSet<BodyPartGroup> getProtectedByEquipment(EquipmentSlot slot) {
        EnumSet<BodyPartGroup> set = EnumSet.noneOf(BodyPartGroup.class);
        for (BodyPartGroup group : BodyPartGroup.values()) {
            if (slot.equals(group.armorSlot)) {
                set.add(group);
            }
        }
        return set;
    }

    @Nullable
    public EquipmentSlot getArmorSlot() {
        return armorSlot;
    }

    public int getHitboxColor() {
        return hitboxColor;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }
}
