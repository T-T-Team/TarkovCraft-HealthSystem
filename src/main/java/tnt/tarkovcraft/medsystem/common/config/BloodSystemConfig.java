package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class BloodSystemConfig {

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Enables blood system simulation, unconscious player state")
    public boolean useBloodSystem = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Enables blood level display in health UI")
    public boolean showBloodLevel = false;

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Enables blood type display in health UI")
    public boolean showBloodType = true;

    @Configurable
    @Configurable.Comment({
            "Specifies when entities can target unconscious players",
            "ALWAYS - unconscious players will always be attacked",
            "IGNORE_RESCUE - unconscious players will be attacked as long as they're not in rescue mode",
            "NEVER - unconscious players will never be attacked"
    })
    public UnconsciousEntityTargeting unconsciousEntityTargeting = UnconsciousEntityTargeting.NEVER;

    @Configurable
    @Configurable.Comment("Will trigger downed state if playing alone in singleplayer")
    public boolean allowDownedSingleplayer = false;

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
    @Configurable.Comment("Unconscious duration interval on unconsciousness due to blood loss")
    public TimeRange unconsciousOnBloodLoss = new TimeRange(5, 10);
    
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

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Gui.Slider
    public float shockPerHpLoss = 0.04F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Gui.Slider
    public float shockPerLimbLoss = 0.25F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Gui.Slider
    public float shockPerFracture = 0.1F;
}
