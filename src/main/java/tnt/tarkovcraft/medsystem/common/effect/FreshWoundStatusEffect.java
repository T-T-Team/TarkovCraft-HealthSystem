package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class FreshWoundStatusEffect extends StatusEffect {

    public static final MapCodec<FreshWoundStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(instance.group(
            BleedStatusEffect.BleedType.CODEC.optionalFieldOf("bleed_type", BleedStatusEffect.BleedType.LIGHT).forGetter(t -> t.bleedType),
            Codec.FLOAT.optionalFieldOf("bleed_chance", 0.0F).forGetter(t -> t.bleedChance)
    )).apply(instance, FreshWoundStatusEffect::new));
    private static final Component INFO = Component.translatable("status_effect.medsystem.fresh_wound.info").withStyle(ChatFormatting.DARK_GRAY);

    private final BleedStatusEffect.BleedType bleedType;
    private float bleedChance;

    public FreshWoundStatusEffect(int duration, BleedStatusEffect.BleedType bleedType) {
        this(duration, bleedType, 0.0F);
    }

    public FreshWoundStatusEffect(int duration, BleedStatusEffect.BleedType bleedType, float bleedChance) {
        super(duration);
        this.bleedType = bleedType;
        this.bleedChance = bleedChance;
    }

    public static FreshWoundStatusEffect createTemplate(int duration) {
        return new FreshWoundStatusEffect(duration, BleedStatusEffect.BleedType.LIGHT);
    }

    public static FreshWoundStatusEffect createTemplate(BleedStatusEffect.BleedType bleedType) {
        return new FreshWoundStatusEffect(-1, bleedType);
    }

    @Override
    public void apply(StatusEffectContext context) {
        LivingEntity entity = context.entity();
        if (entity.isSprinting()) {
            this.bleedChance += 0.00035F;
            if (this.bleedChance >= 1.0F) {
                this.markForRemoval();
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
        LivingEntity entity = context.entity();
        RandomSource source = entity.getRandom();
        if (source.nextFloat() < this.bleedChance) {
            context.submitImmediate(BleedStatusEffect.createTemplate(-1, this.bleedType));
        }
    }

    @Override
    public StatusEffect copy() {
        return new FreshWoundStatusEffect(this.getDuration(), this.bleedType, this.bleedChance);
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(INFO);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRESH_WOUND.value();
    }

    public static FreshWoundStatusEffect merge(FreshWoundStatusEffect first, FreshWoundStatusEffect second) {
        BleedStatusEffect.BleedType bleedType = first.bleedType.ordinal() > second.bleedType.ordinal() ? first.bleedType : second.bleedType;
        int duration = sumEffectDurations(first, second);
        return new FreshWoundStatusEffect(duration, bleedType);
    }
}
