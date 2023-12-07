package com.jellied.veinminer.chatcommands;

import net.minecraft.src.client.gui.GuiChat;
import net.minecraft.src.game.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CommandWhitelistAutocomplete {
    List<String> operations = new ArrayList<>();
    List<String> blocks = new ArrayList<>();
    List<String> blankList = new ArrayList<>();

    public CommandWhitelistAutocomplete() {
        operations.add("add");
        operations.add("remove");

        for (Block block : Block.blocksList) {
            if (block != null) {
                blocks.add(block.getBlockName().replaceFirst("tile.", ""));
            }
        }
    }

    public List<String> getEntriesThatBeginWith(List<String> listToCheck, String with) {
        List<String> entries = new ArrayList<>();

        for (String entry : listToCheck) {
            if (entry != null && entry.startsWith(with)) {
                entries.add(entry);
            }
        }

        return entries;
    }

    public List<String> getCommandSuggestions(GuiChat gui, int commandArgIndex) {
        if (commandArgIndex == 1) {
            String typedOperation = gui.chat.text.replaceFirst("/veinminewhitelist ", "");

            return getEntriesThatBeginWith(operations, typedOperation);
        }
        else if (commandArgIndex == 2) {
            String typedBlock = gui.chat.text.replaceFirst("/veinminewhitelist ", "");
            String[] splitCommand = typedBlock.split(" ");
            typedBlock = typedBlock.replaceFirst(splitCommand[0] + " ", "");

            return getEntriesThatBeginWith(blocks, typedBlock);
        }

        return blankList;
    }
}
