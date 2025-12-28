package tnt.tarkovcraft.medsystem.common.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitResult;

import java.util.List;

public interface ArmorComponent {

    boolean shouldDeflectIncomingHit(DamageSource source, LivingEntity entity, List<HitResult> hits);

    void applyItemDamage(ArmorHurtEvent event, DamageContext context);

    void applyDamageReduction(LivingIncomingDamageEvent event, DamageContext context);
}
