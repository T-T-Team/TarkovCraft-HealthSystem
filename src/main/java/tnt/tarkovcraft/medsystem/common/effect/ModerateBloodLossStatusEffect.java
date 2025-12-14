package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ModerateBloodLossStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<ModerateBloodLossStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.BOOL.optionalFieldOf("critical", false).forGetter(t -> t.critical)
    ).apply(instance, ModerateBloodLossStatusEffect::new));

    private static final Component[] TITLES = {
            Component.translatable("status_effect.medsystem.moderate_bloodloss"),
            Component.translatable("status_effect.medsystem.critical_bloodloss")
    };
    private static final Identifier[] ICONS = {
            MedicalSystem.createIdentifier("textures/icons/status_effect/moderate_bloodloss.png"),
            MedicalSystem.createIdentifier("textures/icons/status_effect/critical_bloodloss.png"),
    };
    private static final Component DESCRIPTION_CRITICAL_STATE = Component.translatable("status_effect.medsystem.critical_bloodloss.info").withStyle(ChatFormatting.DARK_GRAY);

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
    public void applyEffect(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
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
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
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
    public Identifier getCustomIcon() {
        return ICONS[this.critical ? 1 : 0];
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        if (this.critical) {
            tooltip.accept(DESCRIPTION_CRITICAL_STATE);
        }
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.MODERATE_BLOODLOSS.value();
    }
}
