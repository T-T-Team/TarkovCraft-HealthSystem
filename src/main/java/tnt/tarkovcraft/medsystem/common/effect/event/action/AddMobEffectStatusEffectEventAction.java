package tnt.tarkovcraft.medsystem.common.effect.event.action;

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
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunction;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventActions;

import java.util.Collections;
import java.util.List;

public record AddMobEffectStatusEffectEventAction(Holder<MobEffect> type, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, List<StatusEffectEventFunction> durationModifiers, List<StatusEffectEventFunction> amplifierModifiers) implements StatusEffectEventAction {

    public static final MapCodec<AddMobEffectStatusEffectEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(AddMobEffectStatusEffectEventAction::type),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 600).forGetter(AddMobEffectStatusEffectEventAction::duration),
            ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(AddMobEffectStatusEffectEventAction::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", true).forGetter(AddMobEffectStatusEffectEventAction::ambient),
            Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(AddMobEffectStatusEffectEventAction::visible),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(AddMobEffectStatusEffectEventAction::showIcon),
            StatusEffectEventFunctionType.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddMobEffectStatusEffectEventAction::durationModifiers),
            StatusEffectEventFunctionType.CODEC.listOf().optionalFieldOf("amplifier_modifiers", Collections.emptyList()).forGetter(AddMobEffectStatusEffectEventAction::amplifierModifiers)
    ).apply(instance, AddMobEffectStatusEffectEventAction::new));

    @Override
    public boolean apply(StatusEffectEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        MobEffectInstance effectInstance = this.createMobEffectInstance(ctx);
        if (!entity.level().isClientSide())
            entity.addEffect(effectInstance);
        return true;
    }

    @Override
    public StatusEffectEventActionType<?> getType() {
        return MedSystemStatusEffectEventActions.ADD_MOB_EFFECT.value();
    }

    private MobEffectInstance createMobEffectInstance(StatusEffectEventContext context) {
        int duration = Mth.floor(StatusEffectEventFunctionType.applyFunctions(this.duration, context, this.durationModifiers));
        int amplifier = Mth.floor(StatusEffectEventFunctionType.applyFunctions(this.amplifier, context, this.amplifierModifiers));
        return new MobEffectInstance(this.type, duration, amplifier, this.ambient, this.visible, this.showIcon);
    }
}
