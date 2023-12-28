package me.gatogamer.dynamicpremium.bungee.commands;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.commons.cache.Cache;
import me.gatogamer.dynamicpremium.commons.database.Database;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import me.gatogamer.dynamicpremium.commons.utils.UUIDUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullPremiumCommand extends Command {

    private final Map<String, Long> cooldown = new HashMap<>();
    private final List<String> confirm = new ArrayList<>();

    public FullPremiumCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dynamicpremium.fullpremium")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', DynamicPremium.getInstance().getMainSettings().getString("Admin.No Permission")));
            return;
        }
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            final long time = System.currentTimeMillis();
            if (!confirm.contains(player.getName()) && cooldown.containsKey(player.getName())) {
                final long playerTime = cooldown.get(player.getName());
                if (playerTime > time) {
                    final long seconds = (playerTime - time) / 1000;
                    player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                            DynamicPremium.getInstance().getMainSettings().getString("CooldownMessage").replace("%seconds%", String.valueOf(seconds))
                    )));
                    return;
                }
            }

            Configuration mainSettings = DynamicPremium.getInstance().getMainSettings();
            cooldown.put(player.getName(), time + mainSettings.getLong("PremiumCommandDelay", 60000));

            Cache cache = DynamicPremium.getInstance().getCacheManager().getOrCreateCache(player.getName());

            ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                Database database = DynamicPremium.getInstance().getDatabaseManager().getDatabase();
                PlayerState state = database.playerState(player.getName());
                if (state == PlayerState.FULL_PREMIUM) {
                    player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Disabled"))));
                    database.removePlayer(player.getName());
                    DynamicPremium.getInstance().getWebhookNoPremium().send(s -> s.replace("%player%", player.getName()));
                } else if (!UUIDUtils.isPremium(player.getName())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.NoPremium")));
                } else if (!confirm.contains(player.getName())) {
                    confirm.add(player.getName());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.FullPremium")));
                } else {
                    confirm.remove(player.getName());
                    final String host = mainSettings.getString("PremiumVerifyServer", "false");
                    if (!host.equalsIgnoreCase("false")) {
                        final ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(host);
                        if (serverInfo != null) {
                            player.connect(serverInfo);
                            return;
                        }
                    }
                    player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("Alert.Checking").replace("/premium", "/fullpremium"))));
                    cache.setPendingVerification(true);
                    cache.setFullPremium(true);
                }
            });
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', DynamicPremium.getInstance().getMainSettings().getString("Alert.PlayerOnly")));
        }
    }
}