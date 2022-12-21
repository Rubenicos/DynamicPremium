package me.gatogamer.dynamicpremium.bungee.listeners;

import lombok.RequiredArgsConstructor;
import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.commons.cache.Cache;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

/**
 * This code has been created by
 * gatogamer#6666 A.K.A. gatogamer.
 * If you want to use my code, please
 * don't remove this messages and
 * give me the credits. Arigato! n.n
 */
@RequiredArgsConstructor
public class ServerConnectionListener implements Listener {
    private final DynamicPremium dynamicPremium;

    @EventHandler
    public void onServerChangeEvent(ServerConnectEvent e) {
        Configuration settings = DynamicPremium.getInstance().getMainSettings();
        ProxiedPlayer proxiedPlayer = e.getPlayer();
        Cache cache = dynamicPremium.getCacheManager().getOrCreateCache(proxiedPlayer.getName());
        cache.updateUsage();

        if (cache.isPremium()) {
            ServerInfo newTarget = DynamicPremium.getInstance().getLobbySelector().getLobby(settings, proxiedPlayer);
            if (settings.getStringList("AuthServers").contains(e.getTarget().getName())) {
                e.setTarget(newTarget);
            }
        }
        if (settings.getStringList("access-only-if-player-is-premium").contains(e.getTarget().getName().toLowerCase())) {
            String message = ChatColor.translateAlternateColorCodes('&', settings.getString("access-only-if-player-is-premium-message"));

            if (!cache.isPremium()) {
                e.setCancelled(true);
                proxiedPlayer.sendMessage(message);
                if (proxiedPlayer.getServer() == null) {
                    proxiedPlayer.disconnect(message); // disconnect because they don't have any server to connect to.
                }
            }
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent e) {
        final Configuration settings = DynamicPremium.getInstance().getMainSettings();
        final String host = settings.getString("PremiumVerifyServer", "false");
        if (!host.equalsIgnoreCase("false") && e.getServer() != null && e.getServer().getInfo().getName().equals(host)) {
            final ProxiedPlayer player = e.getPlayer();
            ProxyServer.getInstance().getScheduler().runAsync(DynamicPremium.getInstance(), () -> {
                if (player.isConnected() && player.getServer().getInfo().getName().equals(host)) {
                    Cache cache = dynamicPremium.getCacheManager().getOrCreateCache(player.getName());
                    player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', settings.getString("Alert.Checking").replace("/premium", "/fullpremium"))));
                    cache.setPendingVerification(true);
                    cache.setFullPremium(true);
                }
            });
        }
    }
}