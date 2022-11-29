package me.gatogamer.dynamicpremium.bungee.commands;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.bungee.config.ConfigUtils;
import me.gatogamer.dynamicpremium.commons.cache.Cache;
import me.gatogamer.dynamicpremium.commons.cache.CacheManager;
import me.gatogamer.dynamicpremium.commons.database.Database;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import me.gatogamer.dynamicpremium.commons.utils.UUIDUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class PremiumCommand extends Command {
    public PremiumCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Configuration mainSettings = DynamicPremium.getInstance().getMainSettings();
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Cache cache = DynamicPremium.getInstance().getCacheManager().getOrCreateCache(player.getName());

            ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                Database database = DynamicPremium.getInstance().getDatabaseManager().getDatabase();
                PlayerState state = database.playerState(player.getName());
                if (state == PlayerState.PREMIUM) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Disabled")));
                    database.removePlayer(player.getName());
                } else if (!UUIDUtils.isPremium(player.getName())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.NoPremium")));
                } else {
                    player.disconnect(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Checking")));
                    cache.setPendingVerification(true);
                    cache.setFullPremium(false);
                }
            });
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.PlayerOnly")));
        }
    }
}