package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.UnconsciousMode;
import tnt.tarkovcraft.medsystem.common.init.MedSystemBloodLevelEffects;

public final class ApplyUnconsciousConfigBloodLevelEffect implements BloodLevelEffect {

    public static final ApplyUnconsciousConfigBloodLevelEffect INSTANCE = new ApplyUnconsciousConfigBloodLevelEffect();
    public static final MapCodec<ApplyUnconsciousConfigBloodLevelEffect> CODEC = MapCodec.unit(INSTANCE);

    private ApplyUnconsciousConfigBloodLevelEffect() {
    }

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        MedSystemConfig config = MedicalSystem.getConfig();
        UnconsciousMode mode = config.bloodSystem.bleedOutUnconsciousness;
        if (!mode.allowsUnconsciousState(level)) {
            DeathBloodLevelEffect.INSTANCE.applyEffects(entity, level, bloodSystem);
        }
    }

    @Override
    public BloodLevelEffectType<?> getType() {
        return MedSystemBloodLevelEffects.APPLY_UNCONSCIOUS_CONFIG.value();
    }
}
