package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

public record ToolMultiplierEventFunction(TagKey<Item> tag, NumberProvider multiplier) implements StatusEffectEventFunction {

    public static final MapCodec<ToolMultiplierEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(ToolMultiplierEventFunction::tag),
            NumberProviderType.VALUE_CODEC.fieldOf("multiplier").forGetter(ToolMultiplierEventFunction::multiplier)
    ).apply(instance, ToolMultiplierEventFunction::new));

    @Override
    public float apply(float value, StatusEffectEventContext ctx) {
        DamageContext damageContext = ctx.getParameter(StatusEffectEventParams.DAMAGE_CONTEXT);
        if (damageContext != null) {
            DamageSource damageSource = damageContext.getSource();
            ItemStack itemStack = damageSource.getWeaponItem();
            if (itemStack != null && !itemStack.isEmpty() && itemStack.is(this.tag)) {
                return value * this.multiplier.floatValue();
            }
        }
        return value;
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.TOOL_MULTIPLIER.value();
    }
}
