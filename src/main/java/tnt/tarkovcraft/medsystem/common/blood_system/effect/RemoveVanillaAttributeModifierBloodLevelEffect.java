package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public record RemoveVanillaAttributeModifierBloodLevelEffect(Holder<Attribute> attribute, Identifier modifier) implements BloodLevelEffect {

    public static final MapCodec<RemoveVanillaAttributeModifierBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RemoveVanillaAttributeModifierBloodLevelEffect::attribute),
            Identifier.CODEC.fieldOf("modifier").forGetter(RemoveVanillaAttributeModifierBloodLevelEffect::modifier)
    ).apply(instance, RemoveVanillaAttributeModifierBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        entity.getAttribute(this.attribute).removeModifier(this.modifier);
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }
}
