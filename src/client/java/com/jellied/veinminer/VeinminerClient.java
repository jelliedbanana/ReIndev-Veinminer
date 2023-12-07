package com.jellied.veinminer;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.loader.ModContainer;
import com.fox2code.foxloader.loader.ModLoader;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import com.jellied.veinminer.chatcommands.CommandWhitelistAutocomplete;
import com.jellied.veinminer.chatcommands.VeinmineWhitelistChatCommandClient;
import net.minecraft.client.Minecraft;
import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.game.block.*;
import net.minecraft.src.game.item.*;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class VeinminerClient extends Veinminer implements ClientMod {
    static final int MAX_VEINMINE_ITERATIONS = 10;
    final Minecraft MINECRAFT = Minecraft.getInstance();

    // Utility methods
    private static int getMagnitudeBetween(int x1, int x2) {
        return Math.abs(x1 - x2);
    }

    private static boolean areCoordinatesDuplicate(ArrayList<ArrayList<Integer>> allCoords, ArrayList<Integer> coords) {
        for (int i = 0; i <= allCoords.size() - 1; i++) {
            ArrayList<Integer> theseCoords = allCoords.get(i);
            if (theseCoords.equals(coords)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isLogNatural(World world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & 0x3) <= 2;
    }

    private static int getTotalLogHeight(World world, int x, int startY, int z) {
        int height = 0;

        // Check above
        for (int y = startY; y < startY + 100; y++) {
            if (!(Block.blocksList[world.getBlockId(x, y, z)] instanceof BlockLog)) {
                break;
            }

            if (isLogNatural(world, x, y, z)) {
                height++;
            }
        }

        return height;
    }

    private static void doNormalVeinSearch(ArrayList<ArrayList<Integer>> allCoords, World world, int targetBlockId, int x, int y, int z, Integer totalIterations) {
        int blockId = world.getBlockId(x, y, z);
        if (blockId == targetBlockId) {
            if (totalIterations >= MAX_VEINMINE_ITERATIONS) {
                return;
            }

            if (Block.blocksList[blockId] instanceof BlockLog && !isLogNatural(world, x, y, z)) {
                return;
            }


            ArrayList<Integer> coords = new ArrayList<>();
            coords.add(x);
            coords.add(y);
            coords.add(z);
            if (areCoordinatesDuplicate(allCoords, coords)) {
                return;
            }

            allCoords.add(coords);
            totalIterations++;

            // Check left, right, below, above, front, and back
            // I LOVE RECURSION!!!!!!!!!!!!!!!!!
            doNormalVeinSearch(allCoords, world, targetBlockId,x - 1, y, z, totalIterations);
            doNormalVeinSearch(allCoords, world, targetBlockId,x + 1, y, z, totalIterations);
            doNormalVeinSearch(allCoords, world, targetBlockId, x,y - 1, z, totalIterations);
            doNormalVeinSearch(allCoords, world, targetBlockId, x,y + 1, z, totalIterations);
            doNormalVeinSearch(allCoords, world, targetBlockId, x, y, z - 1, totalIterations);
            doNormalVeinSearch(allCoords, world, targetBlockId, x, y, z + 1, totalIterations);
        }
    }

    // I LOVE NESTED LOOPS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static void doTreeVeinSearch(ArrayList<ArrayList<Integer>> allCoords, World world, int startX, int startY, int startZ) {
        int treeHeight = getTotalLogHeight(world, startX, startY, startZ);
        for (int y = startY; y <= startY + treeHeight; y++) {
            for (int x = startX - 5; x <= startX + 5; x++) {
                for (int z = startZ - 5; z <= startZ + 5; z++) {
                    int blockAtPos = world.getBlockId(x, y, z);
                    if (!(Block.blocksList[blockAtPos] instanceof BlockLog) | !isLogNatural(world, x, y, z)) {
                        continue;
                    }

                    // The big trees may spawn a multi-block tall branch that is within 1 block of the base log.
                    // That's what this check is for.
                    if (getTotalLogHeight(world, x, y, z) > 1) {
                        int distX = getMagnitudeBetween(startX, x);
                        int distZ = getMagnitudeBetween(startZ, z);

                        if (distX > 1 && distZ > 1) {
                            continue;
                        }
                    }

                    ArrayList<Integer> coords = new ArrayList<>();
                    coords.add(x);
                    coords.add(y);
                    coords.add(z);
                    if (areCoordinatesDuplicate(allCoords, coords)) {
                        continue;
                    }

                    allCoords.add(coords);
                }
            }
        }
    }

    static ArrayList<ArrayList<Integer>> getVein(World world, int targetBlockId, int startX, int startY, int startZ) {
        ArrayList<ArrayList<Integer>> allCoords = new ArrayList<>();

        if (Block.blocksList[targetBlockId] instanceof BlockLog && isLogNatural(world, startX, startY, startZ)) {
            doTreeVeinSearch(allCoords, world, startX, startY, startZ);
        }
        else {
            doNormalVeinSearch(allCoords, world, targetBlockId, startX, startY, startZ, 0);
        }

        return allCoords;
    }

    static boolean isBlockVeinmineable(Block block) {
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
    public void veinmine(EntityPlayerSP plr, int x, int y, int z) {
        World world = plr.worldObj;
        if (!plr.isSneaking() | !plr.isHoldingTool()) {
            // without this check we get a NullPointerException if the player breaks a block with their fist
            return;
        }

        ItemStack stack = plr.getCurrentEquippedItem();
        Item tool = stack.getItem();

        int blockId = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockId];
        if (!tool.canHarvestBlock(block)) {
            return;
        }

        if (!isBlockVeinmineable(block) && !WhitelistHandlerClient.isBlockWhitelisted(blockId)) {
            return;
        }

        if (block instanceof BlockLog) {
            int meta = world.getBlockMetadata(x, y, z);
            // no idea what (meta 0x3) means, it's just how my decompiler displays the code that
            // checks for a naturally spawned log.
            if ((meta & 0x3) > 2) {
                // In any case, if it's less than 2, that means it's a naturally spawned log
                // Otherwise, it's a player placed log, and we don't wanna veinmine a whole ass house.

                return;
            }
        }

        world.removeBlockTileEntity(x, y, z);
        ArrayList<ArrayList<Integer>> coordsToVeinmine = getVein(world, blockId, x, y, z);
        for (int i = 0; i <= coordsToVeinmine.size() - 1; i++) {
            int currentDamage = stack.getItemDamage();
            if (currentDamage >= stack.getMaxDamage()) {
                break; // Preserve the tool if it's about to break.
            }

            ArrayList<Integer> coords = coordsToVeinmine.get(i);

            int thisX = (coords.get(0));
            int thisY = (coords.get(1));
            int thisZ = (coords.get(2));
            int metadata = world.getBlockMetadata(thisX, thisY, thisZ);
            int thisBlockId = world.getBlockId(thisX, thisY, thisZ);

            if (thisBlockId != blockId) {
                continue; // For leaf edge cases.
            }

            block.harvestBlock(world, plr, thisX, thisY, thisZ, metadata);
            block.onBlockDestroyedByPlayer(world, thisX, thisY, thisZ, metadata);
            stack.onDestroyBlock(blockId, thisX, thisY, thisZ, plr);
            world.playAuxSFX(2001, x, y, z, blockId + metadata * 65536);
            world.setBlockWithNotify(thisX, thisY, thisZ, 0);
        }
    }

    public boolean onPlayerBreakBlock(NetworkPlayer plr, RegisteredItemStack stack, int x, int y, int z, int facing, boolean wasCancelled) {
        boolean isSingleplayer = !MINECRAFT.isMultiplayerWorld();

        if (isSingleplayer) {
            veinmine(MINECRAFT.thePlayer, x, y, z);
        }

        return false;
    }

    public void onInit() {
        initAutocomplete();
        CommandCompat.registerCommand(new VeinmineWhitelistChatCommandClient());

        System.out.println("Veinminer client initialized.");
    }

    public void initAutocomplete() {
        ModContainer autocompleteModContainer = ModLoader.getModContainer("jelliedautocomplete");

        if (autocompleteModContainer == null) {
            return;
        }

        Object clientMod = autocompleteModContainer.getClientMod();
        System.out.println("Detected autocomplete mod, initializing... ");

        try {
            Method addAutocompleteMethod =  clientMod.getClass().getMethod("addAutocomplete", String.class, Object.class);
            addAutocompleteMethod.invoke(clientMod, "/veinminewhitelist", new CommandWhitelistAutocomplete());

            System.out.println("Successfully initialized veinmine whitelist autocompletion.");
        }
        catch (Exception e) {
            System.out.println("Could not initialize veinmine whitelist autocompletion:");
            e.printStackTrace();
        }
    }
}
