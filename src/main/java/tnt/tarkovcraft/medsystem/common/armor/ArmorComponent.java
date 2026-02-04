package tnt.tarkovcraft.medsystem.common.armor;

import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

public interface ArmorComponent {

    void applyItemDamage(ArmorHurtEvent event, DamageContext context);

    void applyDamageReduction(LivingIncomingDamageEvent event, DamageContext context);
}
