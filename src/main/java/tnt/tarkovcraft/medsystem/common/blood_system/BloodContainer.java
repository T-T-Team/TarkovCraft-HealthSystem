package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

import java.util.Optional;

public record BloodContainer(float capacity, float value, boolean refillable, Optional<Identifier> bloodType) {

    public static final Codec<BloodContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("capacity").forGetter(BloodContainer::capacity),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("value", 0.0F).forGetter(BloodContainer::value),
            Codec.BOOL.optionalFieldOf("refillable", true).forGetter(BloodContainer::refillable),
            Identifier.CODEC.optionalFieldOf("blood_type").forGetter(BloodContainer::bloodType)
    ).apply(instance, BloodContainer::new));
    public static final StreamCodec<ByteBuf, BloodContainer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BloodContainer::capacity,
            ByteBufCodecs.FLOAT, BloodContainer::value,
            ByteBufCodecs.BOOL, BloodContainer::refillable,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), BloodContainer::bloodType,
            BloodContainer::new
    );
    public static final Component UNKNOWN_BLOOD_TYPE = Component.translatable("blood_type.medsystem.unknown").withStyle(ChatFormatting.RED);

    public static BloodContainer emptyContainer(float capacity, boolean refillable) {
        return new BloodContainer(capacity, 0.0F, refillable, Optional.empty());
    }

    public boolean isEmpty() {
        return this.value <= 0;
    }

    public boolean isFull() {
        return this.value >= this.capacity;
    }

    public float getMissingCapacity() {
        return this.capacity - this.value;
    }

    public boolean isCompatible(Identifier type) {
        return this.bloodType.isEmpty() || this.bloodType.get().equals(type);
    }

    public Component getBloodTypeLabel() {
        return this.bloodType.flatMap(id -> MedicalSystem.BLOOD_SYSTEM.getConfig().getStylizedBloodLabel(id))
                .orElse(UNKNOWN_BLOOD_TYPE);
    }

    public BloodContainer fill(float amount, Identifier bloodType) {
        float newAmount = Mth.clamp(this.value + amount, 0.0F, this.capacity);
        return new BloodContainer(this.capacity, newAmount, this.refillable, Optional.of(bloodType));
    }

    public BloodContainer extract(float amount) {
        float newAmount = Mth.clamp(this.value - amount, 0.0F, this.capacity);
        return new BloodContainer(this.capacity, newAmount, this.refillable, newAmount <= 0 ? Optional.empty() : this.bloodType);
    }

    public UserActionResult<Boolean> canUseMode(BloodContainerMode mode, EntityBloodSystem bloodSystem) {
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        return switch (mode) {
            case TRANSFUSION -> {
                if (this.isEmpty()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.empty_container").withStyle(ChatFormatting.RED));
                }
                if (bloodSystem.getBloodVolume() >= definition.getMaxBloodVolume()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.max_blood_level").withStyle(ChatFormatting.RED));
                }
                yield UserActionResult.success(true);
            }
            case EXTRACTION -> {
                Identifier bloodType = bloodSystem.getBloodType();
                if (!this.refillable()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.not_refillable").withStyle(ChatFormatting.RED));
                }
                if (!this.isCompatible(bloodType)) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.incompatible_blood_type").withStyle(ChatFormatting.RED));
                }
                if (this.isFull()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.full_container").withStyle(ChatFormatting.RED));
                }
                if (bloodSystem.hasBledOut()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.no_blood").withStyle(ChatFormatting.RED));
                }
                yield UserActionResult.success(true);
            }
            case DRAIN -> {
                if (this.isEmpty()) {
                    yield UserActionResult.failure(Component.translatable("label.medsystem.blood_container.empty_container").withStyle(ChatFormatting.RED));
                }
                yield UserActionResult.success(true);
            }
        };
    }
}
