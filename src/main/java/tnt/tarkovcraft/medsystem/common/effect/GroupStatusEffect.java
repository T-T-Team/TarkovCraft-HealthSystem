package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public abstract class GroupStatusEffect extends StatusEffect {

    private final List<EffectGroupHolder> items = new ArrayList<>();

    public GroupStatusEffect(List<EffectGroupHolder> items) {
        super(-1, 0);
        this.items.addAll(items);
        int duration = 0;
        for (EffectGroupHolder item : this.items) {
            duration = Math.max(duration, item.getDuration());
        }
        this.setDuration(duration);
    }

    protected GroupStatusEffect(int duration, int delay, List<EffectGroupHolder> items) {
        super(duration, delay);
        this.items.addAll(items);
    }

    public abstract GroupStatusEffect newInstance(List<EffectGroupHolder> copiedItems);

    @Override
    public final StatusEffect copy() {
        List<EffectGroupHolder> copy = this.items.stream()
                .map(EffectGroupHolder::copy)
                .toList();
        return this.newInstance(copy);
    }

    @Override
    public final void apply(Context context) {
        if (this.items.isEmpty()) {
            this.markForRemoval();
            return;
        }
        Iterator<EffectGroupHolder> iterator = items.iterator();
        while (iterator.hasNext()) {
            EffectGroupHolder item = iterator.next();
            item.tick(context);
            if (item.isExpired())
                iterator.remove();
        }
    }

    @Override
    public final StatusEffect onRemoved(Context context) {
        this.items.forEach(item -> item.cleanUp(context));
        return null;
    }

    @Override
    public final boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public final boolean hasCustomTooltip() {
        return true;
    }

    @Override
    public final void addAdditionalInfo(Consumer<Component> tooltip) {
        this.items.forEach(item -> {
            if (item.isActive() && !item.isExpired())
                item.addInformation(tooltip, false);
        });
    }

    @Override
    public final void addCustomTooltip(Consumer<Component> tooltip) {
        this.items.forEach(item -> item.addInformation(tooltip, true));
    }

    protected final List<EffectGroupHolder> getItems() {
        return items;
    }

    @Override
    public boolean isVisible() {
        return this.items.stream().anyMatch(EffectGroupHolder::isActive);
    }

    @SuppressWarnings("unchecked")
    public static <T extends GroupStatusEffect> T merge(T first, T second) {
        List<EffectGroupHolder> firstItems = first.getItems();
        List<EffectGroupHolder> secondItems = second.getItems();
        List<EffectGroupHolder> result = new ArrayList<>();
        Iterator<EffectGroupHolder> firstIt = firstItems.iterator();
        while (firstIt.hasNext()) {
            EffectGroupHolder item = firstIt.next();
            Iterator<EffectGroupHolder> secondIt = secondItems.iterator();
            while (secondIt.hasNext()) {
                EffectGroupHolder secondItem = secondIt.next();
                EffectGroupHolder mergeResult = item.tryMerge(secondItem);
                if (mergeResult != null) {
                    result.add(mergeResult);
                    secondIt.remove();
                    firstIt.remove();
                    break;
                }
            }
        }
        result.addAll(firstItems);
        result.addAll(secondItems);
        return (T) first.newInstance(result);
    }

    public static <T extends GroupStatusEffect> Products.P3<RecordCodecBuilder.Mu<T>, Integer, Integer, List<EffectGroupHolder>> commonGroup(RecordCodecBuilder.Instance<T> instance) {
        return common(instance).and(
                EffectGroupHolder.CODEC.listOf().optionalFieldOf("items", Collections.emptyList()).forGetter(GroupStatusEffect::getItems)
        );
    }
}
