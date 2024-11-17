package com.maks.storageplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class StorageCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission("playerstorage.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        PlayerStorage storage = new PlayerStorage(player.getUniqueId());

        // Open the storage GUI on page 1
        StorageGUI.openStorage(player, storage, 1);

        return true;
    }
}
