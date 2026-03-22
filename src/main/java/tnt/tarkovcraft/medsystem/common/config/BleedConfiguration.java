package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;

public final class BleedConfiguration {

    @Configurable
    public BleedStageConfig lightBleed = new BleedStageConfig(60, 0.005F, 0, 1);

    @Configurable
    public BleedStageConfig moderateBleed = new BleedStageConfig(45, 0.010F, 0, 2);

    @Configurable
    public BleedStageConfig heavyBleed = new BleedStageConfig(30, 0.025F, 6000, 5);

    @Configurable
    public BleedStageConfig criticalBleed = new BleedStageConfig(10, 0.06F, 12000, 8);

    public BleedStageConfig getLightBleed() {
        return this.lightBleed;
    }

    public BleedStageConfig getModerateBleed() {
        return this.moderateBleed;
    }

    public BleedStageConfig getHeavyBleed() {
        return this.heavyBleed;
    }

    public BleedStageConfig getCriticalBleed() {
        return this.criticalBleed;
    }

    public static final class BleedStageConfig {

        @Configurable
        @Configurable.Range(min = 5)
        public int bleedInterval;

        @Configurable
        @Configurable.DecimalRange(min = 0.0001F)
        @Configurable.Gui.NumberFormat("0.000#")
        public float bleedAmount;

        @Configurable
        @Configurable.Range(min = 0)
        public int woundDuration;

        @Configurable
        @Configurable.Range(min = 0, max = 16)
        @Configurable.Gui.Slider
        public int decalCount;

        public BleedStageConfig(int bleedInterval, float bleedAmount, int woundDuration, int decalCount) {
            this.bleedInterval = bleedInterval;
            this.bleedAmount = bleedAmount;
            this.woundDuration = woundDuration;
            this.decalCount = decalCount;
        }
    }
}
