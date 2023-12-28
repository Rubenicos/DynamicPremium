package me.gatogamer.dynamicpremium.bungee;

import lombok.Getter;
import lombok.Setter;
import me.gatogamer.dynamicpremium.bungee.commands.AdminCommand;
import me.gatogamer.dynamicpremium.bungee.commands.FullPremiumCommand;
import me.gatogamer.dynamicpremium.bungee.commands.PremiumCommand;
import me.gatogamer.dynamicpremium.bungee.config.BungeeConfigParser;
import me.gatogamer.dynamicpremium.bungee.config.ConfigCreator;
import me.gatogamer.dynamicpremium.bungee.config.ConfigUtils;
import me.gatogamer.dynamicpremium.bungee.imports.FastLoginImport;
import me.gatogamer.dynamicpremium.bungee.imports.FileConfigurationImport;
import me.gatogamer.dynamicpremium.bungee.listeners.ChatListener;
import me.gatogamer.dynamicpremium.bungee.listeners.ConnectionListener;
import me.gatogamer.dynamicpremium.bungee.listeners.PostConnectionListener;
import me.gatogamer.dynamicpremium.bungee.listeners.PreConnectionListener;
import me.gatogamer.dynamicpremium.bungee.listeners.ServerConnectionListener;
import me.gatogamer.dynamicpremium.bungee.lobby.LobbySelector;
import me.gatogamer.dynamicpremium.bungee.utils.Utils;
import me.gatogamer.dynamicpremium.commons.cache.CacheManager;
import me.gatogamer.dynamicpremium.commons.database.DatabaseManager;
import me.gatogamer.dynamicpremium.commons.utils.TinyWebhook;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public final class DynamicPremium extends Plugin {

    @Getter
    private static DynamicPremium instance;

    private CacheManager cacheManager;
    private Configuration mainSettings;
    private ConfigUtils configUtils;
    private DatabaseManager databaseManager;
    private LobbySelector lobbySelector;

    private TinyWebhook tinyWebhook;
    private TinyWebhook.Message webhookPremium;
    private TinyWebhook.Message webhookNoPremium;
    private TinyWebhook.Message webhookFullPremium;

    @Override
    public void onEnable() {
        instance = this;
        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Loading &cDynamicPremium &7by &cgatogamer#6666"));

        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Loading configurations using an API by &ciSnakeBuzz_"));
        setConfigUtils(new ConfigUtils());
        ConfigCreator.get().setupBungee(this, "Settings");
        ConfigCreator.get().setupBungee(this, "PremiumUsers");
        mainSettings = ConfigUtils.getConfig(this, "Settings");

        cacheManager = new CacheManager();

        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Loading commands"));
        getProxy().getPluginManager().registerCommand(this, new PremiumCommand("premium"));
        getProxy().getPluginManager().registerCommand(this, new FullPremiumCommand("fullpremium"));
        getProxy().getPluginManager().registerCommand(this, new AdminCommand("premiumadmin", "dynamicpremium", "dpremium"));
        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Commands loaded"));

        lobbySelector = new LobbySelector();

        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Loading ConnectionListener"));
        getProxy().getPluginManager().registerListener(this, new ChatListener(mainSettings));
        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this));
        getProxy().getPluginManager().registerListener(this, new PostConnectionListener(this));
        getProxy().getPluginManager().registerListener(this, new PreConnectionListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerConnectionListener(this));
        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Listeners loaded"));

        loadWebhook();

        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Loading database."));
        databaseManager = new DatabaseManager(new BungeeConfigParser(mainSettings), getDataFolder());
        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7Database loaded."));

        new FastLoginImport();
        new FileConfigurationImport();

        getProxy().getScheduler().schedule(this, () -> getProxy().getPlayers().forEach(proxiedPlayer -> {
            cacheManager.getOrCreateCache(proxiedPlayer.getName()).updateUsage();
            cacheManager.tick();
        }), 500L, TimeUnit.MILLISECONDS);

        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7DynamicPremium has been loaded"));
    }

    public void onReload() {
        ConfigCreator.get().setupBungee(this, "Settings");
        ConfigCreator.get().setupBungee(this, "PremiumUsers");
        mainSettings = ConfigUtils.getConfig(this, "Settings");
        loadWebhook();
        databaseManager.reload(new BungeeConfigParser(mainSettings));
        ProxyServer.getInstance().getConsole().sendMessage(Utils.colorize("&cDynamicPremium &8> &7DynamicPremium has been reloaded"));
    }

    private void loadWebhook() {
        final String url = mainSettings.getString("Webhook.Url", "");
        final String username = mainSettings.getString("Webhook.Username");
        final String avatarUrl = mainSettings.getString("Webhook.AvatarUrl");
        final boolean tts = mainSettings.getBoolean("Webhook.Tts", false);
        tinyWebhook = new TinyWebhook(url, username, avatarUrl, tts);
        webhookPremium = tinyWebhook.msg(mainSettings.getString("Webhook.Premium.Content", ""))
                .username(mainSettings.getString("Webhook.Premium.Username"))
                .avatarUrl(mainSettings.getString("Webhook.Premium.AvatarUrl"))
                .tts(mainSettings.getBoolean("Webhook.Premium.Tts", false));
        webhookNoPremium = tinyWebhook.msg(mainSettings.getString("Webhook.NoPremium.Content", ""))
                .username(mainSettings.getString("Webhook.NoPremium.Username"))
                .avatarUrl(mainSettings.getString("Webhook.NoPremium.AvatarUrl"))
                .tts(mainSettings.getBoolean("Webhook.NoPremium.Tts", false));
        webhookFullPremium = tinyWebhook.msg(mainSettings.getString("Webhook.FullPremium.Content", ""))
                .username(mainSettings.getString("Webhook.FullPremium.Username"))
                .avatarUrl(mainSettings.getString("Webhook.FullPremium.AvatarUrl"))
                .tts(mainSettings.getBoolean("Webhook.FullPremium.Tts", false));
    }
}
