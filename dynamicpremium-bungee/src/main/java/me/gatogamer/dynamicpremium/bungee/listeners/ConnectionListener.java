package me.gatogamer.dynamicpremium.bungee.listeners;

import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.bungee.utils.Utils;
import me.gatogamer.dynamicpremium.commons.cache.Cache;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ConnectionListener implements Listener {
    private final DynamicPremium dynamicPremium;

    @EventHandler(priority = -64)
    public void onLogin(LoginEvent e) {
        PendingConnection pendingConnection = e.getConnection();
        if (pendingConnection == null || !pendingConnection.isConnected()) {
            return;
        }
        String name = pendingConnection.getName();
        Cache cache = dynamicPremium.getCacheManager().getCache(name);

        if (cache == null || !cache.isFullPremium()) {
            Configuration configuration = dynamicPremium.getMainSettings();
            if (configuration.getString("UUIDMode").equalsIgnoreCase("NO_PREMIUM")) {
                UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                Utils.setUuid(pendingConnection, offlineUuid);
            }
        }
    }
}