package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.interaction.DismountEntityInteraction;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionType;
import tnt.tarkovcraft.medsystem.common.interaction.RescueDownedEntityInteraction;

public final class MedSystemEntityInteractions {

    public static final DeferredRegister<EntityInteractionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.ENTITY_INTERACTION, MedSystemConstants.MOD_ID);

    public static final Holder<EntityInteractionType<?>> RESCUE_DOWNED_ENTITY = REGISTRY.register("rescue_downed_entity", id -> EntityInteractionType.Builder.createSingleton(id, RescueDownedEntityInteraction.INSTANCE)
            .serialize(RescueDownedEntityInteraction.CODEC, RescueDownedEntityInteraction.STREAM_CODEC)
            .build()
    );

    public static final Holder<EntityInteractionType<?>> DISMOUNT_ENTITY = REGISTRY.register("dismount_entity", id -> EntityInteractionType.Builder.createSingleton(id, DismountEntityInteraction.INSTANCE)
            .serialize(DismountEntityInteraction.CODEC, DismountEntityInteraction.STREAM_CODEC)
            .build()
    );
}
