package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.FieldVisibility;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;

@Config(id = MedSystemConstants.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    @Configurable.Comment(value = {"Debug renderer for last hit information", "Available only in singleplayer/lan worlds"}, localize = true)
    public boolean enableHitDebug = false;

    @Configurable
    @Configurable.Synchronized
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    @Configurable.Comment(value = "Forces player rotation synchronization between server and clients", localize = true)
    public boolean forceEntityRotationSynchronization = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0, max = 0.5)
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment(value = "Expands entity hitbox for projectile detection purposes by this amount in all directions", localize = true)
    public float defaultEntityHitboxInflation = 0.2F;

    @Configurable
    @Configurable.Comment(value = {
            "Defines armor calculation logic with custom health system",
            "SIMULATED - modular armor with fully simulated logic such as deflections, blunt damage and so on",
            "MODULAR - modular armor - getting entryPoint in head damages only helmet",
            "MODULAR_BOOSTED - same as above, but each armor piece has 150% additional protection",
            "VANILLA - vanilla armor calculation, full armor is used for any damage calculations"
    }, localize = true)
    @Configurable.UpdateRestriction(UpdateRestrictions.MAIN_MENU)
    public ArmorSystem armorSystem = ArmorSystem.SIMULATED;

    @Configurable
    @Configurable.Comment(value = "Health will be primarily recovered into vital parts", localize = true)
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.Comment(value = "Allows interactions (such as healing) with other entities", localize = true)
    @Configurable.Synchronized
    public boolean allowThirdPartyEntityInteractions = true;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(value = "Threshold for prioritized vital body part health recovery", localize = true)
    public float vitalBodyPartHealthTrigger = 0.75F;

    @Configurable
    @Configurable.Comment(value = "Allows scaling of injury recovery status effects when getting the effect repeatedly", localize = true)
    public boolean allowInjuryRecoveryScaling = true;

    @Configurable
    @Configurable.Comment(value = "Vanilla tools will have chance to cause some negative effects such as bleeds or fractures", localize = true)
    @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
    public boolean addHitEffectsToVanillaItems = true;

    @Configurable
    @Configurable.Comment(value = "Blood/Unconscious system related configurations", localize = true)
    public BloodSystemConfig bloodSystem = new BloodSystemConfig();

    @Configurable
    public StatusEffectConfig statusEffects = new StatusEffectConfig();
}
