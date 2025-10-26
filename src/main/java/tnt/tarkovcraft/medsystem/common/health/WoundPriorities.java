package tnt.tarkovcraft.medsystem.common.health;

public final class WoundPriorities {

    public static final float VITAL_PART_MULTIPLIER = 1.5F;
    public static final int SURGERY_BASE = 1000; // we want to always prioritize dead limbs
    public static final int SURGERY_MOVEMENT = 30;
    public static final int SURGERY_HEALTH = 20;
    public static final int SURGERY_OTHER = 10;
    public static final int EFFECT_CRITICAL = 50;
    public static final int EFFECT_MAJOR = 30;
    public static final int EFFECT_MINOR = 15;
    public static final int HEALTH_UNIT = 5;
}
