package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class StatusEffectConfig {

    @Configurable
    @Configurable.Comment("Enables hit effects such as bleeds, fractures and other effects")
    @Configurable.Synchronized
    public boolean enableStatusEffects = true;

    @Configurable
    @Configurable.Comment("Enables transferring of status effects from items/projectiles to hit entities")
    public boolean enableItemDamageStatusEffects = true;

    @Configurable
    @Configurable.Range(min = 200)
    @Configurable.Comment("Default duration of item/projectile side effects")
    public int itemStatusEffectDuration = Duration.minutes(2).tickValue();

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.001F)
    @Configurable.Comment("Blood loss per minute for light bleeds")
    public float lightBleedAmount = 0.1F;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.001F)
    @Configurable.Comment("Blood loss per minute for heavy bleeds")
    public float heavyBleedAmount = 1.0F;

    // used in player.json data
    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Base chance for getting any type of bleed from '#medsystem:bleed_causing' damage type")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float playerBleedChance = 0.025F;

    // used in player.json data
    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Base chance for getting fracture from '#medsystem:fracture' damage type")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float playerFractureChance = 0.03F;

    // used in player.json data
    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Base chance for getting fracture from '#minecraft:is_fall' damage type")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float playerFractureFallChance = 0.035F;

    // used in player.json data
    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment({
            "Scaling function for 'Fall fracture chance' value",
            "Formula: (SCALE * fallDistance) * baseChance"
    })
    public float playerFractureFallDistanceScale = 0.75F;

    @Configurable
    public ItemStatusEffectConfig swordStatusEffects = new ItemStatusEffectConfig(0.10F, 0.04F, 0.0F);

    @Configurable
    public ItemStatusEffectConfig axeStatusEffects = new ItemStatusEffectConfig(0.10F, 0.02F, 0.10F);

    @Configurable
    public ItemStatusEffectConfig bluntStatusEffects = new ItemStatusEffectConfig(0.05F, 0.0F, 0.10F);
}
