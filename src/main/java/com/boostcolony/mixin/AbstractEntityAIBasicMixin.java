package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.JobStatus;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = AbstractEntityAIBasic.class, remap = false)
public abstract class AbstractEntityAIBasicMixin extends AbstractAISkeleton<IJob<?>> {

    protected AbstractEntityAIBasicMixin(IJob<?> job) {
        super(job);
    }

    @Shadow(remap = false)
    protected Tuple<Predicate<ItemStack>, Integer> needsCurrently;

    @Shadow(remap = false)
    protected BlockPos walkTo;

    @Shadow(remap = false)
    private int pickUpCounter;

    @Shadow(remap = false)
    public AbstractBuilding building;

    @Shadow(remap = false)
    public abstract IAIState getStateAfterPickUp();

    @Shadow(remap = false)
    protected abstract boolean tryTransferFromPosToWorkerIfNeeded(BlockPos pos, Tuple<Predicate<ItemStack>, Integer> needs);

    @Shadow(remap = false)
    protected abstract int getMostEfficientTool(BlockState blockState, BlockPos pos);

    /**
     * Allow builders to break/destroy blocks without needing tools.
     * If a tool is in inventory, it is held; if not, blocks are destroyed bare-handed without tool requests.
     */
    @Inject(method = "holdEfficientTool", at = @At("HEAD"), cancellable = true, remap = false)
    private void onHoldEfficientTool(BlockState blockState, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        int slot = this.getMostEfficientTool(blockState, pos);
        if (slot >= 0) {
            if (this.worker != null && this.worker.getCitizenData() != null) {
                this.worker.getCitizenData().setJobStatus(JobStatus.WORKING);
                CitizenItemUtils.setHeldItem(this.worker, InteractionHand.MAIN_HAND, slot);
            }
            cir.setReturnValue(true);
            return;
        }

        // For builders (AbstractEntityAIStructure), allow tool-less block destruction:
        if ((Object) this instanceof AbstractEntityAIStructure) {
            if (this.worker != null) {
                if (this.worker.getCitizenData() != null) {
                    this.worker.getCitizenData().setJobStatus(JobStatus.WORKING);
                }
                CitizenItemUtils.removeHeldItem(this.worker);
            }
            cir.setReturnValue(true);
        }
    }

    /**
     * Expand the reach distance for citizens interacting with work positions/chests.
     */
    @Inject(method = "walkToWorkPos", at = @At("HEAD"), cancellable = true, remap = false)
    private void onWalkToWorkPos(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (pos == null) {
            cir.setReturnValue(true);
            return;
        }

        if (this.worker != null) {
            double range = BoostColonyConfig.SERVER.placeBlockRange.get();
            if (this.worker.blockPosition().closerThan(pos, range)) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * Fast-track needed item retrieval when the worker is already within reach of the chest.
     */
    @Inject(method = "getNeededItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetNeededItem(CallbackInfoReturnable<IAIState> cir) {
        if (this.needsCurrently == null) {
            cir.setReturnValue(this.getStateAfterPickUp());
            return;
        }

        if (this.walkTo == null) {
            if (this.building != null && this.building.getTileEntity() != null) {
                this.walkTo = this.building.getTileEntity().getPositionOfChestWithItemStack(this.needsCurrently.getA());
            }
            if (this.walkTo == null) {
                cir.setReturnValue(this.getStateAfterPickUp());
                return;
            }
        }

        if (this.worker != null && this.walkTo != null) {
            double range = BoostColonyConfig.SERVER.placeBlockRange.get();
            if (this.worker.blockPosition().closerThan(this.walkTo, range)) {
                this.pickUpCounter = 0;
                this.tryTransferFromPosToWorkerIfNeeded(this.walkTo, this.needsCurrently);
                this.walkTo = null;
                cir.setReturnValue(this.getStateAfterPickUp());
            }
        }
    }

    /**
     * Reduce the delay when waiting for/checking requests from 10 ticks to 1 tick.
     */
    @ModifyConstant(method = "waitForRequests", constant = @Constant(intValue = 10), remap = false)
    private int modifyWaitForRequestsDelay(int original) {
        return 1;
    }
}
