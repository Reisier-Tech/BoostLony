package com.boostcolony.mixin;

import com.minecolonies.core.entity.ai.workers.service.EntityAIWorkDeliveryman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EntityAIWorkDeliveryman.class, remap = false)
public abstract class EntityAIWorkDeliverymanMixin {

    /**
     * Reduce pickup delay for deliverymen from 20 ticks (1 second) to 1 tick.
     */
    @ModifyConstant(method = "pickup", constant = @Constant(intValue = 20), remap = false)
    private int modifyPickupDelay(int original) {
        return 1;
    }

    /**
     * Reduce decision walking delay for deliverymen from 20 ticks to 2 ticks.
     */
    @ModifyConstant(method = "decide", constant = @Constant(intValue = 20), remap = false)
    private int modifyDecideDelay(int original) {
        return 2;
    }
}
