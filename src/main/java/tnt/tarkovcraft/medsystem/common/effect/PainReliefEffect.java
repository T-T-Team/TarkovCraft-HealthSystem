package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.AttributeInstance;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.attribute.EntityAttributeData;
import tnt.tarkovcraft.core.common.attribute.modifier.AddValueModifier;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.UUID;

public class PainReliefEffect implements StatusEffect {

    public static final MapCodec<PainReliefEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(StatusEffect::getDuration)
    ).apply(instance, PainReliefEffect::new));
    public static final UUID PAIN_RELIEF_MODIFIER_ID = UUID.fromString("03d3708f-37bf-42a9-8599-b8af97cc7b4f");

    private int duration;

    public PainReliefEffect(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (AttributeSystem.isEnabledForEntity(entity)) {
            EntityAttributeData data = AttributeSystem.getAttributes(entity);
            AttributeInstance instance = data.getAttribute(MedSystemAttributes.PAIN_RELIEF);
            if (!instance.hasModifier(PAIN_RELIEF_MODIFIER_ID)) {
                instance.addModifier(new AddValueModifier(PAIN_RELIEF_MODIFIER_ID, 1));
            }
        }
    }

    @Override
    public void onRemoved(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        if (AttributeSystem.isEnabledForEntity(entity)) {
            EntityAttributeData data = AttributeSystem.getAttributes(entity);
            AttributeInstance instance = data.getAttribute(MedSystemAttributes.PAIN_RELIEF);
            instance.removeModifier(PAIN_RELIEF_MODIFIER_ID);
        }
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
