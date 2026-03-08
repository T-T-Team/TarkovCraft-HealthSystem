package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public final class MedSystemDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MedSystemConstants.MOD_ID);

    public static final Supplier<AttachmentType<HealthContainer>> HEALTH_CONTAINER = REGISTRY.register("health_container", () -> AttachmentType.builder(HealthContainer::new)
            .serialize(HealthContainer.CODEC)
            .sync(new HealthContainer.SyncHandler())
            .build()
    );
    public static final Supplier<AttachmentType<SideEffectHolder>> SIDE_EFFECTS = REGISTRY.register("side_effects", () -> AttachmentType.builder(() -> new SideEffectHolder(Optional.empty(), Collections.emptyList(), false))
            .serialize(SideEffectHolder.CODEC)
            .build()
    );
    public static final Supplier<AttachmentType<DamageContext>> ACTIVE_DAMAGE_CONTEXT = REGISTRY.register("active_damage_context", () -> AttachmentType.builder(() -> new DamageContext(null, null))
            .build()
    );
    public static final Supplier<AttachmentType<EntityBloodSystem>> BLOOD_SYSTEM = REGISTRY.register("blood_system", () -> AttachmentType.builder(EntityBloodSystem::invalid)
            .serialize(EntityBloodSystem.CODEC)
            .sync(EntityBloodSystem.STREAM_CODEC)
            .build()
    );
}
