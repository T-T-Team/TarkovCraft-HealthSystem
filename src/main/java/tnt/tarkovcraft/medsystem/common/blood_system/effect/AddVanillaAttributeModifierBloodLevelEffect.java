package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemBloodLevelEffects;

public record AddVanillaAttributeModifierBloodLevelEffect(Holder<Attribute> attribute, AttributeModifier modifier) implements BloodLevelEffect {

    public static final MapCodec<AddVanillaAttributeModifierBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AddVanillaAttributeModifierBloodLevelEffect::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(AddVanillaAttributeModifierBloodLevelEffect::modifier)
    ).apply(instance, AddVanillaAttributeModifierBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        AttributeInstance instance = entity.getAttribute(this.attribute);
        instance.addOrUpdateTransientModifier(this.modifier);
    }

    @Override
    public BloodLevelEffectType<?> getType() {
        return MedSystemBloodLevelEffects.ADD_VANILLA_ATTRIBUTE_MODIFIER.value();
    }
}
