package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.UnconsciousPoseHelper;
import tnt.tarkovcraft.medsystem.client.util.UnconsciousModelHelper;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @SuppressWarnings("unchecked")
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"),
            cancellable = true
    )
    private void medsystem$setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!UnconsciousPoseHelper.shouldApplyUnconsciousAttributes(entity))
            return;
        // TODO verify all models copy parents properly
        PlayerModel<T> model = (PlayerModel<T>) (Object) this;
        UnconsciousModelHelper.applyPlayerUnconsciousTransforms(model);
        ci.cancel();
    }
}
