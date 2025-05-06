package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class FractureStatusEffect implements StatusEffect {

    public static final FractureStatusEffect INSTANCE = new FractureStatusEffect();
    public static final MapCodec<FractureStatusEffect> CODEC = MapCodec.unit(INSTANCE);

    private FractureStatusEffect() {
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public void onRemoved(Context context) {
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public void setDuration(int duration) {
    }

    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public boolean isInfinite() {
        return true;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRACTURE.value();
    }
}
