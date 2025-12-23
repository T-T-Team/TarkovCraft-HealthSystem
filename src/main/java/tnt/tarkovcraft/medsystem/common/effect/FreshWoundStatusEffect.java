package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class FreshWoundStatusEffect extends StatusEffect {

    public static final MapCodec<FreshWoundStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.FLOAT.optionalFieldOf("bleedChance", 0.0F).forGetter(t -> t.bleedChance)
    ).apply(instance, FreshWoundStatusEffect::new));
    private static final Component INFO = Component.translatable("status_effect.medsystem.fresh_wound.info").withStyle(ChatFormatting.DARK_GRAY);

    private float bleedChance;

    public FreshWoundStatusEffect(int duration) {
        this(duration, 0.0F);
    }

    public FreshWoundStatusEffect(int duration, float bleedChance) {
        super(duration);
        this.bleedChance = bleedChance;
    }

    public static FreshWoundStatusEffect createTemplate() {
        return new FreshWoundStatusEffect(-1);
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (entity.isSprinting()) {
            this.bleedChance += 0.00035F;
            if (this.bleedChance >= 1.0F) {
                this.markForRemoval();
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        RandomSource source = entity.getRandom();
        if (source.nextFloat() < this.bleedChance) {
            submitter.submitImmediate(new LightBleedStatusEffect(-1));
        }
    }

    @Override
    public StatusEffect copy() {
        return new FreshWoundStatusEffect(this.getDuration(), this.bleedChance);
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(INFO);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRESH_WOUND.value();
    }
}
