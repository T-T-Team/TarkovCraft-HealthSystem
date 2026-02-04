package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResult;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DamageContext {

    private final HitCalculationContext context;
    private final HitCalculationResult result;
    private final Set<EquipmentSlot> damagedSlots = EnumSet.noneOf(EquipmentSlot.class);

    public DamageContext(HitCalculationContext context, HitCalculationResult result) {
        this.context = context;
        this.result = result;
    }

    public HitCalculationContext getCalculationContext() {
        return this.context;
    }

    public HitCalculationResult getCalculationResult() {
        return this.result;
    }

    public void addArmorDamageSlot(EquipmentSlot slot) {
        this.damagedSlots.add(slot);
    }

    public Set<EquipmentSlot> getArmorDamageSlots() {
        return this.damagedSlots;
    }

    @Nullable
    public SideEffectHolder getEffects() {
        return SideEffectHolder.fromDamage(this.context.source());
    }

    public DamageSource getSource() {
        return this.context.source();
    }

    public List<HitInfo> getHits() {
        return this.result.getHits();
    }

    public Map<Limb, Float> getDamage(float incomingTotalDamage) {
        DamageDistributor damageDistributor = this.result.getDamageDistributor();
        return damageDistributor.distribute(this, incomingTotalDamage);
    }
}
