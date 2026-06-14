package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;

public record ToolMultiplierEventFunction(TagKey<Item> tag, float multiplier) implements HealthEventFunction {

    public static final MapCodec<ToolMultiplierEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(ToolMultiplierEventFunction::tag),
            NumberProvider.NON_NEGATIVE_FLOAT.fieldOf("multiplier").forGetter(ToolMultiplierEventFunction::multiplier)
    ).apply(instance, ToolMultiplierEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext ctx) {
        DamageContext damageContext = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
        if (damageContext != null) {
            DamageSource damageSource = damageContext.getSource();
            ItemStack itemStack = damageSource.getWeaponItem();
            if (itemStack != null && !itemStack.isEmpty() && itemStack.is(this.tag)) {
                return value * this.multiplier;
            }
        }
        return value;
    }

    @Override
    public MapCodec<? extends HealthEventFunction> codec() {
        return CODEC;
    }
}
