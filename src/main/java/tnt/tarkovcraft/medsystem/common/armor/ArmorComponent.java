package tnt.tarkovcraft.medsystem.common.armor;

import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public interface ArmorComponent {

    void applyItemDamage(ArmorHurtEvent event);

    void applyDamageReduction(LivingIncomingDamageEvent event);
}
