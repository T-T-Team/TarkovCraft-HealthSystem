package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

public final class HealthConfig {

    @Configurable
    @Configurable.Comment(value = "Health will be primarily recovered into vital parts", localize = true)
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Threshold for prioritized vital limb health recovery", localize = true)
    public float vitalBodyPartHealthTrigger = 0.75F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Global damage multiplier for all limbs", localize = true)
    public float globalDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for head", localize = true)
    public float headDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for body", localize = true)
    public float bodyDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for stomach", localize = true)
    public float stomachDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for arms", localize = true)
    public float armDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for legs", localize = true)
    public float legDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for animal limbs", localize = true)
    public float animalDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Damage multiplier for all other limbs", localize = true)
    public float otherDamageMultiplier = 1.0F;

    public float applyDamageMultipliers(float incomingDamage, LimbType limb) {
        float reducedGlobal = incomingDamage * this.globalDamageMultiplier;
        return switch (limb) {
            case HEAD -> reducedGlobal * this.headDamageMultiplier;
            case TORSO -> reducedGlobal * this.bodyDamageMultiplier;
            case STOMACH -> reducedGlobal * this.stomachDamageMultiplier;
            case ARM -> reducedGlobal * this.armDamageMultiplier;
            case LEG -> reducedGlobal * this.legDamageMultiplier;
            case ANIMAL -> reducedGlobal * this.animalDamageMultiplier;
            default -> reducedGlobal * this.otherDamageMultiplier;
        };
    }
}
