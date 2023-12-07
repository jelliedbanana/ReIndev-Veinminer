package com.jellied.veinminer.server.mixins;

import com.jellied.veinminer.WhitelistHandlerServer;
import com.jellied.veinminer.WorldInfoAccessorServer;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "saveLevel", at = @At("HEAD"))
    public void beforeSave(CallbackInfo ci) {
        WhitelistHandlerServer.saveWhitelistToWorldInfo();
    }
}
