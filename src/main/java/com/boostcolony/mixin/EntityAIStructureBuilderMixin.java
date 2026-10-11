package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.core.entity.ai.workers.builder.EntityAIStructureBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EntityAIStructureBuilder.class, remap = false)
public abstract class EntityAIStructureBuilderMixin {

    @ModifyConstant(method = "walkToConstructionSite", constant = @Constant(longValue = 5L), remap = false)
    private long modifyReachDistance2D(long original) {
        return (long) BoostColonyConfig.SERVER.placeBlockRange.get().intValue();
    }

    @ModifyConstant(method = "walkToConstructionSite", constant = @Constant(doubleValue = 10.0D), remap = false)
    private double modifyPrevBlockDistance(double original) {
        return (double) BoostColonyConfig.SERVER.placeBlockRange.get().intValue();
    }

    @ModifyConstant(method = "walkToConstructionSite", constant = @Constant(intValue = 4), remap = false)
    private int modifyPathJobDistance(int original) {
        return Math.max(2, BoostColonyConfig.SERVER.placeBlockRange.get() - 2);
    }
}
