package com.jellied.veinminer;

import com.fox2code.foxloader.loader.ServerMod;
import net.minecraft.src.game.block.Block;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileWriteAndLoadWhitelist {
    public static void writeWhitelistToFile(final ArrayList<Integer> whitelist, final String filename) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write("whitelist=");

            for (Integer i : whitelist)
                fileWriter.write(i + ",");

            fileWriter.write("\n");
            fileWriter.close();
        } catch(Throwable e) {
            System.err.println("Failed to write whitelist to file: " + filename);
            e.printStackTrace();
        }
    }

    public static ArrayList<Integer> loadWhitelistFromFile(final String filename) {
        ArrayList<Integer> ret = new ArrayList<>();

        Scanner scanner;
        try{
            scanner = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            return ret;
        }

        int lineNum = 0;
        while (scanner.hasNextLine()) {
            ++lineNum;
            String line = scanner.nextLine();

            // Let's allow lines starting with a hash to be comments
            if (line.startsWith("#") || line.isEmpty())
                continue;

            int separatorIndex = line.indexOf("=");
            if (separatorIndex == -1) {
                ServerMod.getGameInstance().logWarning(VeinminerServer.loggingPrefix + "Failed to find '=' separator on line " + lineNum + " in whitelist file " + filename);
                continue;
            }

            String value = line.substring(separatorIndex + 1).trim();

            if (line.startsWith("whitelist=")) {
                String[] whitelistBlockIDsFromLine = value.split(",");
                for (int i = 0; i < whitelistBlockIDsFromLine.length; i++) {
                    int blockID;
                    try {
                        blockID = Integer.parseInt(whitelistBlockIDsFromLine[i]);
                        // Let's filter out any invalid block IDs because they can crash clients through the Packet61SoundFX packet
                        if (blockID >= Block.blocksList.length){
                            ServerMod.getGameInstance().logWarning(VeinminerServer.loggingPrefix + "Ignoring invalid block ID " + blockID + " found on line " + lineNum + ", at element index " + i + " in whitelist file " + filename);
                            continue;
                        }
                    } catch(NumberFormatException e) {
                        ServerMod.getGameInstance().logWarning(VeinminerServer.loggingPrefix + "Failed to parse integer on line " + lineNum + ", at element index " + i + " in whitelist file " + filename);
                        continue;
                    }

                    if (!ret.contains(blockID))
                        ret.add(blockID);
                }
            }
        }

        return ret;
    }
}
