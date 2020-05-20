package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Acrobot
 */
public class Version implements CommandExecutor {
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, String[] args) {
        if (args.length > 0 && args[0].equals("reload")) {
            ChestShop.getPlugin().loadConfig();

            sender.sendMessage(ChatColor.DARK_GREEN + "The config was reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + ChestShop.getPluginName() + "'s version is: " + ChatColor.GREEN + ChestShop.getVersion());
        return true;
    }
}
