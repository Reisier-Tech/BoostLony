package com.boostcolony.mixin;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenSkillHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CitizenSkillHandler.class, remap = false)
public abstract class CitizenSkillHandlerMixin {

    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetLevel(Skill skill, CallbackInfoReturnable<Integer> cir) {
        if (BoostColonyConfig.SERVER.maxCitizenSkills.get()) {
            cir.setReturnValue(BoostColonyConfig.SERVER.maxSkillLevel.get());
        }
    }
}
