package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.function.Supplier;

public final class MedSystemDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MedicalSystem.MOD_ID);

    public static final Supplier<AttachmentType<HealthContainer>> HEALTH_CONTAINER = REGISTRY.register("health_container", () -> AttachmentType.builder(() -> new HealthContainer())
            .serialize(HealthContainer.CODEC)
            .build()
    );
}
