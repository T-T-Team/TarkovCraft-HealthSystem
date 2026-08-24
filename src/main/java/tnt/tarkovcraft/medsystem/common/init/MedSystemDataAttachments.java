package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.function.Supplier;

public final class MedSystemDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MedSystemConstants.MOD_ID);

    public static final Supplier<AttachmentType<HealthContainer>> HEALTH_CONTAINER = REGISTRY.register("health_container", () -> AttachmentType.builder(HealthContainer::invalid)
            .serialize(HealthContainer.CODEC)
            .sync(new HealthContainer.SyncHandler())
            .build()
    );
    public static final Supplier<AttachmentType<SideEffectHolder>> SIDE_EFFECTS = REGISTRY.register("side_effects", () -> AttachmentType.builder(SideEffectHolder::empty)
            .serialize(SideEffectHolder.CODEC)
            .build()
    );
    // reusable damage container for entities
    public static final Supplier<AttachmentType<DamageContext>> DAMAGE_CONTEXT = REGISTRY.register("damage_context", () -> AttachmentType.builder(DamageContext::createEmptyInstance)
            .build()
    );
    public static final Supplier<AttachmentType<EntityBloodSystem>> BLOOD_SYSTEM = REGISTRY.register("blood_system", () -> AttachmentType.builder(EntityBloodSystem::invalid)
            .serialize(EntityBloodSystem.CODEC)
            .sync(new EntityBloodSystem.SyncHandler())
            .build()
    );
    public static final Supplier<AttachmentType<Boolean>> EXTERNALLY_CONTROLLED = REGISTRY.register("externally_controlled", () -> AttachmentType.builder(() -> false)
            .serialize(Codec.BOOL)
            .sync(ByteBufCodecs.BOOL)
            .build()
    );
}
