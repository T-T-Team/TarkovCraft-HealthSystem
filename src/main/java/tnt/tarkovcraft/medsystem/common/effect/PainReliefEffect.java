package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
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

    public static final MapCodec<PainReliefEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(StatusEffect::getDuration)
    ).apply(instance, PainReliefEffect::new));
    public static final UUID PAIN_RELIEF_MODIFIER_ID = UUID.fromString("03d3708f-37bf-42a9-8599-b8af97cc7b4f");

    private int duration;

    public PainReliefEffect(int duration) {
        this.duration = duration;
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
    public int getDuration() {
        return duration;
    }

    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.PAIN_RELIEF.value();
    }
}
