package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class FreshWoundStatusEffect extends StatusEffect {

    public static final MapCodec<FreshWoundStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.FLOAT.optionalFieldOf("bleedChance", 0.0F).forGetter(t -> t.bleedChance)
    ).apply(instance, FreshWoundStatusEffect::new));

    private float bleedChance;

    public FreshWoundStatusEffect(int duration, int delay) {
        this(duration, delay, 0.0F);
    }

    public FreshWoundStatusEffect(int duration, int delay, float bleedChance) {
        super(duration, delay);
        this.bleedChance = bleedChance;
    }

    @Override
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (entity.isSprinting()) {
            this.bleedChance += 0.00035F;
            if (this.bleedChance >= 1.0F) {
                this.markForRemoval();
            }
        }
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        RandomSource source = entity.getRandom();
        if (source.nextFloat() < this.bleedChance) {
            return new LightBleedStatusEffect(-1, 0);
        }
        return null;
    }

    @Override
    public StatusEffect copy() {
        return new FreshWoundStatusEffect(this.getDuration(), this.getDelay(), this.bleedChance);
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("status_effect.medsystem.fresh_wound.info").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRESH_WOUND.value();
    }
}
