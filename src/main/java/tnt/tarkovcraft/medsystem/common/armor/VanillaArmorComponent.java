package tnt.tarkovcraft.medsystem.common.armor;

import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

public class VanillaArmorComponent implements ArmorComponent {

    public static final VanillaArmorComponent INSTANCE = new VanillaArmorComponent();

    VanillaArmorComponent() {
    }

    @Override
    public void applyItemDamage(ArmorHurtEvent event, DamageContext context) {
        // keep original
    }

    @Override
    public void applyDamageReduction(LivingIncomingDamageEvent event, DamageContext context) {
        // keep original
    }
}
