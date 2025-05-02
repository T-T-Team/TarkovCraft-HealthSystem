package tnt.tarkovcraft.medsystem.api;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

import java.util.Set;
import java.util.function.Consumer;

public interface ArmorComponent {

    boolean useVanillaArmorDamage();

    void collectAffectedBodyPartsWithProtection(Consumer<BodyPartGroup> register, LivingEntity entity, DamageContext context);

    float handleArmorReductions(LivingEntity entity, DamageContext ctx, Set<EquipmentSlot> protectedSlots, float incomingDamage, FloatConsumer damageProvider, Consumer<IReductionFunction> reductionProvider);
}
