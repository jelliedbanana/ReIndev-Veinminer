package com.jellied.veinminer.server.mixins;

import com.jellied.veinminer.WorldInfoAccessorServer;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.level.WorldInfo;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldInfo.class)
public class MixinWorldInfo implements WorldInfoAccessorServer {
    private String whitelist;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onWorldInfoConstructed(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("jelliedveinminewhitelist")) {
            whitelist = tag.getString("jelliedveinminewhitelist");
        }
        else {
            whitelist = "";
            tag.setString("jelliedveinminewhitelist", whitelist);
        }
    }

    @Inject(method = "saveNBTTag", at = @At("TAIL"))
    public void onNBTSave(NBTTagCompound newTag, NBTTagCompound plrTag, CallbackInfo ci) {
        newTag.setString("jelliedveinminewhitelist", whitelist);
    }

    @Override
    public String getVeinmineWhitelist() {
        return whitelist;
    }

    @Override
    public void setVeinmineWhitelist(String newWhitelist) {
        whitelist = newWhitelist;
    }
}
