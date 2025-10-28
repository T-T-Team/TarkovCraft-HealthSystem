package tnt.tarkovcraft.medsystem.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class WeightedList<E> {
    private static final int FLAT_THRESHOLD = 64;
    private final int totalWeight;
    private final List<Weighted<E>> items;
    @Nullable
    private final WeightedList.Selector<E> selector;

    WeightedList(List<? extends Weighted<E>> items) {
        this.items = List.copyOf(items);
        this.totalWeight = items.stream().mapToInt(Weighted::weight).sum();
        if (this.totalWeight == 0) {
            this.selector = null;
        } else if (this.totalWeight < 64) {
            this.selector = new WeightedList.Flat<>(this.items, this.totalWeight);
        } else {
            this.selector = new WeightedList.Compact<>(this.items);
        }
    }

    public static <E> WeightedList<E> of() {
        return new WeightedList<>(List.of());
    }

    public static <E> WeightedList<E> of(E element) {
        return new WeightedList<>(List.of(new Weighted<>(element, 1)));
    }

    @SafeVarargs
    public static <E> WeightedList<E> of(Weighted<E>... items) {
        return new WeightedList<>(List.of(items));
    }

    public static <E> WeightedList<E> of(List<Weighted<E>> items) {
        return new WeightedList<>(items);
    }

    public static <E> WeightedList.Builder<E> builder() {
        return new WeightedList.Builder<>();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public <T> WeightedList<T> map(Function<E, T> mapper) {
        return new WeightedList(Lists.transform(this.items, p_409789_ -> p_409789_.map((Function<E, E>)mapper)));
    }

    public Optional<E> getRandom(RandomSource random) {
        if (this.selector == null) {
            return Optional.empty();
        } else {
            int i = random.nextInt(this.totalWeight);
            return Optional.of(this.selector.get(i));
        }
    }

    public E getRandomOrThrow(RandomSource random) {
        if (this.selector == null) {
            throw new IllegalStateException("Weighted list has no elements");
        } else {
            int i = random.nextInt(this.totalWeight);
            return this.selector.get(i);
        }
    }

    public List<Weighted<E>> unwrap() {
        return this.items;
    }

    public static <E> Codec<WeightedList<E>> codec(Codec<E> elementCodec) {
        return Weighted.codec(elementCodec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> codec(MapCodec<E> elementCodec) {
        return Weighted.codec(elementCodec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(Codec<E> elementCodec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(elementCodec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(MapCodec<E> elementCodec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(elementCodec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E, B extends ByteBuf> StreamCodec<B, WeightedList<E>> streamCodec(StreamCodec<B, E> elementCodec) {
        return Weighted.streamCodec(elementCodec).apply(ByteBufCodecs.list()).map(WeightedList::of, WeightedList::unwrap);
    }

    public boolean contains(E element) {
        for (Weighted<E> weighted : this.items) {
            if (weighted.value().equals(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        } else {
            return !(other instanceof WeightedList<?> weightedlist)
                    ? false
                    : this.totalWeight == weightedlist.totalWeight && Objects.equals(this.items, weightedlist.items);
        }
    }

    @Override
    public int hashCode() {
        int i = this.totalWeight;
        return 31 * i + this.items.hashCode();
    }

    public static class Builder<E> {
        private final List<Weighted<E>> result = new java.util.ArrayList<>();

        public WeightedList.Builder<E> add(E element) {
            return this.add(element, 1);
        }

        public WeightedList.Builder<E> add(E element, int weight) {
            this.result.add(new Weighted<>(element, weight));
            return this;
        }

        public WeightedList.Builder<E> add(Weighted<E> value) {
            this.result.add(value);
            return this;
        }

        public WeightedList.Builder<E> addAll(WeightedList<E> values) {
            return this.addAll(values.unwrap());
        }

        public WeightedList.Builder<E> addAll(java.util.Collection<Weighted<E>> values) {
            this.result.addAll(values);
            return this;
        }

        public WeightedList.Builder<E> remove(Weighted<E> value) {
            this.result.remove(value);
            return this;
        }

        public WeightedList.Builder<E> remove(E value) {
            this.result.removeIf(weighted -> weighted.value().equals(value));
            return this;
        }

        public WeightedList.Builder<E> removeIf(java.util.function.Predicate<Weighted<E>> filter) {
            this.result.removeIf(filter);
            return this;
        }

        public List<Weighted<E>> getList() {
            return java.util.Collections.unmodifiableList(this.result);
        }

        public WeightedList<E> build() {
            return new WeightedList<>(this.result);
        }
    }

    static class Compact<E> implements WeightedList.Selector<E> {
        private final Weighted<?>[] entries;

        Compact(List<Weighted<E>> entries) {
            this.entries = entries.toArray(Weighted[]::new);
        }

        @Override
        public E get(int index) {
            for (Weighted<?> weighted : this.entries) {
                index -= weighted.weight();
                if (index < 0) {
                    return (E)weighted.value();
                }
            }

            throw new IllegalStateException(index + " exceeded total weight");
        }
    }

    static class Flat<E> implements WeightedList.Selector<E> {
        private final Object[] entries;

        Flat(List<Weighted<E>> entries, int size) {
            this.entries = new Object[size];
            int i = 0;

            for (Weighted<E> weighted : entries) {
                int j = weighted.weight();
                Arrays.fill(this.entries, i, i + j, weighted.value());
                i += j;
            }
        }

        @Override
        public E get(int index) {
            return (E)this.entries[index];
        }
    }

    interface Selector<E> {
        E get(int index);
    }
}
