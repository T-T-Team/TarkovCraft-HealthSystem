package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.Collection;

public class ModerateBloodLossStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<ModerateBloodLossStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.BOOL.optionalFieldOf("critical", false).forGetter(t -> t.critical)
    ).apply(instance, ModerateBloodLossStatusEffect::new));

    private static final Component[] TITLES = {
            Component.translatable("status_effect.medsystem.moderate_bloodloss"),
            Component.translatable("status_effect.medsystem.critical_bloodloss")
    };
    private static final ResourceLocation[] ICONS = {
            MedicalSystem.resource("textures/icons/status_effect/moderate_bloodloss.png"),
            MedicalSystem.resource("textures/icons/status_effect/critical_bloodloss.png"),
    };

    private boolean critical;

    public ModerateBloodLossStatusEffect() {
        super(-1);
    }

    private ModerateBloodLossStatusEffect(int duration, boolean critical) {
        super(duration);
        this.critical = critical;
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(LivingEntity entity, Context context) {
        if (!BloodSystem.hasBloodDataIntegration(entity)) {
            this.markForRemoval();
            return;
        }
        BloodData data = BloodSystem.getBloodData(entity);
        float percentage = data.getBloodVolumePercentage();
        if (!BloodStatus.MODERATE_BLOOD_LOSS.isInRange(percentage)) {
            this.markForRemoval();
            return;
        }
        this.critical = BloodStatus.RANDOM_BLACKOUT.isInRange(percentage);
    }

    @Override
    public Collection<PostEffect> onRemoved(Context context) {
        return null;
    }

    @Override
    public StatusEffect copy() {
        return new ModerateBloodLossStatusEffect(this.getDuration(), this.critical);
    }

    @Override
    public Component getCustomDisplayName() {
        return TITLES[this.critical ? 1 : 0];
    }

    @Override
    public ResourceLocation getCustomIcon() {
        return ICONS[this.critical ? 1 : 0];
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.MODERATE_BLOODLOSS.value();
    }
}
