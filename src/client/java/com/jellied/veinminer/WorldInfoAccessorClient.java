package com.jellied.veinminer;

import net.minecraft.src.game.nbt.NBTTagCompound;

public interface WorldInfoAccessorClient {
    String getVeinmineWhitelist();
    void setVeinmineWhitelist(String whitelist);
}
