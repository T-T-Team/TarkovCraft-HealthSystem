package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.attribute.EntityAttributeData;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public record RemoveAttributeModifierBloodLevelEffect(Holder<Attribute> attribute, Identifier modifier) implements BloodLevelEffect {

    public static final MapCodec<RemoveAttributeModifierBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CoreRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RemoveAttributeModifierBloodLevelEffect::attribute),
            Identifier.CODEC.fieldOf("modifier").forGetter(RemoveAttributeModifierBloodLevelEffect::modifier)
    ).apply(instance, RemoveAttributeModifierBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        EntityAttributeData data = AttributeSystem.getExistingAttributes(entity);
        if (data == null)
            return;
        data.getAttribute(this.attribute).removeModifier(this.modifier);
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }
}
