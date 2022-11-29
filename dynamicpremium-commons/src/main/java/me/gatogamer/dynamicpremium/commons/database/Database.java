package me.gatogamer.dynamicpremium.commons.database;

import me.gatogamer.dynamicpremium.commons.config.IConfigParser;

public interface Database {
    void loadDatabase(IConfigParser iConfigParser, DatabaseManager databaseManager);
    default PlayerState playerState(String name) {
        return playerIsPremium(name) ? PlayerState.PREMIUM : PlayerState.NO_PREMIUM;
    }
    boolean playerIsPremium(String name);
    default void updatePlayer(String name) {
        updatePlayer(name, PlayerState.PREMIUM);
    }
    void updatePlayer(String name, PlayerState state);
    void removePlayer(String name);
}
