package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class StatusEffectConfig {

    @Configurable
    @Configurable.Comment(value = "Enables entryPoint effects such as bleeds, fractures and other effects", localize = true)
    @Configurable.Synchronized
    public boolean enableStatusEffects = true;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = "Base chance for getting any type of bleed from '#medsystem:bleed_causing' damage type", localize = true)
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float bleedChance = 0.025F;

    @Configurable
    @Configurable.Comment(value = {"Configure max bleed duration for effect stacking", "Set to 0 to disable all limits"}, localize = true)
    @Configurable.Range(min = 0)
    public int maxBleedDuration = 0;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = "Bleed chance multiplier for items from '#medsystem:sharp_tools' tag - swords, spears, axes", localize = true)
    @Configurable.DecimalRange(min = 1.0F, max = 10.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float sharpToolBleedMultiplier = 5.0F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = "Base chance for getting fracture from '#medsystem:fracture_causing' damage type", localize = true)
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float fractureChance = 0.03F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = {"Default duration of fracture status effect", "-1 means infinite duration"}, localize = true)
    @Configurable.Range(min = -1)
    public int fractureDuration = -1;

    @Configurable
    @Configurable.Comment(value = {"Configure max fracture duration for effect stacking", "Set to 0 to disable all limits"}, localize = true)
    @Configurable.Range(min = 0)
    public int maxFractureDuration = 0;

    @Configurable
    @Configurable.Comment(value = "How long it will take for fractures to heal after applying splints", localize = true)
    @Configurable.Range(min = 1)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    public int fractureRecoveryTime = Duration.minutes(5).tickValue();

    @Configurable
    @Configurable.Comment("How long it will take for fractures to heal after applying bandages")
    @Configurable.Range(min = 1)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    public int fractureRecoveryTimeBandage = Duration.minutes(15).tickValue();

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = "Fracture chance multiplier for items from '#medsystem:blunt_tools' tag - shovels, pickaxes, hoes and mace", localize = true)
    @Configurable.DecimalRange(min = 1.0F, max = 10.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float bluntToolFractureMultiplier = 5.0F;

    @Configurable
    @Configurable.Comment(value = "Allows scaling of injury recovery status effects when getting the effect repeatedly", localize = true)
    public boolean allowInjuryRecoveryScaling = true;

    @Configurable
    @Configurable.Comment(value = {"Configure max injury recovery duration for effect stacking", "Set to 0 to disable all limits"}, localize = true)
    @Configurable.Range(min = 0)
    public int maxInjuryRecoveryDuration = 0;

    @Configurable
    public BleedConfiguration bleedConfiguration = new BleedConfiguration();

    public static int getStackedDuration(int effectDuration, int limit) {
        return limit > 0 ? Math.min(effectDuration, limit) : effectDuration;
    }
}
