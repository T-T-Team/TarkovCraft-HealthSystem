package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.FractureStatusEffect;

import java.util.Optional;

public class ItemStatusEffectConfig {

    @Configurable
    @Configurable.Comment("Chance to get light bleed on hit from this item")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    public float lightBleedChance;

    @Configurable
    @Configurable.Comment("Chance to get heavy bleed on hit from this item")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    public float heavyBleedChance;

    @Configurable
    @Configurable.Comment("Chance to fracture on hit from this item")
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    public float fractureChance;

    public ItemStatusEffectConfig(float lightBleedChance, float heavyBleedChance, float fractureChance) {
        this.lightBleedChance = lightBleedChance;
        this.heavyBleedChance = heavyBleedChance;
        this.fractureChance = fractureChance;
    }

    public SideEffectHolder.Builder apply(SideEffectHolder.Builder builder, int duration) {
        if (this.lightBleedChance > 0.0F) {
            builder.immediate(this.lightBleedChance, duration, BleedStatusEffect.defaultLightBleed(duration, Optional.empty()));
        }
        if (this.heavyBleedChance > 0.0F) {
            builder.immediate(this.heavyBleedChance, duration, BleedStatusEffect.defaultHeavyBleed(duration, Optional.empty()));
        }
        if (this.fractureChance > 0.0F) {
            builder.infinite(this.fractureChance, FractureStatusEffect.createTemplate());
        }
        return builder;
    }
}
