package com.jellied.veinminer.client.mixins;

import com.jellied.veinminer.VeinminerClient;
import com.jellied.veinminer.WorldInfoAccessorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.src.game.level.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public World theWorld;

    @Inject(method = "changeWorld", at = @At("TAIL"))
    public void onWorldChanged(CallbackInfo ci) {
        World world = this.theWorld;

        if (world == null) {
            return;
        }

        VeinminerClient.veinmineWhitelist = ((WorldInfoAccessorClient) world.worldInfo).getVeinmineWhitelist();
    }
}
