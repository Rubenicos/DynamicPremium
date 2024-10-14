package me.gatogamer.dynamicpremium.bungee.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.gatogamer.dynamicpremium.commons.database.PlayerState;
import net.md_5.bungee.api.plugin.Event;

@RequiredArgsConstructor
@Getter
public class PlayerStateChangeEvent extends Event {

    private final String name;
    private final PlayerState state;
}
