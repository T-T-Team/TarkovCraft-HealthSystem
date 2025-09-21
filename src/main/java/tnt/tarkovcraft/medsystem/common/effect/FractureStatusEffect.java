package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.Collection;
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
        return new FractureStatusEffect(this.getDuration());
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }

    @Override
    public Collection<PostEffect> onRemoved(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        return null;
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
