package me.gatogamer.dynamicpremium.bungee.commands;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.commons.cache.Cache;
import me.gatogamer.dynamicpremium.commons.database.Database;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import me.gatogamer.dynamicpremium.commons.utils.UUIDUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class FullPremiumCommand extends Command {

    private final List<String> confirm = new ArrayList<>();

    public FullPremiumCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Configuration mainSettings = DynamicPremium.getInstance().getMainSettings();
        if (!sender.hasPermission("dynamicpremium.fullpremium")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Admin.No Permission")));
            return;
        }
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Cache cache = DynamicPremium.getInstance().getCacheManager().getOrCreateCache(player.getName());

            ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                Database database = DynamicPremium.getInstance().getDatabaseManager().getDatabase();
                PlayerState state = database.playerState(player.getName());
                if (state == PlayerState.FULL_PREMIUM) {
                    player.disconnect(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Disabled")));
                    database.removePlayer(player.getName());
                } else if (!UUIDUtils.isPremium(player.getName())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.NoPremium")));
                } else if (!confirm.contains(player.getName())) {
                    confirm.add(player.getName());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.FullPremium")));
                } else {
                    confirm.remove(player.getName());
                    player.disconnect(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Checking").replace("/premium", "/fullpremium")));
                    cache.setPendingVerification(true);
                    cache.setFullPremium(true);
                }
            });
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.PlayerOnly")));
        }
    }
}