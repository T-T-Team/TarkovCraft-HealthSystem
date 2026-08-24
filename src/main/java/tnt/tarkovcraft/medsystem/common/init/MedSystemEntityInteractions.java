package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.api.EntityInteraction;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.interaction.DismountEntityInteraction;
import tnt.tarkovcraft.medsystem.common.interaction.RescueDownedEntityInteraction;

public final class MedSystemEntityInteractions {

    public static final DeferredRegister<EntityInteraction.Type<?>> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.ENTITY_INTERACTION, MedSystemConstants.MOD_ID);

    public static final Holder<EntityInteraction.Type<?>> RESCUE_DOWNED_ENTITY = REGISTRY.register("rescue_downed_entity", id -> EntityInteraction.Type.singletonBuilder(id, RescueDownedEntityInteraction.INSTANCE)
            .withSerializer(RescueDownedEntityInteraction.CODEC, RescueDownedEntityInteraction.STREAM_CODEC)
            .withPredicate(RescueDownedEntityInteraction::test)
            .withDuration(200)
            .build()
    );

    public static final Holder<EntityInteraction.Type<?>> DISMOUNT_ENTITY = REGISTRY.register("dismount_entity", id -> EntityInteraction.Type.singletonBuilder(id, DismountEntityInteraction.INSTANCE)
            .withSerializer(DismountEntityInteraction.CODEC, DismountEntityInteraction.STREAM_CODEC)
            .withPredicate(DismountEntityInteraction::test)
            .withDuration(120)
            .build()
    );
}
