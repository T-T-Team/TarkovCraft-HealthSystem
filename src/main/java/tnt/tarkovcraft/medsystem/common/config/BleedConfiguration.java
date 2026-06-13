package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;

public final class BleedConfiguration {

    @Configurable
    public BleedStageConfig lightBleed = new BleedStageConfig(50, 1.5F, -1, 60, 0.005F, 0, 1);

    @Configurable
    public BleedStageConfig moderateBleed = new BleedStageConfig(30, 2.5F, -1, 45, 0.010F, 0, 2);

    @Configurable
    public BleedStageConfig heavyBleed = new BleedStageConfig(15, 3.0F, -1, 30, 0.025F, 6000, 5);

    @Configurable
    public BleedStageConfig criticalBleed = new BleedStageConfig(5, 5.0F, -1, 10, 0.06F, 12000, 8);

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
        @Configurable.Range(min = 0)
        public int weight;

        @Configurable
        @Configurable.DecimalRange(min = 0.0F)
        public float minDamageThreshold;

        @Configurable
        @Configurable.Range(min = -1)
        @Configurable.Comment(value = {"Default duration of bleeding status effect", "-1 means infinite duration"}, localize = true)
        public int bleedDuration;

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

        public BleedStageConfig(int weight, float minDamageThreshold, int bleedDuration, int bleedInterval, float bleedAmount, int woundDuration, int decalCount) {
            this.weight = weight;
            this.minDamageThreshold = minDamageThreshold;
            this.bleedDuration = bleedDuration;
            this.bleedInterval = bleedInterval;
            this.bleedAmount = bleedAmount;
            this.woundDuration = woundDuration;
            this.decalCount = decalCount;
        }
    }
}
