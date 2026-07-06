package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class BloodDecalConfig {

    @Configurable
    @Configurable.Comment(value = "Toggles all blood decals", localize = true)
    public boolean enableBloodDecals = true;

    @Configurable
    @Configurable.Comment(value = "Forces all blood decals to be have specific color - configure in the option below", localize = true)
    public boolean forceBloodDecalColor = false;

    @Configurable
    @Configurable.StringPattern("^#[0-9a-fA-F]{1,6}$")
    @Configurable.Gui.ColorValue
    public String bloodDecalColor = "#B20000";

    @Configurable
    @Configurable.Range(min = 100, max = 72000)
    @Configurable.Synchronized
    public int bloodDecalLifetime = Duration.minutes(1).tickValue();

    @Configurable
    @Configurable.DecimalRange(min = 0.10F, max = 0.35F)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Comment(value = "Blood decal rendering scale", localize = true)
    @Configurable.Synchronized
    public float bloodDecalScale = 0.15F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment(value = "Lifetime percentage at which decals will start to fade out", localize = true)
    @Configurable.Synchronized
    public float bloodDecalFadeOutAt = 0.25F;

    @Configurable
    @Configurable.DecimalRange(min = 0.25F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "How much damage entity needs to receive in order for decal to appear", localize = true)
    public float damageDecalScale = 3.0F;

    @Configurable
    @Configurable.Range(min = 0, max = 15)
    @Configurable.Gui.Slider
    @Configurable.Comment(value = {
            "Maximum amount of decals which can appear when damaging entities",
            "Set to 0 to disable damage decals"
    }, localize = true)
    public int maxDamageDecalsPerHit = 8;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.5F)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    @Configurable.Comment(value = "How much motion is applied to decals on received damage", localize = true)
    public float damageMotionScale = 0.25F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.5F)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    @Configurable.Comment(value = "How much motion is applied to decals on received damage from projectiles", localize = true)
    public float projectileDamageMotionScale = 0.6F;

    public int getDecalColor(int inputColor) {
        if (this.forceBloodDecalColor) {
            return Integer.decode(this.bloodDecalColor);
        }
        return inputColor;
    }
}