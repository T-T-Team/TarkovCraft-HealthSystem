package tnt.tarkovcraft.medsystem.client;

import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class RenderStateExtensions {

    public static final ContextKey<Boolean> SPECIAL_POSE = create("special_pose");
    public static final ContextKey<Boolean> PASSENGER = create("passenger");
    public static final ContextKey<Boolean> UNCONSCIOUS = create("unconscious");
    public static final ContextKey<Float> COLLAPSE_ANIMATION_AMOUNT = create("collapse_anim_amount");

    private static <T> ContextKey<T> create(String code) {
        return new ContextKey<>(MedicalSystem.createIdentifier(code));
    }

    public static boolean hasSpecialPoseRenderer(BaseRenderState renderState) {
        return renderState.getRenderDataOrDefault(SPECIAL_POSE, false);
    }

    public static boolean shouldApplyUnconsciousAttributes(BaseRenderState renderState) {
        boolean unconscious = renderState.getRenderDataOrDefault(UNCONSCIOUS, false);
        boolean passenger = renderState.getRenderDataOrDefault(PASSENGER, false);
        return unconscious && !passenger;
    }
}
