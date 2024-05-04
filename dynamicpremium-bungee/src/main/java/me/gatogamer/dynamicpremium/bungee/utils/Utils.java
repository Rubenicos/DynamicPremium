package me.gatogamer.dynamicpremium.bungee.utils;

import lombok.experimental.UtilityClass;
import me.gatogamer.dynamicpremium.commons.utils.UUIDUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * This code has been created by
 * gatogamer#6666 A.K.A. gatogamer.
 * If you want to use my code, please
 * don't remove this messages and
 * give me the credits. Arigato! n.n
 */
@UtilityClass
public class Utils {

    private static final MethodHandle SET_UNIQUE_ID;
    private static final MethodHandle SET_REWRITE_ID;
    private static boolean error = false;

    static {
        MethodHandle set$uniqueId = null;
        MethodHandle set$rewriteId = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Field uniqueId = InitialHandler.class.getDeclaredField("uniqueId");
            uniqueId.setAccessible(true);
            set$uniqueId = lookup.unreflectSetter(uniqueId);

            final Field rewriteId = InitialHandler.class.getDeclaredField("rewriteId");
            rewriteId.setAccessible(true);
            set$rewriteId = lookup.unreflectSetter(rewriteId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        SET_UNIQUE_ID = set$uniqueId;
        SET_REWRITE_ID = set$rewriteId;
    }

    public String colorize(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void setUuid(PendingConnection pendingConnection, UUID uuid) {
        // Not override floodgate uuids
        if (UUIDUtils.isXboxId(pendingConnection.getUniqueId())) {
            return;
        }
        try {
            SET_UNIQUE_ID.invoke(pendingConnection, uuid);

            SET_REWRITE_ID.invoke(pendingConnection, uuid);
        } catch (Throwable t) {
            if (!error) {
                error = true;
                t.printStackTrace();
            }
            pendingConnection.setUniqueId(uuid);
        }
    }
}