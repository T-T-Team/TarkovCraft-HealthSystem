package tnt.tarkovcraft.medsystem.api;

public final class MedSystemConstants {

    // General
    public static final String MOD_ID = "medsystem";

    // heal priorities
    public static final float HEAL_VITAL_PART_MULTIPLIER = 1.5F;
    public static final int HEAL_SURGERY_BASE = 1000; // we want to always prioritize dead limbs
    public static final int HEAL_SURGERY_MOVEMENT = 30;
    public static final int HEAL_SURGERY_HEALTH = 20;
    public static final int HEAL_SURGERY_OTHER = 10;
    public static final int HEAL_EFFECT_CRITICAL = 50;
    public static final int HEAL_EFFECT_MAJOR = 30;
    public static final int HEAL_EFFECT_MINOR = 15;
    public static final int HEAL_HEALTH_UNIT = 5;

    // entity state
    public static final String DEFAULT_ENTITY_STATE = "default";
}
