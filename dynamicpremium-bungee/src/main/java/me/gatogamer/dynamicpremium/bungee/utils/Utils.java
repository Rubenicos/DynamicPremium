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

    private static final MethodHandle setUniqueId;
    private static boolean error = false;

    static {
        MethodHandle set$uniqueId = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Field field = InitialHandler.class.getDeclaredField("uniqueId");
            field.setAccessible(true);
            set$uniqueId = lookup.unreflectSetter(field);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        setUniqueId = set$uniqueId;
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
            setUniqueId.invoke(pendingConnection, uuid);
        } catch (Throwable t) {
            if (!error) {
                error = true;
                t.printStackTrace();
            }
            pendingConnection.setUniqueId(uuid);
        }
    }
}