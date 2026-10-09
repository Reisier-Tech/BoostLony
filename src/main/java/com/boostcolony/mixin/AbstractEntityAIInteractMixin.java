package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = AbstractEntityAIInteract.class, remap = false)
public abstract class AbstractEntityAIInteractMixin {

    @ModifyConstant(method = "getBlockMiningTime", constant = @Constant(intValue = AbstractEntityAIInteract.BLOCK_MINING_DELAY / 2), remap = false)
    private int modifyMiningTime(int original) {
        return Math.max(1, BoostColonyConfig.SERVER.miningDelay.get() / 2);
    }

    @ModifyConstant(method = "calculateWorkerMiningDelay", constant = @Constant(doubleValue = (double) AbstractEntityAIInteract.BLOCK_MINING_DELAY), remap = false)
    private double modifyMiningDelay(double original) {
        return (double) BoostColonyConfig.SERVER.miningDelay.get();
    }
}
