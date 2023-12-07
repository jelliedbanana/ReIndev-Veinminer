package com.jellied.veinminer.chatcommands;

import com.fox2code.foxloader.network.ChatColors;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.jellied.veinminer.WhitelistHandlerServer;
import net.minecraft.src.game.block.Block;

public class VeinmineWhitelistChatCommandServer extends CommandCompat {
    public VeinmineWhitelistChatCommandServer() {
        super("veinminewhitelist", true);
    }

    public String commandSyntax() {
        return ChatColors.YELLOW + "/veinminewhitelist <add/remove> <block name>";
    }

    @Override
    public void onExecute(String[] args, NetworkPlayer user) {
        if (args.length <= 2) {
            user.displayChatMessage(commandSyntax());
            return;
        }

        if (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove")) {
            user.displayChatMessage(ChatColors.RED + "Please either specify 'add' or 'remove'");
            return;
        }

        int id = -1;
        String targetBlockName = args[2];
        for (int i = 0; i < Block.blocksList.length; i++) {
            Block block = Block.blocksList[i];
            if (block == null) {
                continue;
            }

            if (block.getBlockName().replace("tile.", "").equalsIgnoreCase(targetBlockName)) {
                id = i;
                break;
            }
        }

        if (id == -1) {
            user.displayChatMessage(ChatColors.RED + "Could not find block named '" + targetBlockName + "'");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            whitelistAdd(user, targetBlockName, id);
        }
        else {
            whitelistRemove(user, targetBlockName, id);
        }
    }

    public void whitelistAdd(NetworkPlayer user, String blockName, int blockId) {
        if (WhitelistHandlerServer.isBlockWhitelisted(blockId)) {
            user.displayChatMessage(ChatColors.RED + "'" + blockName + "' is already whitelisted!");
            return;
        }

        WhitelistHandlerServer.addToWhitelist(blockId);
        user.displayChatMessage(ChatColors.GREEN + "Whitelisted block '" + blockName + "'");
    }

    public void whitelistRemove(NetworkPlayer user, String blockName, int blockId) {
        if (!WhitelistHandlerServer.isBlockWhitelisted(blockId)) {
            user.displayChatMessage(ChatColors.RED + "'" + blockName + "' is not whitelisted!");
            return;
        }

        WhitelistHandlerServer.removeFromWhitelist(blockId);
        user.displayChatMessage(ChatColors.GREEN + "Removed block '" + blockName + "' from veinmine whitelist");
    }
}
