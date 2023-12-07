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

        if (world == null) {
            return;
        }

        String worldWhitelist = ((WorldInfoAccessorClient) world.worldInfo).getVeinmineWhitelist();

        if (worldWhitelist == null) {
            return;
        }

        for (String stringId : worldWhitelist.split("//")) {
            try {
                whitelist.add(Integer.valueOf(stringId));
            }
            catch(Exception ignored) {
                return;
            }
        }
    }

    public static void saveWhitelistToWorldInfo() {
        if (world != null) {
            String whitelistAsString = "";

            for (Integer integer : whitelist) {
                // What the fuck is a StringBuilder
                whitelistAsString += integer + "//";
            }

            ((WorldInfoAccessorClient) world.worldInfo).setVeinmineWhitelist(whitelistAsString);
        }
    }

    public static void addToWhitelist(int blockId) {
        whitelist.add(new Integer(blockId));
        saveWhitelistToWorldInfo();
    }

    public static void removeFromWhitelist(int blockId) {
        whitelist.remove(new Integer(blockId));
        saveWhitelistToWorldInfo();
    }

    public static boolean isBlockWhitelisted(int blockId) {
        return whitelist.contains(blockId);
    }
}
