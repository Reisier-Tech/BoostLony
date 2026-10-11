package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.core.colony.buildings.utils.BuilderBucket;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

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

    @Shadow(remap = false)
    protected Tuple<StructurePlacer, BuildingStructureHandler<?, ?>> structurePlacer;

    @Shadow(remap = false)
    private int pickUpCount;

    private IAIState getCurrentState() {
        return ((AbstractAISkeleton<?>) (Object) this).getState();
    }

    private static final ThreadLocal<Boolean> IN_BURST = ThreadLocal.withInitial(() -> false);

    /**
     * Rapid batch-gathering: gathers all available building materials from the hut chest in one pass,
     * bypassing the 20-tick-per-item sequential delays while preserving colony requests for missing items.
     */
    @Inject(method = "pickUpMaterial", at = @At("HEAD"), cancellable = true, remap = false)
    private void onPickUpMaterial(CallbackInfoReturnable<IAIState> cir) {
        if (this.structurePlacer == null || this.structurePlacer.getB() == null || !this.structurePlacer.getB().hasBluePrint()) {
            return;
        }

        BuildingProgressStage stage = this.structurePlacer.getB().getStage();
        if (stage == null || stage == BuildingProgressStage.CLEAR) {
            return;
        }

        AbstractBuilding building = ((AbstractEntityAIBasic<?, ?>) (Object) this).building;
        if (!(building instanceof AbstractBuildingStructureBuilder structureBuilder)) {
            return;
        }

        if (structureBuilder.getTileEntity() == null) {
            return;
        }

        AbstractEntityCitizen worker = this.getWorker();
        if (worker == null || worker.getInventoryCitizen() == null) {
            return;
        }

        InventoryCitizen inv = worker.getInventoryCitizen();
        if (InventoryUtils.openSlotCount(inv) <= 5L) {
            structureBuilder.checkOrRequestBucket(structureBuilder.getRequiredResources(), worker.getCitizenData());
            structureBuilder.checkOrRequestBucket(structureBuilder.getNextBucket(), worker.getCitizenData());
            this.pickUpCount = 0;
            cir.setReturnValue(AIWorkerState.START_WORKING);
            return;
        }

        BuilderBucket bucket = structureBuilder.getRequiredResources();
        if (bucket == null || bucket.getResourceMap().isEmpty()) {
            return;
        }

        BuildingResourcesModule module = structureBuilder.getFirstModuleOccurance(BuildingResourcesModule.class);
        if (module == null) {
            return;
        }

        double range = BoostColonyConfig.SERVER.placeBlockRange.get();
        BlockPos buildingPos = structureBuilder.getPosition();
        boolean inReach = buildingPos != null && worker.blockPosition().closerThan(buildingPos, range * 2.0);

        if (!inReach && structureBuilder.getTileEntity() != null) {
            BlockPos hutChestPos = structureBuilder.getTileEntity().getBlockPos();
            if (hutChestPos != null && worker.blockPosition().closerThan(hutChestPos, range * 2.0)) {
                inReach = true;
            }
        }

        if (!inReach) {
            return;
        }

        // Worker is in reach: transfer all available needed items in one pass!
        for (Map.Entry<String, Integer> entry : bucket.getResourceMap().entrySet()) {
            if (InventoryUtils.openSlotCount(inv) <= 5L) {
                break;
            }

            BuildingBuilderResource res = module.getResourceFromIdentifier(entry.getKey());
            if (res == null) {
                continue;
            }

            ItemStack template = res.getItemStack();
            Predicate<ItemStack> pred = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(template, stack);

            int requiredAmount = entry.getValue();
            int inWorkerInv = InventoryUtils.getItemCountInItemHandler(inv, pred);
            int needed = requiredAmount - inWorkerInv;
            if (needed <= 0) {
                continue;
            }

            BlockPos chestPos = structureBuilder.getTileEntity().getPositionOfChestWithItemStack(pred);
            if (chestPos != null) {
                BlockEntity chestTile = worker.level().getBlockEntity(chestPos);
                if (chestTile != null) {
                    InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandlerWithResult(
                        chestTile, pred, needed, inv
                    );
                }
            }
        }

        structureBuilder.checkOrRequestBucket(structureBuilder.getRequiredResources(), worker.getCitizenData());
        structureBuilder.checkOrRequestBucket(structureBuilder.getNextBucket(), worker.getCitizenData());
        this.pickUpCount = 0;
        cir.setReturnValue(AIWorkerState.START_WORKING);
    }

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
