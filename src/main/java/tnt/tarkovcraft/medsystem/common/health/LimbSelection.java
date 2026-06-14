package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.helper.TextHelper;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public record LimbSelection(Mode mode, Set<LimbType> limbs) implements Predicate<Limb> {

    public static final Codec<LimbSelection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.INCLUSIVE).forGetter(LimbSelection::mode),
            Codecs.enumSet(LimbType.CODEC).fieldOf("limbs").forGetter(LimbSelection::limbs)
    ).apply(instance, LimbSelection::new));
    public static final StreamCodec<ByteBuf, LimbSelection> STREAM_CODEC = StreamCodec.composite(
            Mode.STREAM_CODEC, LimbSelection::mode,
            LimbType.STREAM_CODEC.apply(ByteBufCodecs.list()), selection -> new ArrayList<>(selection.limbs),
            (selectionMode, limbs) -> new LimbSelection(selectionMode, EnumSet.copyOf(limbs))
    );

    public static final LimbSelection ALL = new LimbSelection(Mode.EXCLUSIVE, Collections.emptySet());
    public static final LimbSelection ARM_LEG = new LimbSelection(Mode.INCLUSIVE, LimbType.ARM, LimbType.LEG);

    public LimbSelection(Mode mode, LimbType first, LimbType... other) {
        this(mode, EnumSet.of(first, other));
    }

    public static LimbSelection inclusive(LimbType first, LimbType... other) {
        return new LimbSelection(Mode.INCLUSIVE, EnumSet.of(first, other));
    }

    public static LimbSelection exclusive(LimbType first, LimbType... other) {
        return new LimbSelection(Mode.EXCLUSIVE, EnumSet.of(first, other));
    }

    public boolean allowsAll() {
        return switch (this.mode) {
            case INCLUSIVE -> this.limbs.size() == LimbType.values().length;
            case EXCLUSIVE -> this.limbs.isEmpty();
        };
    }

    public List<LimbType> getSelectedLimbs() {
        return switch (this.mode) {
            case INCLUSIVE -> new ArrayList<>(this.limbs);
            case EXCLUSIVE -> {
                Set<LimbType> set = EnumSet.allOf(LimbType.class);
                set.removeAll(this.limbs);
                yield new ArrayList<>(set);
            }
        };
    }

    public void appendApplicableOnLabel(MutableComponent label) {
        if (this.allowsAll())
            return;
        List<LimbType> selection = this.getSelectedLimbs();
        Component selectionLabel = TextHelper.join(selection, LimbType::getLabel, ",");
        Component filterLabel = Component.translatable("label.medsystem.applicable_on", selectionLabel);
        label.append(" (").append(filterLabel).append(")");
    }

    @Override
    public boolean test(Limb limb) {
        return this.test(limb.getType());
    }

    public boolean test(LimbType type) {
        return switch (this.mode) {
            case INCLUSIVE -> this.limbs.contains(type);
            case EXCLUSIVE -> !this.limbs.contains(type);
        };
    }

    public enum Mode implements StringRepresentable {

        INCLUSIVE("inclusive"),
        EXCLUSIVE("exclusive");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final IntFunction<Mode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
