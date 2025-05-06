package tnt.tarkovcraft.medsystem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageEffects;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.Supplier;

// used by enumextensions.json
// https://docs.neoforged.net/docs/advanced/extensibleenums/#iextensibleenum
@SuppressWarnings("unused")
public final class MedicalSystemEnumExtensions {

    public static final EnumProxy<DamageEffects> MEDSYSTEM_NONE = new EnumProxy<>(
            DamageEffects.class,
            "medsystem:none", (Supplier<SoundEvent>) () -> null
    );
}
