package me.gatogamer.dynamicpremium.commons.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
    // "id":" <UUID> "
    private static final String UUID_START = "\"id\":\"";
    private static final String UUID_END = "\"";
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

    private static void computeUUID(String name) {
        try (InputStream in = new URL(USER_API + name).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            final StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            String s = out.toString();
            int index = s.indexOf(UUID_START);
            final int begin;
            if (index >= 0 && (begin = index + UUID_START.length()) < s.length()) {
                s = s.substring(begin);
                index = s.indexOf(UUID_END);
                if (index > 0) {
                    s = s.substring(0, index).trim();
                    if (s.length() == 32) {
                        // UUID without slashes
                        CACHE.put(name, UUID.fromString(new StringBuilder(s)
                                .insert(20, '-')
                                .insert(16, '-')
                                .insert(12, '-')
                                .insert(8, '-')
                                .toString()
                        ));
                        return;
                    } else if (s.contains("-") && s.length() == 36) {
                        // UUID with slashes
                        CACHE.put(name, UUID.fromString(s));
                        return;
                    }
                }
            }
        } catch (Throwable ignored) { }
        CACHE.put(name, false);
    }
}
