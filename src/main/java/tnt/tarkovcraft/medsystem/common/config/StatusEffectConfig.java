package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;

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

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment(value = {"Default duration of bleeding status effect", "-1 means infinite duration"}, localize = true)
    @Configurable.Range(min = -1)
    public int bleedDuration = -1;

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

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Comment("Fracture chance multiplier for items from '#medsystem:blunt_tools' tag - shovels, pickaxes, hoes and mace")
    @Configurable.DecimalRange(min = 1.0F, max = 10.0F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Gui.Slider
    public float bluntToolFractureMultiplier = 5.0F;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Range(min = 0)
    public int lightBleedChance = 50;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Range(min = 0)
    public int moderateBleedChance = 30;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Range(min = 0)
    public int heavyBleedChance = 15;

    @SuppressWarnings("unused")
    @Configurable
    @Configurable.Range(min = 0)
    public int criticalBleedChance = 5;
}
