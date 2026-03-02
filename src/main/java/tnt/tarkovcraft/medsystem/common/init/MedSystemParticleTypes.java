package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.particles.ParticleType;
import tnt.tarkovcraft.core.util.register.ParticleTypeDeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.particle.BloodDecalParticleOptions;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;

import java.util.function.Supplier;

public final class MedSystemParticleTypes {

    public static final ParticleTypeDeferredRegister REGISTRY = ParticleTypeDeferredRegister.create(MedSystemConstants.MOD_ID);

    public static final Supplier<ParticleType<BloodDripParticleOptions>> BLOOD_DRIP = REGISTRY.registerParticleType("blood_drip", BloodDripParticleOptions::codec, BloodDripParticleOptions::streamCodec, true);
    public static final Supplier<ParticleType<BloodDecalParticleOptions>> BLOOD_DECAL = REGISTRY.registerParticleType("blood_decal", BloodDecalParticleOptions::codec, BloodDecalParticleOptions::streamCodec, true);


}
