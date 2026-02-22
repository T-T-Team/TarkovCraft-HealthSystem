package tnt.tarkovcraft.medsystem.common.effect.event;

public enum TriggerResult {
    SUCCESS,
    FAILED,
    INVALID;

    public boolean isValid() {
        return this == SUCCESS;
    }

    public static TriggerResult condition(boolean result) {
        return result ? SUCCESS : FAILED;
    }

    public static TriggerResult inverseCondition(boolean result) {
        return result ? FAILED : SUCCESS;
    }
}
