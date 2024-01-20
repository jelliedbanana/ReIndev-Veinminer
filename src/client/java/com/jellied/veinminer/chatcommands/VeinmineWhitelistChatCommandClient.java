package com.jellied.veinminer.chatcommands;

import com.fox2code.foxloader.network.ChatColors;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.jellied.veinminer.VeinminerClient;
import com.jellied.veinminer.WhitelistHandlerClient;
import net.minecraft.src.game.block.Block;

public class VeinmineWhitelistChatCommandClient extends CommandCompat {
    public VeinmineWhitelistChatCommandClient() {
        super("veinminewhitelist", true);
    }

    public String commandSyntax() {
        return ChatColors.YELLOW + "/veinminewhitelist <add/remove> <block name or id>";
    }

    @Override
    public void onExecute(String[] args, NetworkPlayer commandExecutor) {
        if (args.length <= 2) {
            commandExecutor.displayChatMessage(commandSyntax());
            commandExecutor.displayChatMessage(ChatColors.GREEN + "Veinmine whitelist:");
            for (int blockId : WhitelistHandlerClient.getWhitelist()){
                final Block block = Block.blocksList[blockId];
                commandExecutor.displayChatMessage(ChatColors.AQUA + block.getBlockName().substring(5));
            }
            return;
        }

        final String addOrRemove = args[1];
        final String blockNameOrID = args[2];

        if (addOrRemove.isEmpty() || blockNameOrID.isEmpty()) {
            commandExecutor.displayChatMessage(ChatColors.RED + "You can't specify an empty action or block name/id");
            commandExecutor.displayChatMessage(ChatColors.RED + "You probably typed 2 spaces somewhere in the command!");
            return;
        }

        int blockID;
        try{
            blockID = Integer.parseInt(blockNameOrID);
        } catch(NumberFormatException e) {
            try{
                blockID = Integer.parseInt(Block.getBlockByName(blockNameOrID));
            } catch(NumberFormatException e2) {
                commandExecutor.displayChatMessage(ChatColors.RED + "Failed to find a block named \"" + ChatColors.RESET + blockNameOrID + ChatColors.RED + "\"");
                return;
            }
        }

        Block block;
        try {
            block = Block.blocksList[blockID];
        } catch(ArrayIndexOutOfBoundsException e) {
            commandExecutor.displayChatMessage(ChatColors.RED + "Invalid block ID " + ChatColors.RESET + blockNameOrID);
            return;
        }

        // The .substring(5) removes "tile." prefixing every block name
        if (addOrRemove.equalsIgnoreCase("add")) {
            whitelistAdd(commandExecutor, block.getBlockName().substring(5), blockID);
        } else if (addOrRemove.equalsIgnoreCase("remove")) {
            whitelistRemove(commandExecutor, block.getBlockName().substring(5), blockID);
        } else {
            commandExecutor.displayChatMessage(commandSyntax());
        }
    }

    public void whitelistAdd(NetworkPlayer user, String blockName, int blockId) {
        if (WhitelistHandlerClient.isBlockWhitelisted(blockId)) {
            user.displayChatMessage(ChatColors.RED + "'" + blockName + "' is already whitelisted!");
            return;
        }

        WhitelistHandlerClient.addToWhitelist(blockId);
        user.displayChatMessage(ChatColors.GREEN + "Whitelisted block '" + blockName + "'");

        if (VeinminerClient.isBlockDefaultVeinmineable(Block.blocksList[blockId]))
            user.displayChatMessage(ChatColors.YELLOW + "This block is already a default veinminable block");
    }

    public void whitelistRemove(NetworkPlayer user, String blockName, int blockId) {
        if (!WhitelistHandlerClient.isBlockWhitelisted(blockId)) {
            user.displayChatMessage(ChatColors.RED + "'" + blockName + "' is not whitelisted!");
            return;
        }

        WhitelistHandlerClient.removeFromWhitelist(blockId);
        user.displayChatMessage(ChatColors.GREEN + "Removed block '" + blockName + "' from veinmine whitelist");
    }
}
