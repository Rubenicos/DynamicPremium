package me.gatogamer.dynamicpremium.commons.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This code has been created by
 * gatogamer#6666 A.K.A. gatogamer.
 * If you want to use my code, please
 * don't remove this messages and
 * give me the credits. Arigato! n.n
 */
public class UUIDUtils {

    private static final String USER_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Cache<String, Object> CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    public static UUID getOnlineUUID(String name) {
        Object object = CACHE.getIfPresent(name);
        if (object == null) {
            computeUUID(name);
            object = CACHE.getIfPresent(name);
        }
        if (object instanceof UUID) {
            return (UUID) object;
        } else {
            return null;
        }
    }

    public static boolean isPremium(String name) {
        return getOnlineUUID(name) != null;
    }

    public static boolean isXboxId(UUID uuid) {
        return uuid != null && uuid.toString().startsWith("00000000-0000-0000");
    }

    private static void computeUUID(String name) {
        try (InputStream in = new URL(USER_API + name).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            final StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            JsonElement data = JsonParser.parseString(out.toString());
            if (data instanceof JsonObject) {
                JsonObject json = (JsonObject) data;
                if (json.has("id")) {
                    final String id = json.getAsJsonPrimitive("id").getAsString();
                    if (id.length() == 32) {
                        // UUID without slashes
                        CACHE.put(name, UUID.fromString(new StringBuilder(id)
                                .insert(20, '-')
                                .insert(16, '-')
                                .insert(12, '-')
                                .insert(8, '-')
                                .toString()
                        ));
                        return;
                    } else if (id.contains("-") && id.length() == 36) {
                        // UUID with slashes
                        CACHE.put(name, UUID.fromString(id));
                        return;
                    }
                }
            }
        } catch (Throwable ignored) { }
        CACHE.put(name, false);
    }
}
