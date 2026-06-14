package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
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
        Optional<Entity> causingEntity = this.findBleedEffect(entity)
                .flatMap(effect -> effect.getCausingEntity(level));
        entity.hurt(MedSystemDamageTypes.causeBleedDamage(access, causingEntity), 4.0F);
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }

    private Optional<StatusEffect> findBleedEffect(LivingEntity entity) {
        if (HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthContainer.getAttached(entity);
            return StatusEffectHelper.getAnyTaggedEffect(container, MedSystemTags.StatusEffects.IS_BLEED);
        }
        return Optional.empty();
    }
}
