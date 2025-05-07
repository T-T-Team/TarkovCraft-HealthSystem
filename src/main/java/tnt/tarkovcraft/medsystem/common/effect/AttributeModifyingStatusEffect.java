package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeInstance;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.attribute.EntityAttributeData;
import tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;

import java.util.UUID;

public abstract class AttributeModifyingStatusEffect implements StatusEffect {

    public abstract UUID getUniqueModifierUUID();

    public abstract Holder<Attribute> getAttribute();

    public abstract AttributeModifier createModifier(UUID uuid, LivingEntity entity, EntityAttributeData attributeData, Context context);

    @Override
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (AttributeSystem.isEnabledForEntity(entity)) {
            EntityAttributeData data = AttributeSystem.getAttributes(entity);
            AttributeInstance instance = data.getAttribute(this.getAttribute());
            UUID modifierUUID = getUniqueModifierUUID();
            if (!instance.hasModifier(modifierUUID)) {
                AttributeModifier modifier = this.createModifier(modifierUUID, entity, data, context);
                instance.addModifier(modifier);
            }
        }
    }

    @Override
    public void onRemoved(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (AttributeSystem.isEnabledForEntity(entity)) {
            EntityAttributeData data = AttributeSystem.getAttributes(entity);
            AttributeInstance instance = data.getAttribute(this.getAttribute());
            instance.removeModifier(this.getUniqueModifierUUID());
        }
    }
}
