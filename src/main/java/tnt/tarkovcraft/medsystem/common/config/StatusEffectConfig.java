package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;

public final class StatusEffectConfig {

    @Configurable
    @Configurable.Comment("Enables entryPoint effects such as bleeds, fractures and other effects")
    @Configurable.Synchronized
    public boolean enableStatusEffects = true;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Base chance for getting any type of bleed from '#medsystem:bleed_causing' damage type")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float bleedChance = 0.025F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment({"Default duration of bleeding status effect", "-1 means infinite duration"})
    @Configurable.Range(min = -1)
    public int bleedDuration = -1;

    @Configurable
    @Configurable.Comment({"Configure max bleed duration for effect stacking", "Set to 0 to disable all limits"})
    @Configurable.Range(min = 0)
    public int maxBleedDuration = 0;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Bleed chance multiplier for items from '#medsystem:sharp_tools' tag - swords, spears, axes")
    @Configurable.DecimalRange(min = 1.0F, max = 10.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float sharpToolBleedMultiplier = 5.0F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Base chance for getting fracture from '#medsystem:fracture_causing' damage type")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float fractureChance = 0.03F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment({"Default duration of fracture status effect", "-1 means infinite duration"})
    @Configurable.Range(min = -1)
    public int fractureDuration = -1;

    @Configurable
    @Configurable.Comment({"Configure max fracture duration for effect stacking", "Set to 0 to disable all limits"})
    @Configurable.Range(min = 0)
    public int maxFractureDuration = 0;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Fracture chance multiplier for items from '#medsystem:blunt_tools' tag - shovels, pickaxes, hoes and mace")
    @Configurable.DecimalRange(min = 1.0F, max = 10.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float bluntToolFractureMultiplier = 5.0F;

    @Configurable
    @Configurable.Comment("Allows scaling of injury recovery status effects when getting the effect repeatedly")
    public boolean allowInjuryRecoveryScaling = true;

    @Configurable
    @Configurable.Comment({"Configure max injury recovery duration for effect stacking", "Set to 0 to disable all limits"})
    @Configurable.Range(min = 0)
    public int maxInjuryRecoveryDuration = 0;

    @Configurable
    public BleedConfiguration bleedConfiguration = new BleedConfiguration();

    public static int getStackedDuration(int effectDuration, int limit) {
        return limit > 0 ? Math.min(effectDuration, limit) : effectDuration;
    }
}
