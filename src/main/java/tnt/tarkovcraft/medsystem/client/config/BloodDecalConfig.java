package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.validate.ValidationResult;
import dev.toma.configuration.config.validate.Validator;
import dev.toma.configuration.config.value.IConfigValueReadable;
import dev.toma.configuration.util.ConfigurationHelper;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.common.data.duration.Duration;

public final class BloodDecalConfig {

    @Configurable
    @Configurable.Comment("Toggles all blood decals")
    public boolean enableBloodDecals = true;

    @Configurable
    @Configurable.StringPattern(ConfigurationHelper.SIMPLE_RGB_PATTERN)
    @Configurable.Gui.ColorValue
    public String bloodDecalColor = "#B20000";

    @Configurable
    @Configurable.Range(min = 100, max = 72000)
    @Configurable.Validate(LifetimeValidator.class)
    public int bloodDecalLifetime = Duration.minutes(1).tickValue();

    @Configurable
    @Configurable.DecimalRange(min = 0.10F, max = 0.35F)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00")
    @Configurable.Comment("Blood decal rendering scale")
    public float bloodDecalScale = 0.15F;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.0F)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment("Lifetime percentage at which decals will start to fade out")
    public float bloodDecalFadeOutAt = 0.25F;

    @Configurable
    @Configurable.Range(min = 0, max = 5)
    @Configurable.Gui.Slider
    @Configurable.Comment("How many decals will be spawned by each heavy bleed tick")
    public int heavyBleedDecalCount = 3;

    @Configurable
    @Configurable.Range(min = 0, max = 5)
    @Configurable.Gui.Slider
    @Configurable.Comment("How many decals will be spawned by each light bleed tick")
    public int lightBleedDecalCount = 1;

    @Configurable
    @Configurable.DecimalRange(min = 0.25F)
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment({"How much damage entity needs to receive in order for decal to appear", "At most 5 decals will be spawned from single attack"})
    public float damageDecalScale = 3.0F;

    @Configurable
    @Configurable.Range(min = 0, max = 15)
    @Configurable.Gui.Slider
    @Configurable.Comment({
            "Maximum amount of decals which can appear when damaging entities",
            "Set to 0 to disable damage decals"
    })
    public int maxDamageDecalsPerHit = 5;

    @Configurable
    @Configurable.DecimalRange(min = 0.0F, max = 1.5F)
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Gui.Slider
    @Configurable.Comment("How much motion is applied to decals on received damage")
    public float damageMotionScale = 0.1F;

    public static final class LifetimeValidator implements Validator<Integer> {

        private static final int WARNING_THRESHOLD = Duration.minutes(20).tickValue();
        private static final Component MESSAGE = Component.translatable("label.medsystem.validation.config.decal_lifetime");

        @Override
        public ValidationResult validate(Integer integer, IConfigValueReadable<Integer> iConfigValueReadable) {
            if (integer >= WARNING_THRESHOLD) {
                return ValidationResult.warning(MESSAGE);
            }
            return ValidationResult.success();
        }
    }
}
