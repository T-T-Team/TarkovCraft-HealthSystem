package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.EntityAttributeData;
import tnt.tarkovcraft.core.common.attribute.modifier.AddValueModifier;
import tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.UUID;

public class PainReliefEffect extends AttributeModifyingStatusEffect {

    public static final MapCodec<PainReliefEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, PainReliefEffect::new));
    public static final UUID PAIN_RELIEF_MODIFIER_ID = UUID.fromString("03d3708f-37bf-42a9-8599-b8af97cc7b4f");

    public PainReliefEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public UUID getUniqueModifierUUID() {
        return PAIN_RELIEF_MODIFIER_ID;
    }

    @Override
    public Holder<Attribute> getAttribute() {
        return MedSystemAttributes.PAIN_RELIEF;
    }

    @Override
    public AttributeModifier createModifier(UUID uuid, LivingEntity entity, EntityAttributeData attributeData, Context context) {
        return new AddValueModifier(uuid, 1);
    }

    @Override
    public StatusEffect copy() {
        return new PainReliefEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.PAIN_RELIEF.value();
    }
}
