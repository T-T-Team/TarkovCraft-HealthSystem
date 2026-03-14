package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class BloodSystemConfig {

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Enables blood system simulation, unconscious player state")
    public boolean useBloodSystem = true;

    @Configurable
    @Configurable.Comment({
            "Defines handling of unconscious bleed out stage when the affected player has too low blood level to wake up on their own",
            "When disabled, bleed out damage will be applied immediately after losing ability to wake up"
    })
    public UnconsciousMode bleedOutUnconsciousness = UnconsciousMode.ALLOW;

    @Configurable
    @Configurable.DecimalRange(min = 0.0, max = 1.0)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    @Configurable.Comment("Chance specifying if you can enter unconscious state instead of dying so that you may be rescued by your friends")
    public float unconsciousOnDeathChance = 1.0F;

    @Configurable
    @Configurable.Comment("Will prevent entering unconscious state on death if your head limb is dead too")
    @Configurable.DecimalRange(min = 0.0, max = 1.0)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    public float unconsciousOnHeadDeathChance = 1.0F;

    @Configurable
    @Configurable.Range(min = 200)
    @Configurable.Comment("Rescue waiting period before dying while in death unconscious state")
    public int rescueWaitDuration = Duration.parse("2m30s").tickValue();

    @Configurable
    @Configurable.Comment("Chance multiplier to enter unconscious mode on limb loss")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    public float unconsciousAfterLimbLossMultiplier = 1.0F;

    @Configurable
    @Configurable.Comment("Unconscious duration interval on unconsciousness due to limb loss")
    public TimeRange unconsciousOnLimbLoss = new TimeRange(7, 15);

    @Configurable
    @Configurable.Comment("Unconscious duration interval on unconsciousness due to blood loss")
    public TimeRange unconsciousOnBloodLoss = new TimeRange(5, 10);

    // TODO all below
    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    public float unconsciousHeldItemDropChance = 0.6F;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    public float unconsciousSoundVolumeScale = 0.2F;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    public float unconsciousSoundPitchScale = 0.7F;

    @Configurable
    @Configurable.Synchronized
    public UnconsciousOverlayType unconsciousOverlayType = UnconsciousOverlayType.BLINKING;
}
