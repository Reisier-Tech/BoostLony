package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = AbstractEntityAIStructure.class, remap = false)
public abstract class AbstractEntityAIStructureMixin {

    @Shadow(remap = false)
    protected abstract IAIState structureStep();

    @Shadow(remap = false)
    public abstract IAIState doMining();

    @Shadow(remap = false)
    protected abstract boolean isThereAStructureToBuild();

    @Shadow(remap = false)
    public abstract AbstractEntityCitizen getWorker();

    @Shadow(remap = false)
    public abstract Tuple<BlockPos, BuildingProgressStage> getProgressPos();

    @Shadow(remap = false)
    protected BlockPos blockToMine;

    private IAIState getCurrentState() {
        return ((AbstractAISkeleton<?>) (Object) this).getState();
    }

    private static final ThreadLocal<Boolean> IN_BURST = ThreadLocal.withInitial(() -> false);

    @Inject(method = "structureStep", at = @At("RETURN"), cancellable = true, remap = false)
    private void onStructureStepBurst(CallbackInfoReturnable<IAIState> cir) {
        if (IN_BURST.get()) {
            return;
        }

        IAIState currentRet = cir.getReturnValue();
        if (currentRet != AIWorkerState.BUILDING_STEP && currentRet != this.getCurrentState()) {
            return;
        }

        int limit = (int) Math.min(100.0, Math.max(1.0, BoostColonyConfig.SERVER.placeSpeed.get()));
        if (limit <= 1) {
            return;
        }

        AbstractEntityCitizen worker = this.getWorker();
        if (worker == null || (worker.getNavigation() != null && worker.getNavigation().isInProgress())) {
            return;
        }

        IN_BURST.set(true);
        try {
            for (int i = 1; i < limit; i++) {
                if (!this.isThereAStructureToBuild()) {
                    break;
                }
                if (worker.getNavigation() != null && worker.getNavigation().isInProgress()) {
                    break;
                }
                if (worker.getInventoryCitizen() != null && !worker.getInventoryCitizen().hasSpace()) {
                    break;
                }

                Tuple<BlockPos, BuildingProgressStage> prevProgress = this.getProgressPos();
                BlockPos prevBlockToMine = this.blockToMine;

                currentRet = this.structureStep();
                cir.setReturnValue(currentRet);

                if (currentRet != AIWorkerState.BUILDING_STEP && currentRet != this.getCurrentState()) {
                    break;
                }

                if (this.blockToMine != null) {
                    break;
                }

                if (worker.getNavigation() != null && worker.getNavigation().isInProgress()) {
                    break;
                }

                Tuple<BlockPos, BuildingProgressStage> currProgress = this.getProgressPos();
                if (Objects.equals(prevProgress, currProgress) && Objects.equals(prevBlockToMine, this.blockToMine)) {
                    break;
                }
            }
        } finally {
            IN_BURST.set(false);
        }
    }

    @Inject(method = "doMining", at = @At("RETURN"), cancellable = true, remap = false)
    private void onDoMiningBurst(CallbackInfoReturnable<IAIState> cir) {
        if (IN_BURST.get()) {
            return;
        }

        IAIState currentRet = cir.getReturnValue();
        if (currentRet != AIWorkerState.BUILDING_STEP) {
            return;
        }

        int limit = (int) Math.min(100.0, Math.max(1.0, BoostColonyConfig.SERVER.breakSpeed.get()));
        if (limit <= 1) {
            return;
        }

        AbstractEntityCitizen worker = this.getWorker();
        if (worker == null || (worker.getNavigation() != null && worker.getNavigation().isInProgress())) {
            return;
        }

        IN_BURST.set(true);
        try {
            for (int i = 1; i < limit; i++) {
                if (!this.isThereAStructureToBuild()) {
                    break;
                }
                if (worker.getNavigation() != null && worker.getNavigation().isInProgress()) {
                    break;
                }
                if (worker.getInventoryCitizen() != null && !worker.getInventoryCitizen().hasSpace()) {
                    break;
                }

                Tuple<BlockPos, BuildingProgressStage> prevProgress = this.getProgressPos();
                BlockPos prevBlockToMine = this.blockToMine;

                currentRet = this.structureStep();
                cir.setReturnValue(currentRet);

                if (currentRet == AIWorkerState.MINE_BLOCK && this.blockToMine != null) {
                    currentRet = this.doMining();
                    cir.setReturnValue(currentRet);
                }

                if (currentRet != AIWorkerState.BUILDING_STEP) {
                    break;
                }

                if (worker.getNavigation() != null && worker.getNavigation().isInProgress()) {
                    break;
                }

                Tuple<BlockPos, BuildingProgressStage> currProgress = this.getProgressPos();
                if (Objects.equals(prevProgress, currProgress) && Objects.equals(prevBlockToMine, this.blockToMine)) {
                    break;
                }
            }
        } finally {
            IN_BURST.set(false);
        }
    }
}
