package com.jellied.veinminer;

import com.fox2code.foxloader.loader.ServerMod;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import net.minecraft.src.game.block.*;
import net.minecraft.src.game.entity.player.EntityPlayerMP;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.item.ItemToolPickaxe;
import net.minecraft.src.game.item.ItemToolSpade;
import net.minecraft.src.game.level.World;

import java.util.ArrayList;

public class VeinminerServer extends Veinminer implements ServerMod {
    static final int MAX_VEINMINE_RANGE = 10;

    private static boolean areCoordinatesDuplicate(ArrayList<ArrayList<Integer>> allCoords, ArrayList<Integer> coords) {
        for (int i = 0; i <= allCoords.size() - 1; i++) {
            ArrayList<Integer> theseCoords = allCoords.get(i);
            if (theseCoords.equals(coords)) {
                return true;
            }
        }
        return false;
    }

    private static void checkForBlock(ArrayList<ArrayList<Integer>> allCoords, World world, int targetBlockId, int x, int y, int z, int startX, int startY, int startZ) {
        int blockId = world.getBlockId(x, y, z);
        if (blockId == targetBlockId) {
            if (Math.abs(x - startX) >= MAX_VEINMINE_RANGE
                    || Math.abs(y - startY) >= MAX_VEINMINE_RANGE
                    || Math.abs(z - startZ) >= MAX_VEINMINE_RANGE) {
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

            // Check left, right, below, above, front, and back
            checkForBlock(allCoords, world, targetBlockId, x - 1, y, z, startX, startY, startZ);
            checkForBlock(allCoords, world, targetBlockId, x + 1, y, z, startX, startY, startZ);
            checkForBlock(allCoords, world, targetBlockId, x, y - 1, z, startX, startY, startZ);
            checkForBlock(allCoords, world, targetBlockId, x, y + 1, z, startX, startY, startZ);
            checkForBlock(allCoords, world, targetBlockId, x, y, z - 1, startX, startY, startZ);
            checkForBlock(allCoords, world, targetBlockId, x, y, z + 1, startX, startY, startZ);
        }
    }

    static ArrayList<ArrayList<Integer>> getAdjacentBlockCoords(World world, int targetBlockId, int startX, int startY, int startZ) {
        ArrayList<ArrayList<Integer>> allCoords = new ArrayList<>();
        checkForBlock(allCoords, world, targetBlockId, startX, startY, startZ, startX, startY, startZ);
        return allCoords;
    }

    static boolean isItemTool(Item tool) {
        return (tool instanceof ItemToolPickaxe) || (tool instanceof ItemToolSpade);
    }

    static boolean isBlockVeinmineable(Block block) {
        return (block instanceof BlockOre || block instanceof BlockOreMetal || block instanceof BlockOreCoal || block instanceof BlockGravel);
    }


    // Main methods
    public void veinmine(EntityPlayerMP plr, int x, int y, int z) {
        World world = plr.worldObj;
        if (!plr.isHoldingTool()) {
            // without this check we get a NullPointerException if the player breaks a block with their fist
            return;
        }

        ItemStack stack = plr.getCurrentEquippedItem();
        Item tool = stack.getItem();

        if (!plr.isSneaking() || !isItemTool(tool)) {
            return;
        }

        int blockId = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockId];
        if (!isBlockVeinmineable(block) || !tool.canHarvestBlock(block)) {
            return;
        }

        world.removeBlockTileEntity(x, y, z);
        ArrayList<ArrayList<Integer>> coordsToVeinmine = getAdjacentBlockCoords(world, blockId, x, y, z);
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

            block.harvestBlock(world, plr, thisX, thisY, thisZ, metadata);
            block.onBlockDestroyedByPlayer(world, thisX, thisY, thisZ, metadata);
            stack.onDestroyBlock(blockId, thisX, thisY, thisZ, plr);
            world.playAuxSFX(2001, x, y, z, blockId + metadata * 65536);
            world.setBlockWithNotify(thisX, thisY, thisZ, 0);
        }
    }

    public boolean onPlayerBreakBlock(NetworkPlayer plr, RegisteredItemStack stack, int x, int y, int z, int facing, boolean wasCancelled) {
        veinmine(ServerMod.toEntityPlayerMP(plr), x, y, z);

        return false;
    }

    public void onInit() {
        System.out.println("Veinminer server initialized.");
    }
}
