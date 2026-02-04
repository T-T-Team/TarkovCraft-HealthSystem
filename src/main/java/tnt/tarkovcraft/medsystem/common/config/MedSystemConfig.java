package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.FieldVisibility;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;

@Config(id = MedSystemConstants.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    @Configurable.Synchronized
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    @Configurable.Comment("Forces player rotation synchronization between server and clients")
    public boolean forceEntityRotationSynchronization = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0, max = 0.5)
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment("Expands entity hitbox for projectile detection purposes by this amount in all directions")
    public float defaultEntityHitboxInflation = 0.2F;

    @Configurable
    @Configurable.Comment({
            "Defines armor calculation logic with custom health system",
            "SIMULATED - modular armor with fully simulated logic such as deflections, blunt damage and so on",
            "MODULAR - modular armor - getting entryPoint in head damages only helmet",
            "MODULAR_BOOSTED - same as above, but each armor piece has 150% additional protection",
            "VANILLA - vanilla armor calculation, full armor is used for any damage calculations"
    })
    @Configurable.UpdateRestriction(UpdateRestrictions.MAIN_MENU)
    public ArmorSystem armorSystem = ArmorSystem.SIMULATED;

    @Configurable
    @Configurable.Comment("Health will be primarily recovered into vital parts")
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.Comment("Allows interactions (such as healing) with other entities")
    @Configurable.Synchronized
    public boolean allowThirdPartyEntityInteractions = true;

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
    @Configurable.Comment("Blood/Unconscious system related configurations")
    public BloodSystemConfig bloodSystem = new BloodSystemConfig();

    @Configurable
    public StatusEffectConfig statusEffects = new StatusEffectConfig();

    @Configurable
    public BloodDecalConfig bloodDecals = new BloodDecalConfig();
}
