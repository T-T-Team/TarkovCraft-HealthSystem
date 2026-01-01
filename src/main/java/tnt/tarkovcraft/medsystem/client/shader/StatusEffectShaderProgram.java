package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

public abstract class StatusEffectShaderProgram extends SimpleScalingShaderProgram {

    protected abstract Holder<StatusEffectType<?>> getStatusEffect();

    protected abstract float getGain();

    protected abstract float getDecay();

    @Override
    public void update(Minecraft client, LivingEntity entity) {
        super.update(client, entity);
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap map = container.getGlobalStatusEffects();
        if (this.canApply(entity, container, map) && map.hasEffect(this.getStatusEffect())) {
            this.strength = Math.min(1.0F, this.strength + this.getGain());
        } else if (this.strength > 0.0F) {
            this.strength = Math.max(0.0F, this.strength - this.getDecay());
        }
        this.strength = Mth.clamp(this.strength, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation postChainId() {
        return this.getStatusEffect().getKey().location();
    }

    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return true;
    }
}
