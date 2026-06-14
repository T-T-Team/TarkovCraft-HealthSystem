package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;

public interface BloodLevelEffect {

    Codec<BloodLevelEffect> CODEC = MedSystemRegistries.BLOOD_LEVEL_EFFECT.byNameCodec()
            .dispatch(BloodLevelEffect::codec, Function.identity());

    void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem);

    MapCodec<? extends BloodLevelEffect> codec();
}
