package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractEntityAIStructure.class, remap = false)
public abstract class AbstractEntityAIStructureMixin {

    @Shadow(remap = false)
    protected BlockPos workFrom;

    @Shadow(remap = false)
    public abstract AbstractEntityCitizen getWorker();

    @ModifyConstant(method = "structureStep", constant = @Constant(intValue = 150), remap = false)
    private int modifyBuildBlockDelay(int originalDelay) {
        return BoostColonyConfig.SERVER.buildingDelay.get() * CitizenConstants.PROGRESS_MULTIPLIER;
    }

    @Inject(method = "walkToConstructionSite", at = @At("HEAD"), cancellable = true, remap = false)
    private void onWalkToConstructionSite(BlockPos currentBlock, CallbackInfoReturnable<Boolean> cir) {
        int customRange = BoostColonyConfig.SERVER.placeBlockRange.get();
        if (customRange > CitizenConstants.STANDARD_WORKING_RANGE) {
            AbstractEntityCitizen citizenWorker = this.getWorker();
            BlockPos target = this.workFrom != null ? this.workFrom : currentBlock;
            if (target != null && citizenWorker != null) {
                double distSq = citizenWorker.blockPosition().distSqr(target);
                if (distSq <= (double) (customRange * customRange)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
