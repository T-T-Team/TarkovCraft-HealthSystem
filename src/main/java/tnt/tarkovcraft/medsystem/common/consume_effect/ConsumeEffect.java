package tnt.tarkovcraft.medsystem.common.consume_effect;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public interface ConsumeEffect {

    Codec<ConsumeEffect> CODEC = MedSystemRegistries.CONSUME_EFFECT.byNameCodec().dispatch(ConsumeEffect::getType, ConsumeEffectType::codec);

    ConsumeEffectType<?> getType();

    boolean apply(final Level level, final ItemStack itemStack, final LivingEntity user);
}
