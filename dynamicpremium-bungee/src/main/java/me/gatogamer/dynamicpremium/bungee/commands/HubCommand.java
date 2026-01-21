package me.gatogamer.dynamicpremium.bungee.commands;

import me.gatogamer.dynamicpremium.bungee.DynamicPremium;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class HubCommand extends Command {

    private final DynamicPremium dynamicPremium = DynamicPremium.getInstance();

    public HubCommand(String name, String... aliases) {
        super(name, null, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            Configuration mainSettings = dynamicPremium.getMainSettings();
            ServerInfo newTarget = dynamicPremium.getLobbySelector().getLobby(mainSettings, player);
            player.connect(newTarget);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', mainSettings.getString("SendLobbyMessage")));
        }
    }
}
