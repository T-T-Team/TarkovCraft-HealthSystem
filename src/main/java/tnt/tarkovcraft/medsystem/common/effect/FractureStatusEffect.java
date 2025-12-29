package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class FractureStatusEffect extends EntityCausedStatusEffect {

    public static final MapCodec<FractureStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, FractureStatusEffect::new));
    private static final Component HINT = Component.translatable("status_effect.medsystem.fracture.heal_hint").withStyle(ChatFormatting.DARK_GRAY);

    public FractureStatusEffect(int duration, Optional<UUID> owner) {
        super(duration, owner);
    }

    public FractureStatusEffect(int duration) {
        super(duration);
    }

    public static FractureStatusEffect createTemplate() {
        return new FractureStatusEffect(-1);
    }

    @Override
    public StatusEffect copy() {
        return new FractureStatusEffect(this.getDuration(), Optional.ofNullable(this.getCausingEntity()));
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(HINT);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRACTURE.value();
    }
}
