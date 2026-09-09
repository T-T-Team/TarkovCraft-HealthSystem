package tnt.tarkovcraft.medsystem.integration.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.core.api.SleepFunction;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

public record RegenerateBloodSleepFunction(float regenerationFactor) implements SleepFunction {

    public static final MapCodec<RegenerateBloodSleepFunction> CODEC = ExtraCodecs.POSITIVE_FLOAT
            .xmap(RegenerateBloodSleepFunction::new, RegenerateBloodSleepFunction::regenerationFactor).fieldOf("advance_speed");

    @Override
    public void apply(ServerPlayer player, long sleptDurationTicks) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(player);
        if (bloodSystem == null)
            return;
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        float regenerationAmount = definition.getBloodRegenerationAmount(player) * this.regenerationFactor;
        int recoveryCycles = (int) (sleptDurationTicks / 20L);
        bloodSystem.recoverBlood(regenerationAmount * recoveryCycles);
        bloodSystem.synchronizeImmediately(player);
    }

    @Override
    public MapCodec<? extends SleepFunction> codec() {
        return CODEC;
    }
}
