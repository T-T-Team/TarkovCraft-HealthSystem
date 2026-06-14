package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BloodLossStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<BloodLossStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Stage.CODEC.fieldOf("stage").forGetter(t -> t.stage)
    ).apply(instance, BloodLossStatusEffect::new));

    private final Stage stage;

    private BloodLossStatusEffect(int duration, Stage stage) {
        super(duration);
        this.stage = stage;
    }

    public BloodLossStatusEffect(int duration) {
        this(duration, Stage.MILD);
    }

    public static BloodLossStatusEffect createTemplate(Stage stage) {
        return new BloodLossStatusEffect(-1, stage);
    }

    @Override
    public void applyEffect(StatusEffectContext context) {
        LivingEntity entity = context.entity();
        if (!BloodSystemManager.isEnabled(entity)) {
            this.markForRemoval();
            return;
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        float f = bloodSystem.getBloodVolume() / definition.getMaxBloodVolume();
        Stage actualStage = definition.getBloodLossStage(f);
        if (actualStage == null) {
            this.markForRemoval();
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    @Nullable
    public Component getCustomDisplayName() {
        return this.stage.getTitle();
    }

    @Override
    @Nullable
    public ResourceLocation getCustomIcon() {
        return this.stage.getIcon();
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        Component description = this.stage.getTooltip();
        if (description != null) {
            tooltip.accept(description);
        }
    }

    @Override
    public StatusEffect copy() {
        return new BloodLossStatusEffect(this.getDuration(), this.stage);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.BLOODLOSS.value();
    }

    public Stage getStage() {
        return stage;
    }

    public enum Stage implements StringRepresentable {

        MILD("mild", null),

        MODERATE("moderate", null),

        SEVERE("severe", Component.translatable("status_effect.medsystem.bloodloss.stage.severe.info").withStyle(ChatFormatting.DARK_GRAY));

        public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);
        private final String serializedName;
        private final Component title;
        private final ResourceLocation icon;
        private final @Nullable Component tooltip;

        Stage(String serializedName, @Nullable Component tooltip) {
            this.serializedName = serializedName;
            this.title = Component.translatable("status_effect.medsystem.bloodloss.stage." + serializedName);
            this.icon = MedicalSystem.createIdentifier("textures/icons/status_effect/bloodloss_" + serializedName + ".png");
            this.tooltip = tooltip;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public Component getTitle() {
            return title;
        }

        public ResourceLocation getIcon() {
            return icon;
        }

        public @Nullable Component getTooltip() {
            return tooltip;
        }
    }
}
