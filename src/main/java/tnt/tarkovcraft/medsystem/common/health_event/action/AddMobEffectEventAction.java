package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunction;

import java.util.Collections;
import java.util.List;

public record AddMobEffectEventAction(Holder<MobEffect> type, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, List<HealthEventFunction> durationModifiers, List<HealthEventFunction> amplifierModifiers) implements HealthEventAction {

    public static final MapCodec<AddMobEffectEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(AddMobEffectEventAction::type),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 600).forGetter(AddMobEffectEventAction::duration),
            ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(AddMobEffectEventAction::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(AddMobEffectEventAction::ambient),
            Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(AddMobEffectEventAction::visible),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(AddMobEffectEventAction::showIcon),
            HealthEventFunction.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddMobEffectEventAction::durationModifiers),
            HealthEventFunction.CODEC.listOf().optionalFieldOf("amplifier_modifiers", Collections.emptyList()).forGetter(AddMobEffectEventAction::amplifierModifiers)
    ).apply(instance, AddMobEffectEventAction::new));

    @Override
    public boolean apply(HealthEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        MobEffectInstance effectInstance = this.createMobEffectInstance(ctx);
        if (!entity.level().isClientSide())
            entity.addEffect(effectInstance);
        return true;
    }

    @Override
    public MapCodec<? extends HealthEventAction> codec() {
        return CODEC;
    }

    private MobEffectInstance createMobEffectInstance(HealthEventContext context) {
        int duration = Mth.floor(HealthEventFunction.applyFunctions(this.duration, context, this.durationModifiers));
        int amplifier = Mth.floor(HealthEventFunction.applyFunctions(this.amplifier, context, this.amplifierModifiers));
        return new MobEffectInstance(this.type, duration, amplifier, this.ambient, this.visible, this.showIcon);
    }
}
