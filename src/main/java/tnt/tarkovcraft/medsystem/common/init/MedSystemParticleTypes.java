package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import tnt.tarkovcraft.core.common.particle.SimpleDecalParticleOptions;
import tnt.tarkovcraft.core.util.register.ParticleTypeDeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

import java.util.function.Supplier;

public final class MedSystemParticleTypes {

    public static final ParticleTypeDeferredRegister REGISTRY = ParticleTypeDeferredRegister.create(MedSystemConstants.MOD_ID);

    public static final Supplier<SimpleParticleType> BLOOD_DRIP = REGISTRY.registerSimpleParticleType("blood_drip", true);
    public static final Supplier<ParticleType<SimpleDecalParticleOptions>> BLOOD_DECAL = REGISTRY.registerParticleType("blood_decal", SimpleDecalParticleOptions::codec, SimpleDecalParticleOptions::streamCodec, true);


}
