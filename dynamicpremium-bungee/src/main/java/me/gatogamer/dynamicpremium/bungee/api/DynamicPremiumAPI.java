package me.gatogamer.dynamicpremium.bungee.api;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import me.gatogamer.dynamicpremium.bungee.api.event.PlayerStateChangeEvent;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class DynamicPremiumAPI {

    @NotNull
    public static PlayerState getState(@NotNull ProxiedPlayer player) {
        return getState(player.getName());
    }

    @NotNull
    public static PlayerState getState(@NotNull String name) {
        return DynamicPremium.getInstance().getDatabaseManager().getDatabase().playerState(name);
    }

    public static void setState(@NotNull ProxiedPlayer player, @NotNull PlayerState state, boolean callEvent) {
        setState(player.getName(), state, callEvent);
    }

    public static void setState(@NotNull String name, @NotNull PlayerState state, boolean callEvent) {
        if (state == PlayerState.NO_PREMIUM) {
            DynamicPremium.getInstance().getDatabaseManager().getDatabase().removePlayer(name);
        } else {
            DynamicPremium.getInstance().getDatabaseManager().getDatabase().updatePlayer(name, state);
        }
        if (callEvent) {
            callEvent(name, state);
        }
    }

    public static boolean toggleState(@NotNull ProxiedPlayer player, @NotNull PlayerState state, boolean callEvent) {
        return toggleState(player.getName(), state, callEvent);
    }

    public static boolean toggleState(@NotNull String name, @NotNull PlayerState state, boolean callEvent) {
        final PlayerState databaseState = DynamicPremium.getInstance().getDatabaseManager().getDatabase().playerState(name);
        if (databaseState == state) {
            DynamicPremium.getInstance().getDatabaseManager().getDatabase().removePlayer(name);
            if (callEvent) {
                callEvent(name, PlayerState.NO_PREMIUM);
            }
            return false;
        } else {
            DynamicPremium.getInstance().getDatabaseManager().getDatabase().updatePlayer(name, state);
            if (callEvent) {
                callEvent(name, state);
            }
            return true;
        }
    }

    private static void callEvent(@NotNull String name, @NotNull PlayerState state) {
        final PlayerStateChangeEvent event = new PlayerStateChangeEvent(name, state);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        switch (state) {
            case PREMIUM -> DynamicPremium.getInstance().getWebhookPremium().send(s -> s.replace("%player%", name));
            case NO_PREMIUM -> DynamicPremium.getInstance().getWebhookNoPremium().send(s -> s.replace("%player%", name));
            case FULL_PREMIUM -> DynamicPremium.getInstance().getWebhookFullPremium().send(s -> s.replace("%player%", name));
        }
    }
}
