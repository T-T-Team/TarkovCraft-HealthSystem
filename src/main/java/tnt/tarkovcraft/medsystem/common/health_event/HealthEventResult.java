package tnt.tarkovcraft.medsystem.common.health_event;

public enum HealthEventResult {
    SUCCESS,
    FAILED,
    INVALID;

    public boolean isValid() {
        return this == SUCCESS;
    }

    public static HealthEventResult condition(boolean result) {
        return result ? SUCCESS : FAILED;
    }

    public static HealthEventResult inverseCondition(boolean result) {
        return result ? FAILED : SUCCESS;
    }
}
