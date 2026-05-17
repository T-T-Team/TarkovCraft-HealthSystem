package tnt.tarkovcraft.medsystem.common.consume_effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemConsumeEffects;

import java.util.Locale;
import java.util.function.UnaryOperator;

public record RemoveShockConsumeEffect(NumberProvider amount) implements ConsumeEffect {

    public static final MapCodec<RemoveShockConsumeEffect> CODEC = NumberProviderType.VALUE_CODEC
            .xmap(RemoveShockConsumeEffect::new, RemoveShockConsumeEffect::amount).fieldOf("amount");
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveShockConsumeEffect> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    public RemoveShockConsumeEffect(float amount) {
        this(ConstantNumberProvider.of(amount));
    }

    public static Component createTooltipLabel(float amount) {
        return createTooltipLabel(String.format(Locale.ROOT, "%.1f%%", amount * 100), style -> style.withColor(ChatFormatting.GREEN));
    }

    public static Component createTooltipLabel(String amount, UnaryOperator<Style> styleApplicator) {
        Component value = Component.literal(amount).withStyle(styleApplicator);
        return Component.translatable("tooltip.medsystem.heal_attributes.shock_reduction", value).withStyle(ChatFormatting.GRAY);
    }

    public Component createTooltipLabel() {
        return createTooltipLabel(this.amount.floatValue());
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
        if (BloodSystemManager.isEnabled(entity)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            bloodSystem.removeShock(this.amount.floatValue());
            return true;
        }
        return false;
    }

    @Override
    public ConsumeEffectType<?> getType() {
        return MedSystemConsumeEffects.REMOVE_SHOCK.value();
    }
}
