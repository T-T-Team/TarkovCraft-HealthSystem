package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.LimbDamageSource;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.Optional;

public final class DeathBloodLevelEffect implements BloodLevelEffect {

    public static final DeathBloodLevelEffect INSTANCE = new DeathBloodLevelEffect();
    public static final MapCodec<DeathBloodLevelEffect> CODEC = MapCodec.unit(INSTANCE);

    private DeathBloodLevelEffect() {}

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        RegistryAccess access = entity.registryAccess();
        Optional<Entity> causingEntity = Optional.empty();
        Limb limb = null;
        if (HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthContainer.getAttached(entity);
            causingEntity = StatusEffectHelper.getAnyTaggedEffect(container, MedSystemTags.StatusEffects.IS_BLEED)
                    .flatMap(effect -> effect.getCausingEntity(level));
            limb = container.getRootLimb();
        }
        DamageSource source = limb != null
                ? new LimbDamageSource(MedSystemDamageTypes.of(access, MedSystemDamageTypes.BLEED), causingEntity.orElse(null), limb.getLimbCode())
                : MedSystemDamageTypes.causeBleedDamage(access, causingEntity);
        entity.hurtServer(level, source, 4.0F);
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }
}
