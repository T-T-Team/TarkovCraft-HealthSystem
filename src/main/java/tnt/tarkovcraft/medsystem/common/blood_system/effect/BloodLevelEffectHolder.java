package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.range.NumberRange;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

import java.util.List;

public record BloodLevelEffectHolder(NumberRange range, List<BloodLevelEffect> effects) {

    public static final Codec<BloodLevelEffectHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NumberRange.CODEC.fieldOf("volume_range").forGetter(BloodLevelEffectHolder::range),
            Codecs.list(BloodLevelEffect.CODEC).fieldOf("effects").forGetter(BloodLevelEffectHolder::effects)
    ).apply(instance, BloodLevelEffectHolder::new));

    public boolean canApply(float volume) {
        return this.range.isWithinRange(volume);
    }

    public void apply(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        for (BloodLevelEffect effect : this.effects) {
            effect.applyEffects(entity, level, bloodSystem);
        }
    }
}
