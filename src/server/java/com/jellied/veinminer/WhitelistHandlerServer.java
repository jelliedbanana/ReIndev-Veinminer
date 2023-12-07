package com.jellied.veinminer;

import net.minecraft.src.game.level.World;

import java.util.ArrayList;

public class WhitelistHandlerServer {
    private static final ArrayList<Integer> whitelist = new ArrayList<>();
    private static World world;

    public static void onWorldChanged(World newWorld) {
        world = newWorld;
        whitelist.clear();

        if (world == null) {
            return;
        }

        String worldWhitelist = ((WorldInfoAccessorServer) world.getWorldInfo()).getVeinmineWhitelist();
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

            ((WorldInfoAccessorServer) world.getWorldInfo()).setVeinmineWhitelist(whitelistAsString);
        }
    }

    public static void addToWhitelist(int blockId) {
        whitelist.add(blockId);
        saveWhitelistToWorldInfo();
    }

    public static void removeFromWhitelist(int blockId) {
        whitelist.remove(blockId);
        saveWhitelistToWorldInfo();
    }

    public static boolean isBlockWhitelisted(int blockId) {
        return whitelist.contains(blockId);
    }
}
