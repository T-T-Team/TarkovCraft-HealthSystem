package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.pose.EntityPose;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousDraggedEntityPose;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousEntityPose;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousSittingEntityPose;

public final class MedSystemEntityPoses {

    public static final DeferredRegister<EntityPose.Type<?>> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.ENTITY_POSE, MedSystemConstants.MOD_ID);

    public static final Holder<EntityPose.Type<?>> UNCONSCIOUS = register("unconscious", UnconsciousEntityPose.CODEC);
    public static final Holder<EntityPose.Type<?>> UNCONSCIOUS_SITTING = register("unconscious_sitting", UnconsciousSittingEntityPose.CODEC);
    public static final Holder<EntityPose.Type<?>> UNCONSCIOUS_DRAGGED = register("unconscious_dragged", UnconsciousDraggedEntityPose.CODEC);

    private static Holder<EntityPose.Type<?>> register(String name, MapCodec<? extends EntityPose> codec) {
        return REGISTRY.register(name, key -> new EntityPose.Type<>(key, codec));
    }
}
