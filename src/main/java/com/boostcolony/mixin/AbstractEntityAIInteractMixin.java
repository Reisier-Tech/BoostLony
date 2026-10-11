package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAISkill;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("rawtypes")
@Mixin(value = AbstractEntityAIInteract.class, remap = false)
public abstract class AbstractEntityAIInteractMixin extends AbstractEntityAISkill {

    @SuppressWarnings("unchecked")
    protected AbstractEntityAIInteractMixin() {
        super(null);
    }

    @ModifyConstant(method = "checkMiningLocation", constant = @Constant(doubleValue = 12.0D), remap = false)
    private double modifyMiningMaxDistance(double original) {
        return (double) BoostColonyConfig.SERVER.placeBlockRange.get().intValue();
    }

    /**
     * Eliminate mining delay when breakSpeed is boosted, allowing instant/burst block destruction.
     */
    @Inject(method = "checkMiningLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCheckMiningLocation(BlockPos blockPos, BlockPos workFrom, CallbackInfoReturnable<Boolean> cir) {
        if (blockPos == null) {
            cir.setReturnValue(true);
            return;
        }

        BlockState state = this.world.getBlockState(blockPos);
        if (!this.holdEfficientTool(state, blockPos)) {
            cir.setReturnValue(true);
            return;
        }

        if (workFrom != null && this.walkWithProxy(workFrom)) {
            double range = (double) BoostColonyConfig.SERVER.placeBlockRange.get().intValue();
            if (MathUtils.twoDimDistance(this.worker.blockPosition(), workFrom) > range) {
                cir.setReturnValue(true);
                return;
            }
        }

        this.currentWorkingLocation = blockPos;

        if (BoostColonyConfig.SERVER.breakSpeed.get() > 1.0) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Ensure mining time queries return 0 when breakSpeed is boosted.
     */
    @Inject(method = "getBlockMiningTime", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetBlockMiningTime(BlockState blockState, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (BoostColonyConfig.SERVER.breakSpeed.get() > 1.0) {
            cir.setReturnValue(0);
        }
    }
}
