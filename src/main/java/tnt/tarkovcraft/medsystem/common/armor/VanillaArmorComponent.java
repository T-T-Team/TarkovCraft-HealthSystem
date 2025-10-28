package tnt.tarkovcraft.medsystem.common.armor;

import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class VanillaArmorComponent implements ArmorComponent {

    public static final VanillaArmorComponent INSTANCE = new VanillaArmorComponent();

    VanillaArmorComponent() {
    }

    @Override
    public void applyItemDamage(ArmorHurtEvent event) {
        // keep original
    }

    @Override
    public void applyDamageReduction(LivingIncomingDamageEvent event) {
        // keep original
    }
}
