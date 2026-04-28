package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

public final class HealthConfig {

    @Configurable
    @Configurable.Comment("Health will be primarily recovered into vital parts")
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Threshold for prioritized vital limb health recovery")
    public float vitalBodyPartHealthTrigger = 0.75F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Global damage multiplier for all limbs")
    public float globalDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for head")
    public float headDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for body")
    public float bodyDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for stomach")
    public float stomachDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for arms")
    public float armDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for legs")
    public float legDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for animal limbs")
    public float animalDamageMultiplier = 1.0F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Damage multiplier for all other limbs")
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
