package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemBloodLevelEffects;

public record AddAttributeModifierBloodLevelEffect(Holder<Attribute> attribute, AttributeModifier modifier) implements BloodLevelEffect {

    public static final MapCodec<AddAttributeModifierBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CoreRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AddAttributeModifierBloodLevelEffect::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(AddAttributeModifierBloodLevelEffect::modifier)
    ).apply(instance, AddAttributeModifierBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        AttributeSystem.addModifier(entity, this.attribute, this.modifier, true);
    }

    @Override
    public BloodLevelEffectType<?> getType() {
        return MedSystemBloodLevelEffects.ADD_ATTRIBUTE_MODIFIER.value();
    }
}
