package com.jellied.veinminer.client.mixins;

import com.jellied.veinminer.VeinminerClient;
import com.jellied.veinminer.WorldInfoAccessorClient;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.level.WorldInfo;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WorldInfo.class)
public class MixinWorldInfo implements WorldInfoAccessorClient {
    private int[] veinmineWhitelist;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onWorldInfoConstructedWithTag(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("jelliedveinminewhitelist")) {
            veinmineWhitelist = tag.getIntArray("jelliedveinminewhitelist");
        }
        else {
            veinmineWhitelist = new int[Block.blocksList.length];
            tag.setIntArray("jelliedveinminewhitelist", veinmineWhitelist);
        }

        VeinminerClient.veinmineWhitelist = veinmineWhitelist;
    }

    @Inject(method = "<init>(Lnet/minecraft/src/game/level/WorldInfo;)V", at = @At("TAIL"))
    public void onWorldInfoConstructedWithWorldInfo(WorldInfo worldInfo, CallbackInfo ci) {
        veinmineWhitelist = ((WorldInfoAccessorClient) worldInfo).getVeinmineWhitelist();
        if (veinmineWhitelist == null) {
            veinmineWhitelist = new int[Block.blocksList.length];
        }

        this.setVeinmineWhitelist(veinmineWhitelist);
    }

    @Inject(method = "updateTagCompound", at = @At("TAIL"))
    public void onWorldInfoUpdated(NBTTagCompound newTag, NBTTagCompound plrTag, CallbackInfo ci) {
        newTag.setIntArray("jelliedveinminewhitelist", veinmineWhitelist);
    }

    @Override
    public int[] getVeinmineWhitelist() {
        setVeinmineWhitelist(new int[Block.blocksList.length]);

        return veinmineWhitelist;
    }

    @Override
    public void setVeinmineWhitelist(int[] newWhitelist) {
        veinmineWhitelist = newWhitelist;
        VeinminerClient.veinmineWhitelist = veinmineWhitelist;
    }
}
