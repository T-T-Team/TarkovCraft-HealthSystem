package tnt.tarkovcraft.medsystem.common.damage_effect;

import net.minecraft.util.StringRepresentable;

public enum DamageEffectContextType implements StringRepresentable {

    ON_HURT("on_hurt"),
    ON_UPDATE("on_update");

    public static final EnumCodec<DamageEffectContextType> CODEC = StringRepresentable.fromEnum(DamageEffectContextType::values);

    private final String serializedName;

    DamageEffectContextType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
