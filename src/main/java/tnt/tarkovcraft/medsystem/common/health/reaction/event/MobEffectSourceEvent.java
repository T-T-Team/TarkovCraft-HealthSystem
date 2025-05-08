package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactionResponses;

public class MobEffectSourceEvent implements HealthSourceEvent {

    public static final MapCodec<MobEffectSourceEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.effect),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(t -> t.duration),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("amplifier", 0).forGetter(t -> t.amplifier),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(t -> t.ambient),
            Codec.BOOL.optionalFieldOf("visible", false).forGetter(t -> t.visible),
            Codec.BOOL.optionalFieldOf("showIcon", false).forGetter(t -> t.showIcon)
    ).apply(instance, MobEffectSourceEvent::new));

    private final Holder<MobEffect> effect;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean visible;
    private final boolean showIcon;

    public MobEffectSourceEvent(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
    }

    @Override
    public void onReactionPassed(HealthEventSource source, Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        MobEffectInstance effectInstance = entity.getEffect(this.effect);
        if (effectInstance == null || effectInstance.getDuration() < 20 || effectInstance.getAmplifier() < this.amplifier) {
            MobEffectInstance instance = new MobEffectInstance(this.effect, this.duration, this.amplifier, this.ambient, this.visible, this.showIcon);
            entity.addEffect(instance);
        }
    }

    @Override
    public HealthSourceEventType<?> getType() {
        return MedSystemHealthReactionResponses.MOB_EFFECT.get();
    }
}
