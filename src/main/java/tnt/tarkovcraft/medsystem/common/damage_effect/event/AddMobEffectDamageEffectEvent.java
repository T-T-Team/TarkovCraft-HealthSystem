package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

public record AddMobEffectDamageEffectEvent(Holder<MobEffect> type, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) implements DamageEffectEvent {

    public static final MapCodec<AddMobEffectDamageEffectEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(AddMobEffectDamageEffectEvent::type),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 600).forGetter(AddMobEffectDamageEffectEvent::duration),
            ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(AddMobEffectDamageEffectEvent::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(AddMobEffectDamageEffectEvent::ambient),
            Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(AddMobEffectDamageEffectEvent::visible),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(AddMobEffectDamageEffectEvent::showIcon)
    ).apply(instance, AddMobEffectDamageEffectEvent::new));

    @Override
    public void apply(DamageEffectContext context) {
        LivingEntity entity = context.target();
        MobEffectInstance effectInstance = this.createMobEffectInstance();
        if (!entity.level().isClientSide())
            entity.addEffect(effectInstance);
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.ADD_MOB_EFFECT.value();
    }

    private MobEffectInstance createMobEffectInstance() {
        return new MobEffectInstance(this.type, this.duration, this.amplifier, this.ambient, this.visible, this.showIcon);
    }
}
