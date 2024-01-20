# ReIndev-Veinminer
Simple veinminer mod for ReIndev v2.8.1

To veinmine, sneak while breaking a block with the **_appropriate tool_** and you'll break the whole vein at once!\
You can also veinmine trees with an axe, clay and gravel with a shovel, and if you want, you can whitelist any block yourself.\
You can not veinmine without using a tool. You need to be holding a sword, pickaxe, axe, shovel or hoe.

### Veinminable blocks:
- Ores
- Gravel
- Wood logs (naturally generated, not placed by players)
- Clay

# Whitelisting blocks
You can make additional blocks veinminable with the command below.\
You can not remove or alter the [default veinminable blocks](#veinminable-blocks).

`/veinminewhitelist <add/remove> <block name or id>` (Requires cheats enabled / OP-only)

Example: `/veinminewhitelist add cherry_leaves`\
Typing just `/veinminewhitelist` will show the current whitelist

To mitigate the inconvenience of finding the exact name of your desired block, I made the command compatible with [my autocomplete mod!](https://github.com/jelliedbanana/ReIndev-CommandAutocomplete)

# Known issues/quirks
- Glass and dead bushes can not be veinmined even if whitelisted. There are probably more blocks like this.
- Veinmining favors going in the direction negative X

- The whitelist does not persist over server restart for servers
- Veinmining does not work in creative mode on servers

# Other info / for developers
In singleplayer (client), this mod stores its whitelist as a string of block id integers separated by "//" in the NBT tag `jelliedveinminewhitelist` in the world.
In multiplayer (server), this mod stores its whitelist in `mods/ReIndevVeinminer/whitelist.txt`