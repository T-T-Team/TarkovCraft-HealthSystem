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
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunction;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

import java.util.Collections;
import java.util.List;

public record AddMobEffectDamageEffectEvent(Holder<MobEffect> type, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, List<DamageEffectFunction> durationModifiers, List<DamageEffectFunction> amplifierModifiers) implements DamageEffectEvent {

    public static final MapCodec<AddMobEffectDamageEffectEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(AddMobEffectDamageEffectEvent::type),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 600).forGetter(AddMobEffectDamageEffectEvent::duration),
            ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(AddMobEffectDamageEffectEvent::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(AddMobEffectDamageEffectEvent::ambient),
            Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(AddMobEffectDamageEffectEvent::visible),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(AddMobEffectDamageEffectEvent::showIcon),
            DamageEffectFunctionType.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddMobEffectDamageEffectEvent::durationModifiers),
            DamageEffectFunctionType.CODEC.listOf().optionalFieldOf("amplifier_modifiers", Collections.emptyList()).forGetter(AddMobEffectDamageEffectEvent::amplifierModifiers)
    ).apply(instance, AddMobEffectDamageEffectEvent::new));

    @Override
    public void apply(DamageEffectContext context) {
        LivingEntity entity = context.target();
        MobEffectInstance effectInstance = this.createMobEffectInstance(context);
        if (!entity.level().isClientSide())
            entity.addEffect(effectInstance);
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.ADD_MOB_EFFECT.value();
    }

    private MobEffectInstance createMobEffectInstance(DamageEffectContext context) {
        int duration = DamageEffectFunctionType.applyFunctions(this.duration, context, this.durationModifiers);
        int amplifier = DamageEffectFunctionType.applyFunctions(this.amplifier, context, this.amplifierModifiers);
        return new MobEffectInstance(this.type, duration, amplifier, this.ambient, this.visible, this.showIcon);
    }
}
