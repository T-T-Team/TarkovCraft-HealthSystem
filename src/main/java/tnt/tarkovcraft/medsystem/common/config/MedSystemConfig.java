package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;

@Config(id = MedicalSystem.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    @Configurable.Comment({
            "Defines armor calculation logic with custom health system",
            "SIMULATED - modular armor with fully simulated logic such as deflections, blunt damage and so on",
            "MODULAR - modular armor - getting hit in head damages only helmet",
            "MODULAR_BOOSTED - same as above, but each armor piece has 150% additional protection",
            "VANILLA - vanilla armor calculation, full armor is used for any damage calculations"
    })
    @Configurable.UpdateRestriction(UpdateRestrictions.MAIN_MENU)
    public ArmorSystem armorSystem = ArmorSystem.SIMULATED;

    @Configurable
    @Configurable.DecimalRange(min = 0.15, max = 3.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00#")
    @Configurable.Comment("Damage scale for explosions")
    public float explosionDamageScale = 0.6F;

    @Configurable
    @Configurable.Comment("Health will be primarily recovered into vital parts")
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment("Threshold for prioritized vital body part health recovery")
    public float vitalBodyPartHealthTrigger = 0.75F;

    @Configurable
    @Configurable.Comment("Allows scaling of injury recovery status effects when getting the effect repeatedly")
    public boolean allowInjuryRecoveryScaling = true;

    @Configurable
    @Configurable.Comment("Vanilla tools will have chance to cause some negative effects such as bleeds or fractures")
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    public boolean addHitEffectsToVanillaItems = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Enables blood system simulation")
    public boolean useBloodSystem = true;

    @Configurable
    @Configurable.Comment({
            "Defines handling of unconscious bleed out stage when the affected player has too low blood level to wake up on their own",
            "When disabled, bleed out damage will be applied immediately after losing ability to wake up"
    })
    public UnconsciousMode unconsciousMode = UnconsciousMode.ALLOW;

    @Configurable
    @Configurable.Comment("Allows transition to unconscious state when losing limb")
    public boolean allowUnconsciousOnLimbLost = true;

    @Configurable
    @Configurable.DecimalRange(min = 0.0, max = 1.0)
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Gui.Slider
    @Configurable.Comment("Chance specifying if you can enter unconscious state instead of dying so that you may be rescued by your friends")
    public float unconsciousOnDeathChance = 1.0F;

    @Configurable
    @Configurable.Range(min = 10)
    @Configurable.Comment("Rescue waiting period before dying while in death unconscious state")
    public int rescueWaitDuration = 150; // 2.5 minutes

    @Configurable
    @Configurable.Comment("Will prevent entering unconscious state on death if your head body part is dead too")
    public boolean allowUnconsciousOnHeadDeath = true;

    @Configurable
    public StatusEffectConfig statusEffects = new StatusEffectConfig();
}
