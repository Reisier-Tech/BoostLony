package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.research.ResearchEffectManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResearchEffectManager.class, remap = false)
public abstract class ResearchEffectManagerMixin {

    @Inject(method = "getEffectStrength", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetEffectStrength(ResourceLocation id, CallbackInfoReturnable<Double> cir) {
        if (ResearchConstants.BLOCK_PLACE_SPEED.equals(id)) {
            double speed = BoostColonyConfig.SERVER.placeSpeed.get();
            if (speed > 1.0) {
                double original = cir.getReturnValue();
                double boosted = 1.0 - ((1.0 - original) / speed);
                cir.setReturnValue(Math.min(0.999, Math.max(original, boosted)));
            }
        } else if (ResearchConstants.BLOCK_BREAK_SPEED.equals(id)) {
            double speed = BoostColonyConfig.SERVER.breakSpeed.get();
            if (speed > 1.0) {
                double original = cir.getReturnValue();
                double boosted = 1.0 - ((1.0 - original) / speed);
                cir.setReturnValue(Math.min(0.999, Math.max(original, boosted)));
            }
        }
    }
}
