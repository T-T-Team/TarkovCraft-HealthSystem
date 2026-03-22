package tnt.tarkovcraft.medsystem.common.blood_system;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.init.CoreAttributes;
import tnt.tarkovcraft.core.common.pose.EntityPoseManager;
import tnt.tarkovcraft.core.util.helper.EntityHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.config.BloodSystemConfig;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousEntityPose;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousSittingEntityPose;
import tnt.tarkovcraft.medsystem.network.message.S2C_RefreshEntityDimensions;

import static tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier.multiplier;

public final class UnconsciousModeHelper {

    public static final ResourceLocation UNCONSCIOUS_ATTRIBUTE_MODIFIER = MedicalSystem.resource("unconscious");

    public static void onChanged(boolean unconscious, LivingEntity entity, EntityBloodSystem bloodSystem) {
        updateEntityDimensions(entity);
        if (entity.level().isClientSide())
            return;
        if (unconscious)
            onUnconsciousModeEnabled(entity, bloodSystem);
        else
            onUnconsciousModeDisabled(entity, bloodSystem);
    }

    public static void updateEntityDimensions(LivingEntity entity) {
        entity.refreshDimensions();
        S2C_RefreshEntityDimensions.broadcast(entity);
    }

    public static void onUnconsciousModeEnabled(LivingEntity entity, EntityBloodSystem bloodSystem) {
        BloodSystemConfig config = MedicalSystem.getConfig().bloodSystem;

        EntityPoseManager.setEntityPose(entity, entity.getVehicle() != null && entity.getVehicle().shouldRiderSit()
                ? UnconsciousSittingEntityPose.INSTANCE
                : UnconsciousEntityPose.INSTANCE
        );

        AttributeSystem.addModifier(entity, CoreAttributes.HEARING, multiplier(UNCONSCIOUS_ATTRIBUTE_MODIFIER, config.unconsciousSoundVolumeScale), true);
        AttributeSystem.addModifier(entity, CoreAttributes.HEARING_DISTORTION, multiplier(UNCONSCIOUS_ATTRIBUTE_MODIFIER, config.unconsciousSoundPitchScale), true);

        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
        entity.setSprinting(false);
        entity.ejectPassengers();

        RandomSource random = entity.getRandom();
        if (random.nextFloat() < config.unconsciousHeldItemDropChance) {
            EntityHelper.dropEquippedItem(entity, EquipmentSlot.MAINHAND);
            EntityHelper.dropEquippedItem(entity, EquipmentSlot.OFFHAND);
        }

        AttributeMap attributes = entity.getAttributes();
        addUnconsciousAttributeModifier(attributes, Attributes.MOVEMENT_SPEED);
        addUnconsciousAttributeModifier(attributes, Attributes.JUMP_STRENGTH);
        addUnconsciousAttributeModifier(attributes, Attributes.STEP_HEIGHT);
        addUnconsciousAttributeModifier(attributes, Attributes.ATTACK_SPEED);
        addUnconsciousAttributeModifier(attributes, Attributes.BLOCK_BREAK_SPEED);
        addUnconsciousAttributeModifier(attributes, Attributes.BLOCK_INTERACTION_RANGE);
        NeoForge.EVENT_BUS.post(new BloodSystemEvent.UnconsciousStart(entity, bloodSystem));
    }

    public static void onUnconsciousModeDisabled(LivingEntity entity, EntityBloodSystem bloodSystem) {
        EntityPoseManager.clearEntityPose(entity);

        AttributeSystem.removeModifier(entity, CoreAttributes.HEARING, UNCONSCIOUS_ATTRIBUTE_MODIFIER);
        AttributeSystem.removeModifier(entity, CoreAttributes.HEARING_DISTORTION, UNCONSCIOUS_ATTRIBUTE_MODIFIER);

        AttributeMap attributes = entity.getAttributes();
        removeUnconsciousAttributeModifier(attributes, Attributes.MOVEMENT_SPEED);
        removeUnconsciousAttributeModifier(attributes, Attributes.JUMP_STRENGTH);
        removeUnconsciousAttributeModifier(attributes, Attributes.STEP_HEIGHT);
        removeUnconsciousAttributeModifier(attributes, Attributes.ATTACK_SPEED);
        removeUnconsciousAttributeModifier(attributes, Attributes.BLOCK_BREAK_SPEED);
        removeUnconsciousAttributeModifier(attributes, Attributes.BLOCK_INTERACTION_RANGE);
        NeoForge.EVENT_BUS.post(new BloodSystemEvent.UnconsciousEnd(entity, bloodSystem));
    }

    public static void addUnconsciousAttributeModifier(AttributeMap attributes, Holder<Attribute> attribute) {
        addUnconsciousAttributeModifier(attributes, attribute, -1.0, false);
    }

    public static void addUnconsciousAttributeModifier(AttributeMap attributes, Holder<Attribute> attribute, double value, boolean replace) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (!instance.hasModifier(UNCONSCIOUS_ATTRIBUTE_MODIFIER)) {
            instance.addTransientModifier(new AttributeModifier(UNCONSCIOUS_ATTRIBUTE_MODIFIER, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (replace) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(UNCONSCIOUS_ATTRIBUTE_MODIFIER, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void removeUnconsciousAttributeModifier(AttributeMap attributes, Holder<Attribute> attribute) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance.hasModifier(UNCONSCIOUS_ATTRIBUTE_MODIFIER)) {
            instance.removeModifier(UNCONSCIOUS_ATTRIBUTE_MODIFIER);
        }
    }
}
