package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventFunctions;

public record ToolMultiplierEventFunction(TagKey<Item> tag, NumberProvider multiplier) implements HealthEventFunction {

    public static final MapCodec<ToolMultiplierEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(ToolMultiplierEventFunction::tag),
            NumberProviderType.VALUE_CODEC.fieldOf("multiplier").forGetter(ToolMultiplierEventFunction::multiplier)
    ).apply(instance, ToolMultiplierEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext ctx) {
        DamageContext damageContext = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
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
    public HealthEventFunctionType<?> getType() {
        return MedSystemHealthEventFunctions.TOOL_MULTIPLIER.value();
    }
}
