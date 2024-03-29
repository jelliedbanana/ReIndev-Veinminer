package com.jellied.veinminer;

import com.fox2code.foxloader.loader.ServerMod;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import com.jellied.veinminer.chatcommands.VeinmineWhitelistChatCommandServer;
import net.minecraft.src.game.block.*;
import net.minecraft.src.game.entity.player.EntityPlayerMP;
import net.minecraft.src.game.item.*;
import net.minecraft.src.game.level.World;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VeinminerServer extends Veinminer implements ServerMod {
    static final int MAX_VEINMINE_ITERATIONS = 10;
    public static final String loggingPrefix = "[ReIndevVeinminer] ";

    public static final String veinminerBasePath = "mods/ReIndevVeinminer/";
    public static final String whitelistFilename = veinminerBasePath + "whitelist.txt";

    public static ArrayList<Integer> whitelist = new ArrayList<>();

    private static int getDifferenceBetween(int x1, int x2) {
        return Math.abs(x1 - x2);
    }

    private static boolean areCoordinatesDuplicate(ArrayList<ArrayList<Integer>> allCoords, ArrayList<Integer> coords) {
        for (int i = 0; i <= allCoords.size() - 1; i++) {
            ArrayList<Integer> theseCoords = allCoords.get(i);
            if (theseCoords.equals(coords))
                return true;
        }

        return false;
    }

    private static boolean isLogNatural(World world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & 3) <= 2;
    }

    private static int getTotalLogHeight(World world, int x, int startY, int z) {
        int height = 0;

        // Check above
        for (int y = startY; y < startY + Veinminer.maxLogHeightSearch; y++) {
            if (!(Block.blocksList[world.getBlockId(x, y, z)] instanceof BlockLog))
                break;

            if (isLogNatural(world, x, y, z))
                ++height;
        }

        return height;
    }

    private static void doNormalVeinSearch(ArrayList<ArrayList<Integer>> allCoords, World world, int targetBlockId, int x, int y, int z, Integer totalIterations) {
        int blockId = world.getBlockId(x, y, z);
        if (blockId != targetBlockId)
            return;

        if (totalIterations >= MAX_VEINMINE_ITERATIONS)
            return;

        if (Block.blocksList[blockId] instanceof BlockLog && !isLogNatural(world, x, y, z))
            return;

        ArrayList<Integer> coords = new ArrayList<>();
        coords.add(x);
        coords.add(y);
        coords.add(z);
        if (areCoordinatesDuplicate(allCoords, coords))
            return;

        allCoords.add(coords);
        totalIterations++;

        // This heavily favors veinmining toward negative X

        // Check left, right, below, above, front, and back
        // I LOVE RECURSION!!!!!!!!!!!!!!!!!
        doNormalVeinSearch(allCoords, world, targetBlockId,x - 1, y, z, totalIterations);
        doNormalVeinSearch(allCoords, world, targetBlockId,x + 1, y, z, totalIterations);
        doNormalVeinSearch(allCoords, world, targetBlockId, x,y - 1, z, totalIterations);
        doNormalVeinSearch(allCoords, world, targetBlockId, x,y + 1, z, totalIterations);
        doNormalVeinSearch(allCoords, world, targetBlockId, x, y, z - 1, totalIterations);
        doNormalVeinSearch(allCoords, world, targetBlockId, x, y, z + 1, totalIterations);
    }

    // I LOVE NESTED LOOPS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static void doTreeVeinSearch(ArrayList<ArrayList<Integer>> allCoords, World world, int startX, int startY, int startZ) {
        int treeHeight = getTotalLogHeight(world, startX, startY, startZ);
        for (int y = startY; y <= startY + treeHeight; y++) {
            for (int x = startX - 5; x <= startX + 5; x++) {
                for (int z = startZ - 5; z <= startZ + 5; z++) {
                    int blockAtPos = world.getBlockId(x, y, z);
                    if (!(Block.blocksList[blockAtPos] instanceof BlockLog) | !isLogNatural(world, x, y, z))
                        continue;

                    // The big trees may spawn a multi-block tall branch that is within 1 block of the base log.
                    // That's what this check is for.
                    if (getTotalLogHeight(world, x, y, z) > 1) {
                        int distX = getDifferenceBetween(startX, x);
                        int distZ = getDifferenceBetween(startZ, z);

                        if (distX > 1 && distZ > 1)
                            continue;
                    }

                    ArrayList<Integer> coords = new ArrayList<>();
                    coords.add(x);
                    coords.add(y);
                    coords.add(z);
                    if (areCoordinatesDuplicate(allCoords, coords))
                        continue;

                    allCoords.add(coords);
                }
            }
        }
    }

    static ArrayList<ArrayList<Integer>> getVein(World world, int targetBlockId, int startX, int startY, int startZ) {
        ArrayList<ArrayList<Integer>> allCoords = new ArrayList<>();

        if (Block.blocksList[targetBlockId] instanceof BlockLog && isLogNatural(world, startX, startY, startZ))
            doTreeVeinSearch(allCoords, world, startX, startY, startZ);
        else
            doNormalVeinSearch(allCoords, world, targetBlockId, startX, startY, startZ, 0);

        return allCoords;
    }

    public static boolean isBlockDefaultVeinmineable(Block block) {
        // I wish I could write a neat little loop for this instead of a chunky
        // block of or's
        return block instanceof BlockOre ||
                block instanceof BlockOreCoal ||
                block instanceof BlockOreMetal ||
                block instanceof BlockGravel ||
                block instanceof BlockLog ||
                block instanceof BlockClay;
    }

    // Main methods
    public void veinmine(EntityPlayerMP plr, int x, int y, int z) {
        World world = plr.worldObj;

        // without this check we get a NullPointerException if the player breaks a block with their fist
        if (!plr.isSneaking() | !plr.isHoldingTool())
            return;

        ItemStack stack = plr.getCurrentEquippedItem();
        Item tool = stack.getItem();

        int blockId = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockId];

        final boolean isWhitelisted = whitelist.contains(blockId);
        final boolean isBlockVeinminable = isBlockDefaultVeinmineable(block) || isWhitelisted;

        if (!isBlockVeinminable)
            return;

        if (!tool.canHarvestBlock(block))
            return;

        if (block instanceof BlockLog) {
            int meta = world.getBlockMetadata(x, y, z);
            // no idea what (meta & 3) means, it's just how my decompiler displays the code that
            // checks for a naturally spawned log.
            if ((meta & 3) > 2) {
                // In any case, if it's less than 2, that means it's a naturally spawned log
                // Otherwise, it's a player placed log, and we don't wanna veinmine a whole ass house.

                return;
            }
        }

        world.removeBlockTileEntity(x, y, z);
        ArrayList<ArrayList<Integer>> coordsToVeinmine = getVein(world, blockId, x, y, z);
        for (int i = 0; i <= coordsToVeinmine.size() - 1; i++) {
            int currentDamage = stack.getItemDamage();
            if (currentDamage >= stack.getMaxDamage())
                break; // Preserve the tool if it's about to break.

            ArrayList<Integer> coords = coordsToVeinmine.get(i);

            int thisX = coords.get(0);
            int thisY = coords.get(1);
            int thisZ = coords.get(2);
            int metadata = world.getBlockMetadata(thisX, thisY, thisZ);
            int thisBlockId = world.getBlockId(thisX, thisY, thisZ);

            if (thisBlockId != blockId)
                continue; // For leaf edge cases.

            if (plr.gamemode != 1) // Don't drop the block items when in creative mode
                block.harvestBlock(world, plr, thisX, thisY, thisZ, metadata);

            block.onBlockDestroyedByPlayer(world, thisX, thisY, thisZ, metadata);

            if (plr.gamemode != 1) // Don't lose durability when in creative mode
                stack.onDestroyBlock(blockId, thisX, thisY, thisZ, plr);

            world.setBlockWithNotify(thisX, thisY, thisZ, 0);

            // Let's not play too many sounds, it can get disproportionally loud compared to the rest of the game without this
            if (i < 4)
                world.playAuxSFX(2001, x, y, z, blockId + metadata * 65536);
        }
    }

    // This is a hack to make veinmining work in creative mode for servers
    @Override
    public boolean onPlayerStartBreakBlock(NetworkPlayer networkPlayer, RegisteredItemStack itemStack, int x, int y, int z, int facing, boolean cancelled) {
        if (ServerMod.toEntityPlayerMP(networkPlayer).gamemode != 1)
            return false;

        veinmine(ServerMod.toEntityPlayerMP(networkPlayer), x, y, z);
        return false;
    }

    public boolean onPlayerBreakBlock(NetworkPlayer networkPlayer, RegisteredItemStack stack, int x, int y, int z, int facing, boolean wasCancelled) {
        veinmine(ServerMod.toEntityPlayerMP(networkPlayer), x, y, z);

        return false;
    }

    public void onInit() {
        CommandCompat.registerCommand(new VeinmineWhitelistChatCommandServer());
        whitelist = FileWriteAndLoadWhitelist.loadWhitelistFromFile(whitelistFilename);
        System.out.println("ReIndevVeinminer initialized");
    }

    @Override
    public void onServerStop(NetworkPlayer.ConnectionType connectionType) {
        // Make sure mods/ReIndevVeinminer directory exists
        Path modsPath = Paths.get(veinminerBasePath);
        try {
            Files.createDirectory(modsPath);
        } catch (IOException e){
            if (!(e instanceof FileAlreadyExistsException)) {
                System.err.println(loggingPrefix + veinminerBasePath + " was not found, unable to store mod data");
                e.printStackTrace();
                return;
            }
        }

        FileWriteAndLoadWhitelist.writeWhitelistToFile(whitelist, whitelistFilename);
    }
}
