package tnt.tarkovcraft.medsystem.common.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.List;

public class VanillaArmorComponent implements ArmorComponent {

    public static final VanillaArmorComponent INSTANCE = new VanillaArmorComponent();

    VanillaArmorComponent() {
    }

    @Override
    public boolean shouldDeflectIncomingHit(DamageSource source, LivingEntity entity, List<HitResult> hits) {
        return false;
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
