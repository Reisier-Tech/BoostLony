package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.core.colony.CitizenData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CitizenData.class, remap = false)
public abstract class CitizenDataMixin {

    @Shadow(remap = false)
    public abstract ICitizenSkillHandler getCitizenSkillHandler();

    @Inject(method = "update", at = @At("TAIL"), remap = false)
    private void onUpdate(int tickRate, CallbackInfo ci) {
        if (BoostColonyConfig.SERVER.autoMaxSkillsOnUpdate.get()) {
            ICitizenSkillHandler skillHandler = this.getCitizenSkillHandler();
            if (skillHandler != null) {
                int maxLevel = BoostColonyConfig.SERVER.maxSkillLevel.get();
                boolean changed = false;
                for (Skill skill : Skill.values()) {
                    int currentLevel = skillHandler.getLevel(skill);
                    if (currentLevel < maxLevel) {
                        skillHandler.incrementLevel(skill, maxLevel - currentLevel);
                        changed = true;
                    }
                }
                if (changed) {
                    CitizenData self = (CitizenData) (Object) this;
                    self.markDirty(10);
                }
            }
        }
    }
}
