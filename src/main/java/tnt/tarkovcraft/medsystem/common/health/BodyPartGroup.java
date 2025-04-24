package tnt.tarkovcraft.medsystem.common.health;

public enum BodyPartGroup {

    HEAD(0xFF0000),
    TORSO(0xFFFF00),
    STOMACH(0xFF00),
    ARM(0xFFFF),
    LEG(0xFF),
    OTHER(0x444444),
    // To be used for data loading to mark the body part as removed
    INACTIVE(0);

    private final int hitboxColor;

    BodyPartGroup(int hitboxColor) {
        this.hitboxColor = hitboxColor;
    }

    public int getHitboxColor() {
        return hitboxColor;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }
}
