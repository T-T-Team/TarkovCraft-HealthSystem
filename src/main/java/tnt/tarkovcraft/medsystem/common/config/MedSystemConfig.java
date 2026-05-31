package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.FieldVisibility;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;

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
    @Configurable.Comment(value = "Uses less hit traces for explosions to save performance", localize = true)
    public boolean useExplosionPerformanceMode = false;

    @Configurable
    @Configurable.Comment(value = "Allows interactions (such as healing) with other entities", localize = true)
    @Configurable.Synchronized
    public boolean allowThirdPartyEntityInteractions = true;

    @Configurable
    @Configurable.Comment(value = "Health related configurations", localize = true)
    public HealthConfig health = new HealthConfig();

    @Configurable
    public ArmorSystemConfig armor = new ArmorSystemConfig();

    @Configurable
    @Configurable.Comment(value = "Blood/Unconscious system related configurations", localize = true)
    public BloodSystemConfig bloodSystem = new BloodSystemConfig();

    @Configurable
    public StatusEffectConfig statusEffects = new StatusEffectConfig();

    @Configurable
    public BloodDecalConfig bloodDecals = new BloodDecalConfig();
}
