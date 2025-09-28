package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import java.util.function.Consumer;

public class MobEffectGroupItem implements EffectGroupItem {

    public static final MapCodec<MobEffectGroupItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.effect),
            ExtraCodecs.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(t -> t.amplifier),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(t -> t.ambient),
            Codec.BOOL.optionalFieldOf("visible", true).forGetter(t -> t.visible),
            Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(t -> t.showIcon)
    ).apply(instance, MobEffectGroupItem::new));

    private final Holder<MobEffect> effect;
    private final int amplifier;
    private final boolean ambient;
    private final boolean visible;
    private final boolean showIcon;

    public MobEffectGroupItem(Holder<MobEffect> effect, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
    }

    public MobEffectGroupItem(Holder<MobEffect> effect, int amplifier, boolean ambient, boolean visible) {
        this(effect, amplifier, ambient, visible, true);
    }

    public MobEffectGroupItem(Holder<MobEffect> effect, int amplifier, boolean ambient) {
        this(effect, amplifier, ambient, true);
    }

    public MobEffectGroupItem(Holder<MobEffect> effect, int amplifier) {
        this(effect, amplifier, false);
    }

    public MobEffectGroupItem(Holder<MobEffect> effect) {
        this(effect, 0);
    }

    @Override
    public void init(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        if (entity.level().isClientSide()) {
            MobEffectInstance instance = new MobEffectInstance(this.effect, holder.getDuration(), this.amplifier, this.ambient, this.visible, this.showIcon);
            entity.addEffect(instance);
        }
    }

    @Override
    public void apply(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }

    @Override
    public void cleanup(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }

    @Override
    public void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip) {
        MobEffect mobEffect = this.effect.value();
        MobEffectCategory category = mobEffect.getCategory();
        EffectType effectType = EffectType.byMobEffectCategory(category);
        Component component;
        if (this.amplifier < 10) {
            component = mobEffect.getDisplayName().copy().append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (this.amplifier + 1)));
        } else {
            component = mobEffect.getDisplayName();
        }
        tooltip.accept(SideEffect.createDescriptionComponent(effectType, component, 1.0F, holder.getDuration(), holder.getDelay()));
    }

    @Override
    public EffectGroupItem copy() {
        return new MobEffectGroupItem(this.effect, this.amplifier, this.ambient, this.visible, this.showIcon);
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other) {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.MOB_EFFECT.value();
    }
}
