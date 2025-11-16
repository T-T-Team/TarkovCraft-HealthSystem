package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class HeavyBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<HeavyBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, HeavyBleedStatusEffect::new));
    private static final Component HINT = Component.translatable("status_effect.medsystem.heavy_bleed.heal_hint").withStyle(ChatFormatting.DARK_GRAY);

    public HeavyBleedStatusEffect(int duration, Optional<UUID> owner) {
        super(duration, owner);
    }

    public HeavyBleedStatusEffect(int duration) {
        super(duration);
    }

    public static HeavyBleedStatusEffect createTemplate() {
        return new HeavyBleedStatusEffect(-1);
    }

    @Override
    public long getDamageInterval() {
        return 30L;
    }

    @Override
    public float getDamageAmount() {
        return 0.75F;
    }

    @Override
    public float getPerMinuteBloodLossAmount(LivingEntity entity) {
        return MedicalSystem.getConfig().statusEffects.heavyBleedAmount;
    }

    @Override
    public StatusEffect copy() {
        return new HeavyBleedStatusEffect(this.getDuration());
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        submitter.submit(Duration.seconds(5), new FreshWoundStatusEffect(Duration.minutes(5).tickValue()));
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(HINT);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.HEAVY_BLEED.value();
    }
}
