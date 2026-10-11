package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = AbstractEntityAIInteract.class, remap = false)
public abstract class AbstractEntityAIInteractMixin {

    @ModifyConstant(method = "checkMiningLocation", constant = @Constant(doubleValue = 12.0D), remap = false)
    private double modifyMiningMaxDistance(double original) {
        return (double) BoostColonyConfig.SERVER.placeBlockRange.get().intValue();
    }
}
