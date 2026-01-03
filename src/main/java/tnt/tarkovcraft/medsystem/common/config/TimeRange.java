package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import net.minecraft.util.RandomSource;

public final class TimeRange {

    @Configurable
    @Configurable.Range(min = 0)
    public int minDuration;

    @Configurable
    @Configurable.Range(min = 0)
    public int maxDuration;

    public TimeRange(int minDuration, int maxDuration) {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int getDurationInSeconds(RandomSource random) {
        int min = Math.min(this.minDuration, this.maxDuration);
        int bound = Math.max(this.minDuration, this.maxDuration) - min;
        return 20 * (bound > 0 ? min + random.nextInt(bound) : min);
    }
}
