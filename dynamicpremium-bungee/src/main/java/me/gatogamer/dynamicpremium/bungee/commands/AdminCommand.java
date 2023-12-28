package me.gatogamer.dynamicpremium.bungee.commands;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class AdminCommand extends Command {
    public AdminCommand(String name, String... aliases) {
        super(name, null, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Configuration mainSettings = DynamicPremium.getInstance().getMainSettings();
        if (!sender.hasPermission("dynamicpremium.use")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Admin.No Permission")));
            return;
        }

        if (args.length < 2) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                DynamicPremium.getInstance().onReload();
            } else {
                for (String msg : mainSettings.getStringList("Admin.Usage")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
            }
            return;
        }

        switch (args[0].toLowerCase()) {
            case "state":
                if (sender.hasPermission("dynamicpremium.state")) {
                    ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                        String name = args[1];
                        PlayerState state = DynamicPremium.getInstance().getDatabaseManager().getDatabase().playerState(name);
                        sender.sendMessage("§aEl estado actual del jugador §f" + name + " §a es: §f" + state);
                    });
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Admin.No Permission")));
                }
                break;
            case "toggle":
            case "premium":
                if (sender.hasPermission("dynamicpremium.toggle")) {
                    ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                        String name = args[1];
                        PlayerState state = DynamicPremium.getInstance().getDatabaseManager().getDatabase().playerState(name);
                        if (state == PlayerState.PREMIUM) {
                            DynamicPremium.getInstance().getDatabaseManager().getDatabase().removePlayer(name);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString(
                                    "Admin.Toggled.Disabled").replaceAll("%player%", args[1])
                            ));
                        } else {
                            DynamicPremium.getInstance().getDatabaseManager().getDatabase().updatePlayer(name, PlayerState.PREMIUM);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString(
                                    "Admin.Toggled.Enabled").replaceAll("%player%", args[1])
                            ));
                        }
                    });
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Admin.No Permission")));
                }
                break;
            case "full":
            case "fullpremium":
                if (sender.hasPermission("dynamicpremium.toggle.full")) {
                    ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                        String name = args[1];
                        PlayerState state = DynamicPremium.getInstance().getDatabaseManager().getDatabase().playerState(name);
                        if (state == PlayerState.FULL_PREMIUM) {
                            DynamicPremium.getInstance().getDatabaseManager().getDatabase().removePlayer(name);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString(
                                    "Admin.Full.Disabled").replaceAll("%player%", args[1])
                            ));
                        } else {
                            DynamicPremium.getInstance().getDatabaseManager().getDatabase().updatePlayer(name, PlayerState.FULL_PREMIUM);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString(
                                    "Admin.Full.Enabled").replaceAll("%player%", args[1])
                            ));
                        }
                    });
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Admin.No Permission")));
                }
                break;
            default:
                for (String msg : mainSettings.getStringList("Admin.Usage")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
                break;
        }
    }
}
