package com.jellied.veinminer;

import net.minecraft.src.game.nbt.NBTTagCompound;

public interface WorldInfoAccessorClient {
    int[] getVeinmineWhitelist();
    void setVeinmineWhitelist(int[] whitelist);
}
