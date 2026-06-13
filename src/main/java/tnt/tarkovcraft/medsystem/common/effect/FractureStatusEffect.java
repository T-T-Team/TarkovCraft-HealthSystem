package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.config.StatusEffectConfig;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class FractureStatusEffect extends EntityCausedStatusEffect {

    public static final MapCodec<FractureStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, FractureStatusEffect::new));
    private static final Component HINT = Component.translatable("status_effect.medsystem.fracture.heal_hint").withStyle(ChatFormatting.DARK_GRAY);
    private static final ResourceLocation RECOVERING_ICON = StatusEffectHelper.getTextureResource(MedSystemConstants.MOD_ID, "fracture_recovering");

    public FractureStatusEffect(int duration, Optional<UUID> owner) {
        super(duration, owner);
    }

    public FractureStatusEffect(int duration) {
        super(duration);
    }

    public static FractureStatusEffect createTemplate() {
        return new FractureStatusEffect(-1);
    }

    @Override
    public StatusEffect copy() {
        return new FractureStatusEffect(this.getDuration(), Optional.ofNullable(this.getCausingEntity()));
    }

    @Override
    public void apply(StatusEffectContext context) {
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        if (this.isInfinite()) {
            tooltip.accept(HINT);
        } else {
            tooltip.accept(RECOVERING_LABEL);
        }
    }

    @Override
    @Nullable
    public ResourceLocation getCustomIcon() {
        if (!this.isInfinite()) {
            return RECOVERING_ICON;
        }
        return null;
    }

    @Override
    @Nullable
    public EffectType getEffectType() {
        return this.isInfinite() ? super.getEffectType() : EffectType.NEUTRAL;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRACTURE.value();
    }

    public static FractureStatusEffect mergeWithDurationLimits(FractureStatusEffect f1, FractureStatusEffect f2) {
        FractureStatusEffect effect = maxDuration(f1, f2);
        StatusEffectConfig config = MedicalSystem.getConfig().statusEffects;
        effect.setDuration(StatusEffectConfig.getStackedDuration(effect.getDuration(), config.maxFractureDuration));
        return effect;
    }
}
