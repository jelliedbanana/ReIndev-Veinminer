package com.jellied.veinminer;

import net.minecraft.src.game.level.World;
import org.lwjgl.Sys;

import java.util.ArrayList;

public class WhitelistHandlerClient {
    private static final ArrayList<Integer> whitelist = new ArrayList<>();
    private static World world;

    public static void onWorldChanged(World newWorld) {
        world = newWorld;
        whitelist.clear();

        if (world == null)
            return;

        String worldWhitelist = ((WorldInfoAccessorClient) world.worldInfo).getVeinmineWhitelist();

        if (worldWhitelist == null)
            return;

        for (String stringId : worldWhitelist.split("//")) {
            try {
                whitelist.add(Integer.valueOf(stringId));
            } catch(Exception ignored) {}
        }
    }

    public static void saveWhitelistToWorldInfo() {
        if (world == null)
            return;

        StringBuilder whitelistAsString = new StringBuilder();

        for (Integer integer : whitelist)
            whitelistAsString.append(integer).append("//");

        ((WorldInfoAccessorClient) world.worldInfo).setVeinmineWhitelist(whitelistAsString.toString());
    }

    public static ArrayList<Integer> getWhitelist() {
        return whitelist;
    }

    public static void addToWhitelist(int blockId) {
        whitelist.add(blockId);
        saveWhitelistToWorldInfo();
    }

    public static void removeFromWhitelist(int blockId) {
        whitelist.remove((Object)blockId);
        saveWhitelistToWorldInfo();
    }

    public static boolean isBlockWhitelisted(int blockId) {
        return whitelist.contains(blockId);
    }
}
