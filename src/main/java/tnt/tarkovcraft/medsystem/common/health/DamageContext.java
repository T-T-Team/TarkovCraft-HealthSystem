package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.advancements.criterion.LoseLimbTrigger;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResult;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;

import java.util.*;

public final class DamageContext {

    private HitCalculationContext context;
    private HitCalculationResult result;
    private final List<Limb> lostLimbs = new ArrayList<>();
    private final Set<EquipmentSlot> damagedSlots = EnumSet.noneOf(EquipmentSlot.class);

    public DamageContext(HitCalculationContext context, HitCalculationResult result) {
        this.context = context;
        this.result = result;
    }

    public static DamageContext createEmptyInstance() {
        return new DamageContext(null, null);
    }

    public void init(HitCalculationContext context, HitCalculationResult result) {
        this.context = context;
        this.result = result;
        this.lostLimbs.clear();
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

    public void addLostLimb(Limb limb) {
        this.lostLimbs.add(limb);
    }

    public int getLostLimbsCount() {
        return this.lostLimbs.size();
    }

    public void triggerAdvancements(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player))
            return;
        this.lostLimbs.forEach(limb -> LoseLimbTrigger.triggerCriterion(player, limb));
    }

    public void reset() {
        this.context = null;
        this.result = null;
        this.damagedSlots.clear();
    }
}
