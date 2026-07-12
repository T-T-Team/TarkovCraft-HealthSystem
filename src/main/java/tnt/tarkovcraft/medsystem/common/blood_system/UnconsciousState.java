package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public final class UnconsciousState {

    public static final Codec<UnconsciousState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("wake_up_timer", 0).forGetter(t -> t.wakeUpTimer),
            Codec.INT.optionalFieldOf("unconscious_duration", 0).forGetter(t -> t.unconsciousDuration),
            Codec.INT.optionalFieldOf("invulnerable_duration", 0).forGetter(t -> t.invulnerableDuration),
            UnconsciousOptions.CODEC.optionalFieldOf("options", UnconsciousOptions.EMPTY).forGetter(t -> t.options),
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("pose_metadata", Collections.emptyMap()).forGetter(t -> t.poseMetadata)
    ).apply(instance, UnconsciousState::new));

    public static final int COLLAPSE_ANIM_DURATION = 15;

    private int wakeUpTimer;
    private int unconsciousDuration;
    private int invulnerableDuration;
    private UnconsciousOptions options;
    private final Map<String, Float> poseMetadata;

    private final List<Listener> listeners = new ArrayList<>();
    private Boolean lastUnconsciousState;

    public UnconsciousState(int wakeUpTimer, int unconsciousDuration, int invulnerableDuration, UnconsciousOptions options, Map<String, Float> poseMetadata) {
        this.wakeUpTimer = wakeUpTimer;
        this.unconsciousDuration = unconsciousDuration;
        this.invulnerableDuration = invulnerableDuration;
        this.options = options;
        this.poseMetadata = new HashMap<>(poseMetadata);
    }

    public static UnconsciousState createConscious() {
        return new UnconsciousState(0, 0, 0, UnconsciousOptions.EMPTY, Collections.emptyMap());
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void tick(LivingEntity entity) {
        boolean unconscious = this.wakeUpTimer > 0;
        if (this.lastUnconsciousState == null || unconscious != this.lastUnconsciousState) {
            this.notifyListeners(l -> l.onUnconsciousStateChanged(entity, unconscious));
        }
        this.lastUnconsciousState = unconscious;
        if (this.invulnerableDuration > 0 && !this.options.allowRescue()) {
            --this.invulnerableDuration;
            return;
        }
        ++this.unconsciousDuration;
        if (this.wakeUpTimer > 0 && --this.wakeUpTimer <= 0) {
            UnconsciousOptions previousOptions = this.options;
            this.options = UnconsciousOptions.EMPTY;
            this.notifyListeners(l -> l.onWakeUp(entity, previousOptions, this.unconsciousDuration));
        }
    }

    public boolean isUnconscious() {
        return this.wakeUpTimer > 0 && (this.invulnerableDuration <= 0 || this.options.allowRescue());
    }

    public UnconsciousOptions getUnconsciousOptions() {
        return this.isUnconscious() ? this.options : UnconsciousOptions.EMPTY;
    }

    public int getRemainingUnconsciousDuration() {
        return this.wakeUpTimer;
    }

    public void setUnconscious(int duration, UnconsciousOptions options, boolean force, @Nullable Map<String, Float> poseMetadata) {
        if (this.wakeUpTimer <= 0) {
            this.unconsciousDuration = 0;
        }
        if (force) {
            this.invulnerableDuration = 0;
        }
        this.wakeUpTimer = Math.max(0, duration);
        this.options = options;
        if (this.wakeUpTimer > 0) {
            if (poseMetadata != null) {
                this.poseMetadata.putAll(poseMetadata);
            }
        } else {
            this.poseMetadata.clear();
        }
    }

    public void setInvulnerableDuration(int duration) {
        this.invulnerableDuration = duration;
    }

    public UnconsciousAnimationState calculateAnimationState(float delta) {
        int lastUnconsciousTime = Math.max(0, this.unconsciousDuration - 1);
        float start = this.getCollapseAmount(lastUnconsciousTime);
        float end = this.getCollapseAmount(this.unconsciousDuration);
        float collapse = Mth.lerp(delta, start, end);
        return new UnconsciousAnimationState(collapse, this.poseMetadata);
    }

    private void notifyListeners(Consumer<Listener> event) {
        this.listeners.forEach(event);
    }

    private float getCollapseAmount(int value) {
        return Mth.clamp((float) value / (float) COLLAPSE_ANIM_DURATION, 0.0F, 1.0F);
    }

    public interface Listener {
        default void onUnconsciousStateChanged(LivingEntity entity, boolean unconscious) {}
        default void onWakeUp(LivingEntity entity, UnconsciousOptions options, int totalUnconsciousDuration) {}
    }
}
