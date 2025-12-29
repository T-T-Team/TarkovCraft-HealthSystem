package tnt.tarkovcraft.medsystem.common.health;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntFunction;

public enum LimbType implements StringRepresentable {

    HEAD("head", 0, 0xFF0000, EquipmentSlot.HEAD),
    TORSO("torso", 0, 0xFFFF00, EquipmentSlot.CHEST),
    STOMACH("stomach", MedSystemConstants.HEAL_SURGERY_HEALTH, 0xFF00, EquipmentSlot.CHEST),
    ARM("arm", MedSystemConstants.HEAL_SURGERY_OTHER, 0xFFFF),
    LEG("leg", MedSystemConstants.HEAL_SURGERY_MOVEMENT, 0xFF, EquipmentSlot.LEGS, EquipmentSlot.FEET),
    ANIMAL("animal", 0, 0x00FF00, EquipmentSlot.BODY),
    OTHER("other", 0, 0x444444);

    public static final EnumCodec<LimbType> CODEC = StringRepresentable.fromEnum(LimbType::values);
    public static final IntFunction<LimbType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, LimbType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String serializedName;
    private final int surgeryPriority;
    private final int hitboxColor;
    private final Set<EquipmentSlot> armorSlots;

    LimbType(String serializedName, int surgeryPriority, int hitboxColor) {
        this(serializedName, surgeryPriority, hitboxColor, null);
    }

    LimbType(String serializedName, int surgeryPriority, int hitboxColor, EquipmentSlot first, EquipmentSlot... other) {
        this.serializedName = serializedName;
        this.surgeryPriority = surgeryPriority;
        this.hitboxColor = hitboxColor;
        this.armorSlots = first != null ? EnumSet.of(first, other) : Collections.emptySet();
    }

    public static EnumSet<LimbType> getProtectedByEquipment(EquipmentSlot slot) {
        EnumSet<LimbType> set = EnumSet.noneOf(LimbType.class);
        for (LimbType group : LimbType.values()) {
            if (group.armorSlots.contains(slot)) {
                set.add(group);
            }
        }
        return set;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public Set<EquipmentSlot> getArmorSlots() {
        return armorSlots;
    }

    public int getHitboxColor() {
        return hitboxColor;
    }

    public int getSurgeryHealingPriority() {
        return surgeryPriority;
    }
}
