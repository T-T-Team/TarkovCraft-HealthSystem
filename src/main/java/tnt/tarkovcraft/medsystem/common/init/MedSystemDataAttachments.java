package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.status.BloodData;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public final class MedSystemDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MedicalSystem.MOD_ID);

    public static final Supplier<AttachmentType<HealthContainer>> HEALTH_CONTAINER = REGISTRY.register("health_container", () -> AttachmentType.builder(HealthContainer::new)
            .serialize(HealthContainer.CODEC)
            .sync(new HealthContainer.SyncHandler())
            .build()
    );
    public static final Supplier<AttachmentType<SideEffectHolder>> SIDE_EFFECTS = REGISTRY.register("side_effects", () -> AttachmentType.builder(() -> new SideEffectHolder(Optional.empty(), Collections.emptyList(), false))
            .serialize(SideEffectHolder.CODEC)
            .build()
    );
    public static final Supplier<AttachmentType<BloodData>> BLOOD_DATA = REGISTRY.register("blood_data", () -> AttachmentType.builder(() -> new BloodData(5.0F))
            .serialize(BloodData.CODEC)
            .sync(BloodData.STREAM_CODEC)
            .build()
    );
}
