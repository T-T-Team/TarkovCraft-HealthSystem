package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import tnt.tarkovcraft.core.util.helper.Helper;

import java.util.function.IntFunction;

public enum BloodContainerMode implements StringRepresentable {

    // blood container -> entity
    TRANSFUSION("transfusion"),
    // entity -> blood container
    EXTRACTION("extraction"),

    DRAIN("drain");

    public static final Codec<BloodContainerMode> CODEC = StringRepresentable.fromEnum(BloodContainerMode::values);
    public static final IntFunction<BloodContainerMode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, BloodContainerMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    private final String name;
    private final Component label;
    private final String actionLabelKey;

    BloodContainerMode(String name) {
        this.name = name;
        this.label = Component.translatable("label.medsystem.blood_container.mode." + this.name);
        this.actionLabelKey = "label.medsystem.blood_container.mode." + this.name + ".action";
    }

    public Component getLabel() {
        return label;
    }

    public Component getActionLabel(Object... arguments) {
        return Component.translatable(this.actionLabelKey, arguments);
    }

    public boolean isDraining() {
        return this == DRAIN;
    }

    public BloodContainerMode next(BloodContainer container) {
        BloodContainerMode next = Helper.nextEnum(this);
        if (!container.refillable() && next == EXTRACTION) {
            return Helper.nextEnum(next);
        }
        return next;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
