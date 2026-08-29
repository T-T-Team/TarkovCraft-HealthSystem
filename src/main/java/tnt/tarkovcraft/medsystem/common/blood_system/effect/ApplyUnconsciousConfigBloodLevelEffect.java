package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.config.BloodSystemConfig;

public final class ApplyUnconsciousConfigBloodLevelEffect implements BloodLevelEffect {

    public static final ApplyUnconsciousConfigBloodLevelEffect INSTANCE = new ApplyUnconsciousConfigBloodLevelEffect();
    public static final MapCodec<ApplyUnconsciousConfigBloodLevelEffect> CODEC = MapCodec.unit(INSTANCE);

    private ApplyUnconsciousConfigBloodLevelEffect() {
    }

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        BloodSystemConfig config = MedicalSystem.getConfig().bloodSystem;
        int playerCount = level.getServer().getPlayerCount();
        if (!config.allowDownedSingleplayer && playerCount <= 1) {
            DeathBloodLevelEffect.INSTANCE.applyEffects(entity, level, bloodSystem);
        }
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }
}
